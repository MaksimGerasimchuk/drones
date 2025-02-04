package com.gerasimchuk.droneslogistics.service.impl;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoState;
import com.gerasimchuk.droneslogistics.common.enumeration.DroneModelType;
import com.gerasimchuk.droneslogistics.common.enumeration.DroneState;
import com.gerasimchuk.droneslogistics.common.exception.DroneException;
import com.gerasimchuk.droneslogistics.common.exception.ResourceNotFoundException;
import com.gerasimchuk.droneslogistics.domain.entity.Cargo;
import com.gerasimchuk.droneslogistics.domain.entity.Drone;
import com.gerasimchuk.droneslogistics.domain.entity.DroneBatteryCapacity;
import com.gerasimchuk.droneslogistics.domain.repository.CargoRepository;
import com.gerasimchuk.droneslogistics.domain.repository.DroneBatteryCapacityRepository;
import com.gerasimchuk.droneslogistics.domain.repository.DroneRepository;
import com.gerasimchuk.droneslogistics.service.api.DroneService;
import com.gerasimchuk.droneslogistics.service.api.in.CargoIn;
import com.gerasimchuk.droneslogistics.service.api.in.DroneIn;
import com.gerasimchuk.droneslogistics.service.api.out.CargoOut;
import com.gerasimchuk.droneslogistics.service.api.out.DroneOut;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DroneServiceImpl implements DroneService {

    private static final Integer BATTERY_CAPACITY_LOW_VALUE = 25;
    private static final String DRONE_PREFIX = "MEGADRONE_";
    private final DroneRepository droneRepository;
    private final CargoRepository cargoRepository;
    private final DroneBatteryCapacityRepository capacityRepository;
    private final Random random = new Random();

    @PostConstruct
    void postConstruct() {
        log.info("Generating drones ...");
        List<Drone> toBeSaved = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            var capacity = DroneBatteryCapacity.builder()
                    .currentCapacity(random.nextInt(100))
                    .build();
            Drone d = Drone.builder()
                    .droneId(UUID.randomUUID())
                    .number(DRONE_PREFIX + i)
                    .model(DroneModelType.LIGHT_WEIGHT)
                    .state(DroneState.IDLE)
                    .lastStateUpdate(ZonedDateTime.now())
                    .weightLimit(500)
                    .batteryCapacity(capacity)
                    .build();
            capacity.setDrone(d);
            toBeSaved.add(d);
        }
        droneRepository.saveAll(toBeSaved);
        log.info("Drones generated successfully");
    }

    @Override
    @Transactional
    public Page<DroneOut> getDronesPage(Pageable pageable) {
        log.info("Fetching drones page {}", pageable);
        return droneRepository.findAll(pageable).map(this::toDroneOut);
    }

    @Override
    @Transactional
    public Page<DroneOut> getDronesAvailableForCargoLoading(Pageable pageable) {
        log.info("Fetching available drones page {}", pageable);

        var capacitiesList = capacityRepository.findAllByCurrentCapacityGreaterThan(BATTERY_CAPACITY_LOW_VALUE);
        var dronesAvailable = capacitiesList.stream()
                .map(DroneBatteryCapacity::getDrone)
                .filter(d -> d.getState() == DroneState.IDLE)
                .map(this::toDroneOut)
                .toList();

        return new PageImpl<>(dronesAvailable,
                pageable,
                dronesAvailable.size());
    }

    @Override
    @Transactional
    public DroneOut getDroneById(UUID droneId) {
        log.info("Fetching drone by id: {}", droneId);
        return droneRepository.findByDroneId(droneId)
                .map(this::toDroneOut)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    @Transactional
    public DroneOut register(DroneIn droneIn) {
        log.info("Registering new drone ...");
        if (droneRepository.existsByNumber(droneIn.getSerialNumber())) {
            throw new IllegalArgumentException("Already registered by number " + droneIn.getSerialNumber());
        }

        DroneBatteryCapacity capacity = DroneBatteryCapacity.builder()
                .currentCapacity(droneIn.getBatteryCapacity())
                .lastChargeDate(null)
                .build();

        Drone toBeSaved = Drone.builder()
                .droneId(UUID.randomUUID())
                .number(droneIn.getSerialNumber())
                .model(droneIn.getModel())
                .weightLimit(droneIn.getLimit())
                .state(DroneState.IDLE)
                .lastStateUpdate(ZonedDateTime.now())
                .batteryCapacity(capacity)
                .build();
        capacity.setDrone(toBeSaved);

        var saved = droneRepository.save(toBeSaved);
        log.info("Drone registered: {}", saved);
        return toDroneOut(saved);
    }

    @Override
    @Transactional
    public void unregister(UUID droneId) {
        log.info("Unregistering drone: {}", droneId);
        droneRepository.deleteByDroneId(droneId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public DroneOut loadCargo(UUID droneId, List<CargoIn> cargo) {
        log.info("Loading drone {} with cargo {}", droneId, cargo);
        var drone = droneRepository.findByDroneId(droneId).orElseThrow(ResourceNotFoundException::new);
        if (!droneCanBeLoaded(drone, cargo)) {
            throw new DroneException("Drone can not be loaded");
        }
        drone.setState(DroneState.LOADING);
        drone.setLastStateUpdate(ZonedDateTime.now());
        var updated = droneRepository.saveAndFlush(drone);
        for (CargoIn cargoIn : cargo) {
            loadNext(updated, cargoIn);
        }
        updated.setState(DroneState.LOADED);
        updated.setLastStateUpdate(ZonedDateTime.now());
        var saved = droneRepository.saveAndFlush(updated);
        log.info("Drone {} loaded successfully", droneId);
        return toDroneOut(saved);
    }

    @Override
    @Transactional
    public DroneOut unloadCargo(UUID droneId) {
        log.info("Unloading drone {}", droneId);
        var drone = droneRepository.findByDroneId(droneId).orElseThrow(ResourceNotFoundException::new);
        drone.setCargoList(List.of());
        drone.setState(DroneState.IDLE);
        drone.setLastStateUpdate(ZonedDateTime.now());
        var saved = droneRepository.save(drone);
        log.info("Drone {} unloaded successfully", droneId);
        return toDroneOut(saved);
    }

    @Override
    @Transactional
    public DroneOut sendDrone(UUID droneId) {
        log.info("Sending drone ...");
        var drone = droneRepository.findByDroneId(droneId).orElseThrow(ResourceNotFoundException::new);
        if (!droneCanBeSent(drone)) {
            throw new DroneException("Drone can not be sent");
        }
        drone.setState(DroneState.DELIVERING);
        drone.setLastStateUpdate(ZonedDateTime.now());
        drone.getCargoList().forEach(c -> c.setState(CargoState.DELIVERING));
        var sent = droneRepository.save(drone);
        log.info("Drone has been sent successfully");
        return toDroneOut(sent);
    }

    private DroneOut toDroneOut(Drone drone) {
        Map<CargoOut, Integer> cargoOut = new HashMap<>();
        var cargoList = drone.getCargoList();
        if (cargoList != null && !cargoList.isEmpty()) {
            for (Cargo c : cargoList) {
                cargoOut.put(toCargoOut(c), c.getCount());
            }
        }
        return DroneOut.builder()
                .id(drone.getDroneId())
                .model(drone.getModel())
                .serialNumber(drone.getNumber())
                .state(drone.getState())
                .limit(drone.getWeightLimit())
                .batteryCapacity(drone.getBatteryCapacity().getCurrentCapacity())
                .cargo(cargoOut)
                .build();
    }

    private Cargo toCargo(CargoIn cargoIn, Drone drone) {
        var mayBeCargo = cargoRepository.findByDroneAndNameAndCode(drone, cargoIn.getName(), cargoIn.getCode());
        return Cargo.builder()
                .cargoId(UUID.randomUUID())
                .name(cargoIn.getName())
                .type(cargoIn.getType())
                .image(cargoIn.getImage())
                .code(cargoIn.getCode())
                .state(CargoState.LOADED)
                .weight(cargoIn.getWeight())
                .count(mayBeCargo.map(cargo -> cargo.getCount() + 1).orElse(1))
                .build();
    }

    private CargoOut toCargoOut(Cargo cargo) {
        return CargoOut.builder()
                .id(cargo.getCargoId())
                .name(cargo.getName())
                .code(cargo.getCode())
                .type(cargo.getType())
                .weight(cargo.getWeight())
                .image(cargo.getImage())
                .build();
    }

    private void loadNext(Drone drone, CargoIn toBeLoadedNext) {
        for (Cargo cargo : drone.getCargoList()) {
            if (cargo.getCode().equals(toBeLoadedNext.getCode())) {
                cargo.setCount(cargo.getCount() + 1);
                cargo.setState(CargoState.LOADED);
                return;
            }
        }
        drone.addCargo(toCargo(toBeLoadedNext, drone));
    }

    private boolean droneCanBeLoaded(Drone drone, List<CargoIn> cargoIn) {
        return DroneState.IDLE == drone.getState() &&
                drone.getBatteryCapacity().getCurrentCapacity() > BATTERY_CAPACITY_LOW_VALUE &&
                drone.getCargoList().isEmpty() &&
                sumWeightsForCargoIn(cargoIn) <= drone.getWeightLimit();
    }

    private boolean droneCanBeSent(Drone drone) {
        return DroneState.LOADED == drone.getState() &&
                drone.getBatteryCapacity().getCurrentCapacity() > BATTERY_CAPACITY_LOW_VALUE &&
                !drone.getCargoList().isEmpty();
    }

    private Float sumWeightsForCargoIn(List<CargoIn> cargoList) {
        return cargoList.stream()
                .map(CargoIn::getWeight)
                .reduce(Float::sum)
                .orElseThrow();
    }

}

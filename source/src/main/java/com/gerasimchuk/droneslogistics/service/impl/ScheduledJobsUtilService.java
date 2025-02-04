package com.gerasimchuk.droneslogistics.service.impl;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoState;
import com.gerasimchuk.droneslogistics.common.enumeration.DroneState;
import com.gerasimchuk.droneslogistics.domain.entity.*;
import com.gerasimchuk.droneslogistics.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobsUtilService {

    private static final int BATCH_SIZE = 50;
    private static final int DELIVERY_TIME = 5; // seconds
    private static final int DELIVERY_TO_RETURNING_TIME = 5; // seconds
    private static final int RETURNING_TO_IDLE_TIME = 5; // seconds
    private static final int MAX_LOADING_TIME = 15; // seconds
    private static final String LOG_MESSAGE_START = "Start updating drones in {} state ...";
    private static final String LOG_MESSAGE_END = "End updating drones in {} state ...";
    private final DroneBatteryCapacityRepository capacityRepository;
    private final DroneRepository droneRepository;
    private final BatteryAuditRepository batteryAuditRepository;
    private final CargoRepository cargoRepository;
    private final CargoAuditRepository cargoAuditRepository;


    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void performBatteryAudit() {
        log.info("Start battery audit ...");
        performBatchUpdateGenericOperation(
                allDronesBatteryRequester(),
                writeBatteryCapacityAuditEntry()
        );
        log.info("End battery audit ...");
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void decreaseDroneBattery() {
        log.info("Start battery decreasing ...");
        performBatchUpdateGenericOperation(
                allDronesRequester(),
                updateDroneBattery()
        );
        log.info("End battery decreasing ...");
    }

    @Transactional
    @Scheduled(fixedDelay = 30000)
    public void chargeDrone(){
        log.info("Start battery charging ...");
        performBatchUpdateGenericOperation(
                deadBatteryDrones(),
                chargeDroneBattery()
        );
        log.info("End battery charging ...");
    }

    @Transactional
    @Scheduled(fixedDelay = 20000)
    public void checkIfLoadingTooLongAndRelease() {
        log.info("Start check for long loading ...");
        performBatchUpdateGenericOperation(
                dronesByStateRequester(DroneState.LOADING),
                releaseDroneOfTooLongLoading()
        );
        log.info("End check for long loading ...");
    }

    /**
     * Here for the sake of simplicity we just put drones in 'DELIVERED' state
     * <p>
     * In real world scenario we should take care of cases when drone battery is dead
     * right after delivery so drone should be charged somewhere (and put into 'CHARGING' state,
     * and after successful charging in might be put at 'RETURNING' state)
     */
    @Transactional
    @Scheduled(fixedDelay = 10000)
    public void findDronesInDeliveringStateAndSetThemAsDelivered() {
        log.info(LOG_MESSAGE_START, DroneState.DELIVERING);
        performBatchUpdateGenericOperation(
                dronesByStateRequester(DroneState.DELIVERING),
                updateToDelivered()
        );
        log.info(LOG_MESSAGE_END, DroneState.DELIVERING);
    }


    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void updateFromDeliveredToReturning() {
        log.info(LOG_MESSAGE_START, DroneState.DELIVERED);
        performBatchUpdateGenericOperation(
                dronesByStateRequester(DroneState.DELIVERED),
                updateToReturning()
        );
        log.info(LOG_MESSAGE_END, DroneState.DELIVERED);
    }

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void updateFromReturningToIdle() {
        log.info(LOG_MESSAGE_START, DroneState.RETURNING);
        performBatchUpdateGenericOperation(
                dronesByStateRequester(DroneState.RETURNING),
                updateToIdle()
        );
        log.info(LOG_MESSAGE_END, DroneState.RETURNING);
    }

    private <T> void performBatchUpdateGenericOperation(IntFunction<Page<T>> itemsPageRequester,
                                                        Consumer<T> itemsUpdater) {
        int currentPage = 0;
        boolean hasNext = true;
        while (hasNext) {
            var page = itemsPageRequester.apply(currentPage);
            var itemsList = page.getContent();
            for (T d : itemsList) {
                itemsUpdater.accept(d);
            }
            hasNext = page.hasNext();
            currentPage++;
        }
    }

    private IntFunction<Page<Drone>> dronesByStateRequester(DroneState state) {
        return currentPageNum -> droneRepository.findAllByState(state, PageRequest.of(
                currentPageNum,
                BATCH_SIZE
        ));
    }

    private IntFunction<Page<Drone>> allDronesRequester() {
        return currentPageNum -> droneRepository.findAll(PageRequest.of(
                currentPageNum,
                BATCH_SIZE
        ));
    }

    private IntFunction<Page<DroneBatteryCapacity>> deadBatteryDrones() {
        return currentPageNum -> capacityRepository.findAllByCurrentCapacityEquals(0, PageRequest.of(
                currentPageNum,
                BATCH_SIZE
        ));
    }

    private IntFunction<Page<DroneBatteryCapacity>> allDronesBatteryRequester() {
        return currentPageNum -> capacityRepository.findAll(PageRequest.of(
                currentPageNum,
                BATCH_SIZE
        ));
    }

    private Consumer<Drone> updateToDelivered() {
        return d -> {
            if (deliveryTimeHasPassed(d)) {
                d.setState(DroneState.DELIVERED);
                d.setLastStateUpdate(ZonedDateTime.now());
                var cargoList = d.getCargoList();

                List<CargoAudit> cargoAuditEntries = new ArrayList<>(cargoList.size());

                for (Cargo c : cargoList) {
                    cargoAuditEntries.add(toCargoAuditEntry(c, d.getDroneId()));
                }
                d.removeAllCargo();
                cargoRepository.deleteAll(cargoList);
                cargoAuditRepository.saveAll(cargoAuditEntries);
                droneRepository.save(d);
            }
        };
    }

    private Consumer<Drone> updateToReturning() {
        return d -> {
            if (deliveryToReturningTimeHasPassed(d)) {
                d.setState(DroneState.RETURNING);
                d.setLastStateUpdate(ZonedDateTime.now());
                droneRepository.save(d);
            }
        };
    }

    private Consumer<Drone> updateToIdle() {
        return d -> {
            if (returningToIdleTimeHasPassed(d)) {
                d.setState(DroneState.IDLE);
                d.setLastStateUpdate(ZonedDateTime.now());
                droneRepository.save(d);
            }
        };
    }

    private Consumer<Drone> updateDroneBattery() {
        return d -> {
            int currentCapacity = d.getBatteryCapacity().getCurrentCapacity();
            if (currentCapacity > 0) {
                d.getBatteryCapacity().setCurrentCapacity(currentCapacity - 1);
                droneRepository.save(d);
            }
        };
    }

    private Consumer<Drone> releaseDroneOfTooLongLoading() {
        return d -> {
            if (droneIsBeingLoadedTooLong(d)) {
                d.setState(DroneState.IDLE);
                d.setLastStateUpdate(ZonedDateTime.now());
                d.getCargoList().forEach(
                        c -> {
                            cargoAuditRepository.save(toCargoAuditEntry(c, d.getDroneId()));
                            cargoRepository.delete(c);
                        }
                );
                d.removeAllCargo();
                droneRepository.save(d);
            }
        };
    }

    private Consumer<DroneBatteryCapacity> writeBatteryCapacityAuditEntry() {
        return c -> batteryAuditRepository.save(toCapacityAuditEntry(c));
    }

    private Consumer<DroneBatteryCapacity> chargeDroneBattery() {
        return c -> {
            c.setCurrentCapacity(100);
            capacityRepository.save(c);
        };
    }

    private boolean deliveryTimeHasPassed(Drone drone) {
        return drone.getLastStateUpdate().plusSeconds(DELIVERY_TIME).isBefore(ZonedDateTime.now());
    }

    private boolean deliveryToReturningTimeHasPassed(Drone drone) {
        return drone.getLastStateUpdate().plusSeconds(DELIVERY_TO_RETURNING_TIME).isBefore(ZonedDateTime.now());
    }

    private boolean returningToIdleTimeHasPassed(Drone drone) {
        return drone.getLastStateUpdate().plusSeconds(RETURNING_TO_IDLE_TIME).isBefore(ZonedDateTime.now());
    }

    private boolean droneIsBeingLoadedTooLong(Drone drone) {
        return drone.getLastStateUpdate().plusSeconds(MAX_LOADING_TIME).isBefore(ZonedDateTime.now());
    }

    private DroneBatteryCapacityAudit toCapacityAuditEntry(DroneBatteryCapacity capacity) {
        return DroneBatteryCapacityAudit.builder()
                .droneId(capacity.getDrone().getDroneId())
                .checkDateTime(ZonedDateTime.now())
                .batteryCapacity(capacity.getCurrentCapacity())
                .build();
    }

    private CargoAudit toCargoAuditEntry(Cargo c, UUID droneId){
        return CargoAudit.builder()
                .cargoId(c.getCargoId())
                .type(c.getType())
                .code(c.getCode())
                .name(c.getName())
                .weight(c.getWeight())
                .state(CargoState.DELIVERED)
                .count(c.getCount())
                .image(c.getImage())
                .droneId(droneId)
                .build();
    }
}

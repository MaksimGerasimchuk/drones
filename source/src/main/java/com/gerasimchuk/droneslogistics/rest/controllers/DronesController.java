package com.gerasimchuk.droneslogistics.rest.controllers;

import com.gerasimchuk.droneslogistics.rest.model_in.AddCargoRequest;
import com.gerasimchuk.droneslogistics.rest.model_in.CargoItem;
import com.gerasimchuk.droneslogistics.rest.model_in.DroneRegistrationRequest;
import com.gerasimchuk.droneslogistics.rest.model_in.PageRequest;
import com.gerasimchuk.droneslogistics.rest.model_out.CargoResponse;
import com.gerasimchuk.droneslogistics.rest.model_out.DroneResponse;
import com.gerasimchuk.droneslogistics.service.api.DroneService;
import com.gerasimchuk.droneslogistics.service.api.in.CargoIn;
import com.gerasimchuk.droneslogistics.service.api.in.DroneIn;
import com.gerasimchuk.droneslogistics.service.api.out.CargoOut;
import com.gerasimchuk.droneslogistics.service.api.out.DroneOut;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1.0/drones")
@RequiredArgsConstructor
@Tag(name = "Drones management", description = "Drones management operations")
public class DronesController {

    private final DroneService droneService;

    @GetMapping
    @Operation(description = "Get drones page")
    Page<DroneResponse> getDrones(@Valid PageRequest request) {
        return droneService.getDronesPage(org.springframework.data.domain.PageRequest.of(
                request.getPageNumber(), request.getPageSize())
        )
                .map(this::toDroneResponse);

    }

    @GetMapping("/available")
    @Operation(description = "Get available for loading drones page")
    Page<DroneResponse> getDronesAvailableForCargoLoading(@Valid PageRequest request) {
        return droneService.getDronesAvailableForCargoLoading(org.springframework.data.domain.PageRequest.of(
                request.getPageNumber(), request.getPageSize())
        )
                .map(this::toDroneResponse);
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(description = "Register new drone")
    DroneResponse register(@RequestBody @Valid DroneRegistrationRequest request) {
        var registered = droneService.register(toDroneIn(request));
        return toDroneResponse(registered);
    }

    @DeleteMapping("/{droneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(description = "Unregister drone by id")
    void unregister(@PathVariable UUID droneId) {
        droneService.unregister(droneId);
    }

    @PutMapping("/{droneId}/cargo")
    @Operation(description = "Load cargo")
    DroneResponse loadCargo(@PathVariable UUID droneId,
                            @RequestBody @Valid AddCargoRequest request) {
        var loadedDrone = droneService.loadCargo(droneId, Arrays.stream(request.getCargo()).map(this::toCargoIn).toList());
        return toDroneResponse(loadedDrone);
    }

    @PutMapping("/{droneId}/cargo/unload")
    @Operation(description = "Unload cargo")
    DroneResponse unloadCargo(@PathVariable UUID droneId) {
        var unloadedDrone = droneService.unloadCargo(droneId);
        return toDroneResponse(unloadedDrone);
    }

    @PostMapping("/{droneId}/delivery")
    @Operation(description = "Initiate drone delivery")
    DroneResponse initiateDelivery(@PathVariable UUID droneId) {
        var sent = droneService.sendDrone(droneId);
        return toDroneResponse(sent);
    }

    private DroneResponse toDroneResponse(DroneOut drone) {
        var cargoMap = drone.getCargo();
        Map<CargoResponse, Integer> cargoResponseMap = new HashMap<>(cargoMap.size());
        for (Map.Entry<CargoOut, Integer> entry : cargoMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            var cargoResponse = CargoResponse.builder()
                    .code(key.getCode())
                    .image(key.getImage())
                    .name(key.getName())
                    .type(key.getType())
                    .weight(key.getWeight())
                    .build();
            cargoResponseMap.put(cargoResponse, value);
        }
        return DroneResponse.builder()
                .id(drone.getId())
                .model(drone.getModel())
                .serialNumber(drone.getSerialNumber())
                .state(drone.getState())
                .limit(drone.getLimit())
                .batteryCapacity(drone.getBatteryCapacity())
                .cargo(cargoResponseMap)
                .build();
    }

    private DroneIn toDroneIn(DroneRegistrationRequest request) {
        return DroneIn.builder()
                .serialNumber(request.getNumber())
                .model(request.getModel())
                .limit(request.getWeightLimit())
                .batteryCapacity(request.getBatteryCapacity())
                .build();
    }

    private CargoIn toCargoIn(CargoItem item) {
        return CargoIn.builder()
                .name(item.getName())
                .code(item.getCode())
                .type(item.getType())
                .weight(item.getWeight())
                .image(item.getImage())
                .build();
    }

}

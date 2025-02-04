package com.gerasimchuk.droneslogistics.service.api;

import com.gerasimchuk.droneslogistics.service.api.in.CargoIn;
import com.gerasimchuk.droneslogistics.service.api.in.DroneIn;
import com.gerasimchuk.droneslogistics.service.api.out.DroneOut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DroneService {
    Page<DroneOut> getDronesPage(Pageable pageable);

    Page<DroneOut> getDronesAvailableForCargoLoading(Pageable pageable);

    DroneOut getDroneById(UUID droneId);

    DroneOut register(DroneIn droneIn);

    void unregister(UUID droneId);

    DroneOut loadCargo(UUID droneId, List<CargoIn> cargo);

    DroneOut unloadCargo(UUID droneId);

    DroneOut sendDrone(UUID droneId);

}

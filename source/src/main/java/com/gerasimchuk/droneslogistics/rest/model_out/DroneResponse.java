package com.gerasimchuk.droneslogistics.rest.model_out;

import com.gerasimchuk.droneslogistics.common.enumeration.DroneModelType;
import com.gerasimchuk.droneslogistics.common.enumeration.DroneState;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class DroneResponse {
    private final UUID id;
    private final String serialNumber;
    private final DroneModelType model;
    private final Integer limit;
    private final Integer batteryCapacity;
    private final DroneState state;
    private final Map<CargoResponse, Integer> cargo;
}

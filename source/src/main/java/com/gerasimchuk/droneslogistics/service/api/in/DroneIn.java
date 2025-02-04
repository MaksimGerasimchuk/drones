package com.gerasimchuk.droneslogistics.service.api.in;

import com.gerasimchuk.droneslogistics.common.enumeration.DroneModelType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DroneIn {
    private final String serialNumber;
    private final DroneModelType model;
    private final Integer limit;
    private final Integer batteryCapacity;
}

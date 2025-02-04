package com.gerasimchuk.droneslogistics.rest.model_in;

import com.gerasimchuk.droneslogistics.common.enumeration.DroneModelType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DroneRegistrationRequest {

    @NotEmpty
    private String number;

    private DroneModelType model;

    @Positive
    private Integer weightLimit;

    @Min(0)
    @Max(100)
    private Integer batteryCapacity;

}

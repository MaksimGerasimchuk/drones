package com.gerasimchuk.droneslogistics.rest.model_in;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CargoItem {
    @NotNull
    private CargoType type;

    @NotEmpty
    private String name;

    @Positive
    private Float weight;

    @NotEmpty
    private String code;

    @NotEmpty
    private String image;
}

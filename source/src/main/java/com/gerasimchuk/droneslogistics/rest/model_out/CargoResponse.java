package com.gerasimchuk.droneslogistics.rest.model_out;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CargoResponse {
    private final CargoType type;
    private final String name;
    private final Float weight;
    private final String code;
    private final String image;
}

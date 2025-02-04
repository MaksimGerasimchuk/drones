package com.gerasimchuk.droneslogistics.service.api.in;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CargoIn {
    private final CargoType type;
    private final String name;
    private final Float weight;
    private final String code;
    private final String image;
}

package com.gerasimchuk.droneslogistics.service.api.out;

import com.gerasimchuk.droneslogistics.common.enumeration.CargoType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CargoOut {
    private final UUID id;
    private final CargoType type;
    private final String name;
    private final Float weight;
    private final String code;
    private final String image;
}

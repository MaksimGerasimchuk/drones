package com.gerasimchuk.droneslogistics.rest.model_in;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class AddCargoRequest {

    @Valid
    private CargoItem[] cargo;

}

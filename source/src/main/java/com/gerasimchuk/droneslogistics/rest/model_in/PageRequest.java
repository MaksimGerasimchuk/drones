package com.gerasimchuk.droneslogistics.rest.model_in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class PageRequest {

    @PositiveOrZero
    private Integer pageNumber;

    @Positive
    private Integer pageSize;
}

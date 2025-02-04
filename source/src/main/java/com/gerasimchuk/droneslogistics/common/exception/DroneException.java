package com.gerasimchuk.droneslogistics.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class DroneException extends RuntimeException{

    public DroneException(String message) {
        super(message);
    }
}


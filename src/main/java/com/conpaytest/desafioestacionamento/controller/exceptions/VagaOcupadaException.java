package com.conpaytest.desafioestacionamento.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class VagaOcupadaException extends RuntimeException{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public VagaOcupadaException(String message) {
        super(message);
    }
}
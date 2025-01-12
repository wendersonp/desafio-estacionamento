package com.conpaytest.desafioestacionamento.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//Caso o dado procurado nas queries nao seja encontrado
@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotFoundException extends RuntimeException{
   
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
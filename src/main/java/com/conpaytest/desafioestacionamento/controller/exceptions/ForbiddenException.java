package com.conpaytest.desafioestacionamento.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//Qualquer operação incorreta retorna esta excecao
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException{
    
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(message);
    }
}
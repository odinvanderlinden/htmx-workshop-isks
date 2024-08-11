package com.infosupport.stateoftheweb2024.htmxexample.models;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    public NotFoundException(String entity, int id) {
        super(String.format("%s with id %s not found", entity, id));
    }
}

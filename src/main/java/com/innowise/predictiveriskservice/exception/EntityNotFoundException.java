package com.innowise.predictiveriskservice.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class EntityNotFoundException extends AbstractException {
    public EntityNotFoundException(String entityName, UUID id) {
        super(String.format("%s with ID '%s' was not found", entityName, id), HttpStatus.NOT_FOUND);
    }
}

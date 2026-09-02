package com.innowise.predictiveriskservice.exception;

import org.springframework.http.HttpStatus;

public class RelationshipCreationException extends AbstractException {
    public RelationshipCreationException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

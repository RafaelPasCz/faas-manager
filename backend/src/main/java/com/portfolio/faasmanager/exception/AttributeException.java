package com.portfolio.faasmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AttributeException extends RuntimeException {
    public AttributeException(String message) {
        super(message);
    }
}
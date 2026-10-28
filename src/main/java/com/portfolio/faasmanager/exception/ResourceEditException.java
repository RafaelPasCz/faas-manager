package com.portfolio.faasmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ResourceEditException extends RuntimeException {
    public ResourceEditException(String message) {
        super(message);
    }
}
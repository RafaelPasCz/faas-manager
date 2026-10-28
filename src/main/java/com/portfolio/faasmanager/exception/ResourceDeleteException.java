package com.portfolio.faasmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ResourceDeleteException extends RuntimeException {
    public ResourceDeleteException(String message) {
        super(message);
    }
}
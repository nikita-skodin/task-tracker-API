package com.skodin.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidToken extends RuntimeException {
    public InvalidToken(String message) {
        super(message);
    }
}

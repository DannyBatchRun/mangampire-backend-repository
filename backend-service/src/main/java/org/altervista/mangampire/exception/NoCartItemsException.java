package org.altervista.mangampire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoCartItemsException extends RuntimeException {
    public NoCartItemsException() {
        super("No items in the shopping cart.");
    }
    public NoCartItemsException(String message) {
        super(message);
    }
}


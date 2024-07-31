package org.altervista.mangampire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(value = HttpStatus.PAYMENT_REQUIRED)
public class InsufficientCreditException extends RuntimeException {
    public InsufficientCreditException() {
        super("Has no sufficient credit on card.");
    }

    public InsufficientCreditException(String message) {
        super(message);
    }
}

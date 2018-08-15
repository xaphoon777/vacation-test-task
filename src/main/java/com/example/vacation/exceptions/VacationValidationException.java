package com.example.vacation.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class VacationValidationException extends RuntimeException {
    public VacationValidationException() {
        super("Vacation not valid. Vacation end should not be earlier than begin");
    }
}

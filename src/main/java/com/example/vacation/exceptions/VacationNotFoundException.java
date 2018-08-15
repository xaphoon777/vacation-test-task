package com.example.vacation.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VacationNotFoundException extends RuntimeException {
    public VacationNotFoundException(Long id) {
        super("Vacation with id does not exist: " + id.toString());
    }
}

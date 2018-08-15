package com.example.vacation.exceptions;

import com.example.vacation.model.Vacation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class VacationOverlapException extends RuntimeException {
    public VacationOverlapException(Vacation vacation) {
        super("Vacation not allowed because overlap with: " + vacation);
    }
}

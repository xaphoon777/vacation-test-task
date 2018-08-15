package com.example.vacation.controller;

import com.example.vacation.dao.VacationRepository;
import com.example.vacation.exceptions.VacationNotFoundException;
import com.example.vacation.exceptions.VacationOverlapException;
import com.example.vacation.exceptions.VacationValidationException;
import com.example.vacation.model.Vacation;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Objects;

@RestController
@RequestMapping("/vacation")
@Api(value = "Vacation controller", tags = "vacation")
public class VacationRestController {
    private final VacationRepository vacationRepository;

    @Autowired
    public VacationRestController(VacationRepository vacationRepository) {
        this.vacationRepository = vacationRepository;
    }

    @GetMapping
    Collection<Vacation> readAll() {
        return this.vacationRepository.findAll();
    }

    @GetMapping("/{id}")
    Vacation read(@PathVariable Long id) {
        return this.vacationRepository.findById(id).orElseThrow(() -> new VacationNotFoundException(id));
    }

    @PostMapping
    Vacation add(@RequestBody Vacation vacation) {
        this.validateVacation(vacation);
        return this.vacationRepository.save(vacation);
    }

    @PutMapping("/{id}")
    ResponseEntity<?> update(@PathVariable Long id, @RequestBody Vacation inputVacation) {

        if (!this.vacationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        inputVacation.setId(id);
        this.validateVacation(inputVacation);
        Vacation vacation = this.vacationRepository.save(inputVacation);
        return ResponseEntity.ok(vacation);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            this.vacationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new VacationNotFoundException(id);
        }
    }

    private void validateVacation(Vacation vacation) {

        if (vacation.getEnd().before(vacation.getStart())) {
            throw new VacationValidationException();
        }

        Collection<Vacation> overlappingVacations = vacationRepository.
                findByStartLessThanEqualAndEndGreaterThanEqual(vacation.getEnd(), vacation.getStart());
        overlappingVacations.forEach(overlappingVacation -> {
            if (!Objects.equals(overlappingVacation.getId(), vacation.getId())) {
                throw new VacationOverlapException(overlappingVacation);
            }
        });
    }
}

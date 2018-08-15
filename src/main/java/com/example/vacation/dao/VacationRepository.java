package com.example.vacation.dao;

import com.example.vacation.model.Vacation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Date;

public interface VacationRepository extends JpaRepository<Vacation, Long> {
    // Overlapping dates
    Collection<Vacation> findByStartLessThanEqualAndEndGreaterThanEqual(Date end, Date start);
}
package com.budgettravel.repo;

import com.budgettravel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByTravelerIdOrderByIdDesc(Long travelerId);
}

package com.budgettravel.repo;

import com.budgettravel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public interface TravelerRepository extends JpaRepository<Traveler, Long> {
    Optional<Traveler> findByEmail(String email);
}

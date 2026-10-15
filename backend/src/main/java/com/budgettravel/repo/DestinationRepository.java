package com.budgettravel.repo;

import com.budgettravel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    Optional<Destination> findByNameIgnoreCase(String name);
}

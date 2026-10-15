package com.budgettravel.repo;

import com.budgettravel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public interface TravelPreferenceRepository extends JpaRepository<TravelPreference, Long> {
    Optional<TravelPreference> findFirstByTravelerId(Long travelerId);
}

package com.budgettravel.repo;

import com.budgettravel.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    List<Itinerary> findByTripIdOrderByDayNumber(Long tripId);
    @Transactional void deleteByTripId(Long tripId);
}

package com.budgettravel.controller;

import com.budgettravel.model.*;
import com.budgettravel.repo.*;
import com.budgettravel.service.BudgetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TripController {
    private final BudgetService service;
    private final TripRepository trips;
    private final DestinationRepository destinations;

    public TripController(BudgetService s, TripRepository t, DestinationRepository d) {
        this.service = s; this.trips = t; this.destinations = d;
    }

    @GetMapping("/destinations")
    public List<Destination> destinations() { return destinations.findAll(); }

    @PostMapping("/trips")
    public Trip create(@RequestBody Trip t, @AuthenticationPrincipal Long userId) { return service.createTrip(t, userId); }

    @GetMapping("/trips")
    public List<Trip> list(@AuthenticationPrincipal Long userId) { return trips.findByTravelerIdOrderByIdDesc(userId); }

    @GetMapping("/trips/{id}/budget")
    public Map<String, Object> budget(@PathVariable Long id, @AuthenticationPrincipal Long userId) { return service.summary(id, userId); }

    @PostMapping("/itinerary/generate")
    public List<Itinerary> generate(@RequestBody Map<String, Long> b, @AuthenticationPrincipal Long userId) {
        return service.generateItinerary(b.get("tripId"), userId);
    }

    @GetMapping("/itinerary/{tripId}")
    public List<Itinerary> itinerary(@PathVariable Long tripId, @AuthenticationPrincipal Long userId) {
        return service.itinerary(tripId, userId);
    }

    @PostMapping("/recommendations")
    public Map<String, Object> recommend(@RequestBody Map<String, Long> b, @AuthenticationPrincipal Long userId) {
        return service.recommend(b.get("tripId"), userId);
    }

    @PutMapping("/expenses/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Map<String, Double> b, @AuthenticationPrincipal Long userId) {
        return service.updateExpense(id, b.getOrDefault("actualAmount", 0.0), userId);
    }
}

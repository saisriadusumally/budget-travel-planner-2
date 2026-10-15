package com.budgettravel.service;

import com.budgettravel.model.*;
import com.budgettravel.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.*;

/** Cost engine: estimation, budget analysis, itinerary and recommendations. */
@Service
public class BudgetService {
    private final TripRepository trips;
    private final DestinationRepository destinations;
    private final ExpenseRepository expenses;
    private final ItineraryRepository itineraries;
    private final TravelPreferenceRepository preferences;

    public BudgetService(TripRepository t, DestinationRepository d, ExpenseRepository e,
                         ItineraryRepository i, TravelPreferenceRepository p) {
        this.trips = t; this.destinations = d; this.expenses = e; this.itineraries = i; this.preferences = p;
    }

    public static int days(Trip t) {
        return (int) ChronoUnit.DAYS.between(t.startDate, t.endDate) + 1;
    }

    /** Estimated Total = Transport + Accommodation + Food + Activities + Other */
    public LinkedHashMap<String, Double> estimate(Destination d, int days, int people) {
        int rooms = (int) Math.ceil(people / 2.0);
        int nights = Math.max(days - 1, 1);
        double transport = d.transportCost * people;
        double stay = d.stayPerNight * nights * rooms;
        double food = d.foodPerDay * days * people;
        double activities = d.activityPerDay * days * people;
        double other = Math.round((transport + stay + food + activities) * 0.05);
        LinkedHashMap<String, Double> m = new LinkedHashMap<>();
        m.put("Transport", transport);
        m.put("Accommodation", stay);
        m.put("Food", food);
        m.put("Activities", activities);
        m.put("Other", other);
        return m;
    }

    public double sum(Map<String, Double> m) {
        return m.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    /** Loads a trip only if it belongs to the logged-in traveler. */
    public Trip get(Long id, Long userId) {
        return trips.findById(id)
            .filter(t -> t.travelerId != null && t.travelerId.equals(userId))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    private Destination dest(String name) {
        return destinations.findByNameIgnoreCase(name)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown destination: " + name));
    }

    @Transactional
    public Trip createTrip(Trip t, Long userId) {
        t.id = null;
        t.travelerId = userId;
        if (t.startDate == null || t.endDate == null || t.endDate.isBefore(t.startDate))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be on or after start date");
        if (t.travelers < 1) t.travelers = 1;
        Destination d = dest(t.destination);
        t.destination = d.name;
        Trip saved = trips.save(t);
        estimate(d, days(saved), saved.travelers).forEach((cat, amt) -> {
            Expense e = new Expense();
            e.tripId = saved.id; e.category = cat; e.estimatedAmount = amt; e.actualAmount = 0;
            expenses.save(e);
        });
        return saved;
    }

    public Map<String, Object> summary(Long tripId, Long userId) {
        Trip t = get(tripId, userId);
        List<Expense> list = expenses.findByTripId(tripId);
        double est = list.stream().mapToDouble(e -> e.estimatedAmount).sum();
        double act = list.stream().mapToDouble(e -> e.actualAmount).sum();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("trip", t);
        m.put("days", days(t));
        m.put("totalBudget", t.totalBudget);
        m.put("estimatedTotal", est);
        m.put("remaining", t.totalBudget - est);          // Budget Balance
        m.put("actualTotal", act);
        m.put("remainingAfterActual", t.totalBudget - act);
        m.put("overBudget", est > t.totalBudget);
        m.put("expenses", list);
        return m;
    }

    @Transactional
    public List<Itinerary> generateItinerary(Long tripId, Long userId) {
        Trip t = get(tripId, userId);
        Destination d = dest(t.destination);
        int n = days(t);
        String[] spots = d.highlights.split(",");
        double perDay = d.activityPerDay * t.travelers;
        itineraries.deleteByTripId(tripId);
        List<Itinerary> out = new ArrayList<>();
        for (int day = 1; day <= n; day++) {
            String act;
            double cost = perDay;
            if (day == 1 && n > 1) { act = "Arrival & local visit: " + spots[0].trim(); }
            else if (day == n && n > 1) { act = "Leisure & departure"; cost = perDay / 2; }
            else { act = "Sightseeing: " + spots[(day - 1) % spots.length].trim(); }
            Itinerary it = new Itinerary();
            it.tripId = tripId; it.dayNumber = day; it.activity = act; it.cost = cost;
            out.add(itineraries.save(it));
        }
        return out;
    }

    public List<Itinerary> itinerary(Long tripId, Long userId) {
        get(tripId, userId);
        return itineraries.findByTripIdOrderByDayNumber(tripId);
    }

    public Map<String, Object> recommend(Long tripId, Long userId) {
        Trip t = get(tripId, userId);
        Destination current = dest(t.destination);
        int n = days(t);
        double est = sum(estimate(current, n, t.travelers));
        double remaining = t.totalBudget - est;
        List<String> tips = new ArrayList<>();
        List<Map<String, Object>> alternatives = new ArrayList<>();
        List<String> optional = new ArrayList<>();

        if (remaining < 0) {
            tips.add("Choose budget stays such as hostels or homestays; accommodation is usually the largest saving.");
            tips.add("Use local buses, metro or shared transport instead of private cabs.");
            tips.add("Eat at local eateries and prioritise free attractions like beaches, markets and viewpoints.");
            for (Destination d : destinations.findAll()) {
                double cost = sum(estimate(d, n, t.travelers));
                if (!d.name.equalsIgnoreCase(current.name) && cost <= t.totalBudget) {
                    Map<String, Object> a = new LinkedHashMap<>();
                    a.put("destination", d.name); a.put("country", d.country); a.put("estimatedCost", cost);
                    alternatives.add(a);
                }
            }
            alternatives.sort(Comparator.comparingDouble(a -> -(double) a.get("estimatedCost")));
        } else {
            double cheapest = current.activityPerDay;
            tips.add("You have " + Math.round(remaining) + " left. Keep around 10% as an emergency buffer.");
            String[] spots = current.highlights.split(",");
            for (String s : spots) {
                if (remaining >= current.activityPerDay * 0.5) optional.add(s.trim());
            }
            if (remaining < cheapest) optional.clear();
        }
        preferences.findFirstByTravelerId(t.travelerId).ifPresent(p -> {
            if (p.interests != null && !p.interests.isBlank())
                tips.add("Based on your interests (" + p.interests + "), pick activities that match them first.");
        });

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("estimatedTotal", est);
        m.put("remaining", remaining);
        m.put("tips", tips);
        m.put("alternatives", alternatives);
        m.put("optionalActivities", optional);
        return m;
    }

    public Expense updateExpense(Long id, double actual, Long userId) {
        Expense e = expenses.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
        get(e.tripId, userId);
        if (actual < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount cannot be negative");
        e.actualAmount = actual;
        return expenses.save(e);
    }
}

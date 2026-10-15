-- Reference only. Tables are auto-created by Hibernate (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS budget_travel;
USE budget_travel;
-- traveler(id PK, name, email UNIQUE, password_hash, budget_preference)
-- travel_preference(id PK, traveler_id FK, travel_style, interests)
-- trip(id PK, traveler_id FK, destination, start_date, end_date, total_budget, travelers)
-- expense(id PK, trip_id FK, category, estimated_amount, actual_amount)
-- itinerary(id PK, trip_id FK, day_number, activity, cost)
-- destination(id PK, name UNIQUE, country, budget_level, transport_cost, stay_per_night,
--             food_per_day, activity_per_day, highlights)

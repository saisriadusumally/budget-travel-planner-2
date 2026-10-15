package com.budgettravel.model;

import jakarta.persistence.*;

@Entity
public class Itinerary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long tripId;
    public int dayNumber;
    public String activity;
    public double cost;
}

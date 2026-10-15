package com.budgettravel.model;

import jakarta.persistence.*;

@Entity
public class Destination {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(unique = true)
    public String name;
    public String country;
    public String budgetLevel;
    public double transportCost;   // round trip, per person
    public double stayPerNight;    // per room
    public double foodPerDay;      // per person
    public double activityPerDay;  // per person
    @Column(length = 1000)
    public String highlights;      // comma separated
}

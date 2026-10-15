package com.budgettravel.model;

import jakarta.persistence.*;

@Entity
public class TravelPreference {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long travelerId;
    public String travelStyle;
    public String interests;
}

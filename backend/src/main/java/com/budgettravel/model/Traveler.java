package com.budgettravel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Traveler {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    @Column(unique = true)
    public String email;
    @JsonIgnore
    public String passwordHash;
    public String budgetPreference; // LOW / MEDIUM / HIGH
}

package com.budgettravel.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Trip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long travelerId;
    public String destination;
    public LocalDate startDate;
    public LocalDate endDate;
    public double totalBudget;
    public int travelers = 1;
}

package com.budgettravel.model;

import jakarta.persistence.*;

@Entity
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long tripId;
    public String category;
    public double estimatedAmount;
    public double actualAmount;
}

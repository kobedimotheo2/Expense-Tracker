package com.expensetracker;

import java.time.LocalDate;

public class Expense {
    
    private String description;
    private double amount;
    private String category;
    private LocalDate date;

    // Constructor - this runs when we create a new Expense
    public Expense(String description, double amount, String category) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = LocalDate.now(); // automatically set to today
    }

    // Getters - ways to read the values from outside this class
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    // This controls how an Expense looks when printed
    @Override
    public String toString() {
        return date + " | " + category + " | " + description + " | P" + amount;
    }
}
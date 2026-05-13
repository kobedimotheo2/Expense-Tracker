package com.expensetracker;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    // The file where we save expenses
    private static final String FILE_NAME = "expenses.txt";

    // Saves all expenses to the file
    public static void saveExpenses(List<Expense> expenses) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Expense e : expenses) {
                // Each expense is saved as one line, values separated by commas
                writer.write(e.getDate() + "," + e.getDescription() + "," + e.getCategory() + "," + e.getAmount());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving expenses: " + e.getMessage());
        }
    }

    // Loads expenses from the file
    public static List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(FILE_NAME);

        // If file doesn't exist yet, return empty list
        if (!file.exists()) return expenses;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Split each line by comma to get the values back
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String description = parts[1];
                    double amount = Double.parseDouble(parts[3]);
                    String category = parts[2];
                    LocalDate date = LocalDate.parse(parts[0]);

                    Expense e = new Expense(description, amount, category);
                    e.setDate(date); // restore the original date
                    expenses.add(e);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading expenses: " + e.getMessage());
        }

        return expenses;
    }
}
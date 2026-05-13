package com.expensetracker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private Label totalLabel = new Label("Total: P0.00");
    private PieChart pieChart = new PieChart();

    @Override
    public void start(Stage stage) {

        // --- LOAD SAVED EXPENSES ON STARTUP ---
        expenses.addAll(FileManager.loadExpenses());
        updateTotal();
        updateChart();

        // --- FORM FIELDS ---
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g. Lunch");

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 45.00");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Transport", "Education", "Entertainment", "Other");
        categoryBox.setPromptText("Select category");

        Button addButton = new Button("Add Expense");

        // --- FORM LAYOUT ---
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(20));

        form.add(new Label("Description:"), 0, 0);
        form.add(descriptionField, 1, 0);
        form.add(new Label("Amount (P):"), 0, 1);
        form.add(amountField, 1, 1);
        form.add(new Label("Category:"), 0, 2);
        form.add(categoryBox, 1, 2);
        form.add(addButton, 1, 3);

        // --- TABLE ---
        TableView<Expense> table = new TableView<>();

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount (P)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(dateCol, descCol, catCol, amountCol);
        table.setItems(expenses);

        // --- DELETE BUTTON ---
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                expenses.remove(selected);
                FileManager.saveExpenses(expenses);
                updateTotal();
                updateChart();
            } else {
                showAlert("Please select an expense to delete.");
            }
        });

        // --- TOTAL LABEL ---
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // --- BOTTOM BAR ---
        HBox bottomBar = new HBox(20, totalLabel, deleteButton);
        bottomBar.setPadding(new Insets(10, 20, 10, 20));

        // --- ADD BUTTON LOGIC ---
        addButton.setOnAction(e -> {
            String description = descriptionField.getText();
            String amountText = amountField.getText();
            String category = categoryBox.getValue();

            if (description.isEmpty() || amountText.isEmpty() || category == null) {
                showAlert("Please fill in all fields.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                expenses.add(new Expense(description, amount, category));
                FileManager.saveExpenses(expenses);
                updateTotal();
                updateChart();

                descriptionField.clear();
                amountField.clear();
                categoryBox.setValue(null);

            } catch (NumberFormatException ex) {
                showAlert("Amount must be a valid number.");
            }
        });

        // --- TAB 1: EXPENSES ---
        VBox expensesTab = new VBox(20, form, table, bottomBar);
        expensesTab.setPadding(new Insets(20));

        Tab tab1 = new Tab("Expenses", expensesTab);
        tab1.setClosable(false);

        // --- TAB 2: CHART ---
        pieChart.setTitle("Spending by Category");
        VBox chartTab = new VBox(pieChart);
        chartTab.setPadding(new Insets(20));

        Tab tab2 = new Tab("Chart", chartTab);
        tab2.setClosable(false);

        // --- TAB PANE ---
        TabPane tabPane = new TabPane(tab1, tab2);

        Scene scene = new Scene(tabPane, 650, 600);
        stage.setTitle("Expense Tracker");
        stage.setScene(scene);
        stage.show();
    }

    // Updates the pie chart based on current expenses
    private void updateChart() {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : expenses) {
            categoryTotals.put(
                e.getCategory(),
                categoryTotals.getOrDefault(e.getCategory(), 0.0) + e.getAmount()
            );
        }

        ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            chartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChart.setData(chartData);
    }

    private void updateTotal() {
        double total = 0;
        for (Expense e : expenses) {
            total += e.getAmount();
        }
        totalLabel.setText(String.format("Total: P%.2f", total));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
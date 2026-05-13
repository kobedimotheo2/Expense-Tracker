package com.expensetracker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;

public class App extends Application {

    private ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private Label totalLabel = new Label("Total: P0.00");
    private PieChart pieChart = new PieChart();
    private double budget = 0.0;

    @Override
    public void start(Stage stage) {

        expenses.addAll(FileManager.loadExpenses());
        updateTotal();
        updateChart();

        // --- HEADER ---
        Label title = new Label("💰 Expense Tracker");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        Label budgetLabel = makeLabel("Monthly Budget (P):");
        TextField budgetField = new TextField();
        budgetField.setPromptText("e.g. 1000");
        styleTextField(budgetField);
        budgetField.setMaxWidth(120);

        Button setBudgetButton = new Button("Set");
        setBudgetButton.setStyle("-fx-background-color: #bd93f9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
        setBudgetButton.setOnAction(e -> {
            try {
                budget = Double.parseDouble(budgetField.getText());
                updateTotal();
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid budget amount.");
            }
        });

        HBox header = new HBox(20, title, budgetLabel, budgetField, setBudgetButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #1e1e2e;");

        // --- FORM FIELDS ---
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g. Lunch");
        styleTextField(descriptionField);

        TextField amountField = new TextField();
        amountField.setPromptText("e.g. 45.00");
        styleTextField(amountField);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Transport", "Education", "Entertainment", "Other");
        categoryBox.setPromptText("Select category");
        categoryBox.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-border-color: #44475a; -fx-border-radius: 6; -fx-background-radius: 6;");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        // FilteredList declared here so searchField can reference it
        FilteredList<Expense> filteredExpenses = new FilteredList<>(expenses, p -> true);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search expenses...");
        styleTextField(searchField);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredExpenses.setPredicate(expense -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String search = newValue.toLowerCase();
                return expense.getDescription().toLowerCase().contains(search)
                    || expense.getCategory().toLowerCase().contains(search);
            });
        });

        Button addButton = new Button("+ Add Expense");
        addButton.setStyle("-fx-background-color: #6272a4; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        addButton.setMaxWidth(Double.MAX_VALUE);

        // --- FORM LAYOUT ---
        Label descLabel = makeLabel("Description");
        Label amountLabel = makeLabel("Amount (P)");
        Label catLabel = makeLabel("Category");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(25));
        form.setStyle("-fx-background-color: #282a36; -fx-background-radius: 12;");

        form.add(descLabel, 0, 0);
        form.add(descriptionField, 1, 0);
        form.add(amountLabel, 0, 1);
        form.add(amountField, 1, 1);
        form.add(catLabel, 0, 2);
        form.add(categoryBox, 1, 2);
        form.add(makeLabel("Search"), 0, 3);
        form.add(searchField, 1, 3);
        form.add(addButton, 1, 4);

        ColumnConstraints col1 = new ColumnConstraints(110);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        // --- TABLE ---
        TableView<Expense> table = new TableView<>();
        table.setStyle("-fx-background-color: #282a36; -fx-text-fill: white;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount (P)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        table.getColumns().addAll(dateCol, descCol, catCol, amountCol);
        table.setItems(filteredExpenses);

        // --- EDIT BUTTON ---
        Button editButton = new Button("✏ Edit Selected");
        editButton.setStyle("-fx-background-color: #ffb86c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        editButton.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Please select an expense to edit.");
                return;
            }

            // --- EDIT DIALOG ---
            Dialog<Expense> dialog = new Dialog<>();
            dialog.setTitle("Edit Expense");
            dialog.setHeaderText("Update the expense details");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Pre-fill fields with current values
            TextField editDesc = new TextField(selected.getDescription());
            styleTextField(editDesc);

            TextField editAmount = new TextField(String.valueOf(selected.getAmount()));
            styleTextField(editAmount);

            ComboBox<String> editCategory = new ComboBox<>();
            editCategory.getItems().addAll("Food", "Transport", "Education", "Entertainment", "Other");
            editCategory.setValue(selected.getCategory());
            editCategory.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-border-color: #44475a; -fx-border-radius: 6; -fx-background-radius: 6;");

            GridPane editForm = new GridPane();
            editForm.setHgap(10);
            editForm.setVgap(10);
            editForm.setPadding(new Insets(20));
            editForm.setStyle("-fx-background-color: #282a36;");

            editForm.add(makeLabel("Description:"), 0, 0);
            editForm.add(editDesc, 1, 0);
            editForm.add(makeLabel("Amount (P):"), 0, 1);
            editForm.add(editAmount, 1, 1);
            editForm.add(makeLabel("Category:"), 0, 2);
            editForm.add(editCategory, 1, 2);

            dialog.getDialogPane().setContent(editForm);
            dialog.getDialogPane().setStyle("-fx-background-color: #282a36;");

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        String newDesc = editDesc.getText();
                        double newAmount = Double.parseDouble(editAmount.getText());
                        String newCategory = editCategory.getValue();

                        if (newDesc.isEmpty() || newCategory == null) {
                            showAlert("Please fill in all fields.");
                            return null;
                        }

                        selected.setDescription(newDesc);
                        selected.setAmount(newAmount);
                        selected.setCategory(newCategory);
                        return selected;
                    } catch (NumberFormatException ex) {
                        showAlert("Amount must be a valid number.");
                        return null;
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedExpense -> {
                table.refresh();
                FileManager.saveExpenses(expenses);
                updateTotal();
                updateChart();
            });
        });

        // --- DELETE BUTTON ---
        Button deleteButton = new Button("🗑 Delete Selected");
        deleteButton.setStyle("-fx-background-color: #ff5555; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
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
        totalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        totalLabel.setTextFill(Color.web("#50fa7b"));

        // --- BOTTOM BAR ---
        HBox bottomBar = new HBox(20, totalLabel, editButton, deleteButton);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(15, 25, 15, 25));
        bottomBar.setStyle("-fx-background-color: #1e1e2e; -fx-background-radius: 0 0 12 12;");

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
        VBox expensesContent = new VBox(20, form, table, bottomBar);
        expensesContent.setPadding(new Insets(20));
        expensesContent.setStyle("-fx-background-color: #1e1e2e;");

        Tab tab1 = new Tab("  📋 Expenses  ", expensesContent);
        tab1.setClosable(false);

        // --- TAB 2: CHART ---
        pieChart.setTitle("Spending by Category");
        pieChart.setStyle("-fx-background-color: #1e1e2e;");
        pieChart.setLegendVisible(true);

        VBox chartContent = new VBox(pieChart);
        chartContent.setPadding(new Insets(20));
        chartContent.setStyle("-fx-background-color: #1e1e2e;");

        Tab tab2 = new Tab("  📊 Chart  ", chartContent);
        tab2.setClosable(false);

        // --- TAB PANE ---
        TabPane tabPane = new TabPane(tab1, tab2);
        tabPane.setStyle("-fx-background-color: #292947; -fx-tab-min-height: 40px;");

        VBox root = new VBox(header, tabPane);
        root.setStyle("-fx-background-color: #25253d;");

        Scene scene = new Scene(root, 700, 650);
        stage.setTitle("Expense Tracker");
        stage.setScene(scene);
        stage.show();
    }

    private Label makeLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#bd93f9"));
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return l;
    }

    private void styleTextField(TextField field) {
        field.setStyle("-fx-background-color: #2a2a3e; -fx-text-fill: white; -fx-prompt-text-fill: #6272a4; -fx-border-color: #44475a; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
    }

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

        // Make chart title and labels white
        pieChart.lookup(".chart-title").setStyle("-fx-text-fill: #bd93f9; -fx-font-size: 16px; -fx-font-weight: bold;");
        // Make legend text purple
        pieChart.lookupAll(".chart-legend-item").forEach(node -> 
            node.setStyle("-fx-text-fill: #bd93f9;")
        );

        // Make slice labels white
        pieChart.lookupAll(".chart-pie-label").forEach(node -> 
            node.setStyle("-fx-text-fill: #bd93f9; -fx-font-weight: bold; -fx-font-size: 13px;")
        );
    }

    private void updateTotal() {
        double total = 0;
        for (Expense e : expenses) {
            total += e.getAmount();
        }

        totalLabel.setText(String.format("Total: P%.2f", total));

        if (budget > 0 && total > budget) {
            totalLabel.setTextFill(Color.web("#f60909"));
            totalLabel.setText(String.format("Total: P%.2f ⚠ Over budget by P%.2f!", total, total - budget));
        } else if (budget > 0 && total >= budget * 0.9) {
            totalLabel.setTextFill(Color.web("#f9a245"));
            totalLabel.setText(String.format("Total: P%.2f ⚠ Near budget limit!", total));
        } else {
            totalLabel.setTextFill(Color.web("#28f45b"));
        }
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
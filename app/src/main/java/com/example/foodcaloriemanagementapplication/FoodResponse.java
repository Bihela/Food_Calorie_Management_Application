package com.example.foodcaloriemanagementapplication;

import java.util.List;

public class FoodResponse {
    private List<FoodItem> items;

    public List<FoodItem> getItems() {
        return items;
    }

    public class FoodItem {
        private String name;
        private double calories;
        private double fat_total_g;
        private double protein_g;
        private double carbohydrates_total_g;

        // Getters
        public String getName() { return name; }
        public double getCalories() { return calories; }
        public double getFatTotalG() { return fat_total_g; }
        public double getProteinG() { return protein_g; }
        public double getCarbohydratesTotalG() { return carbohydrates_total_g; }
    }
}

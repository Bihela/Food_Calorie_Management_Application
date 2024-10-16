package com.example.foodcaloriemanagementapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meal_table")
public class Meal {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String mealName;
    private String mealType;
    private float calories;  // Change this to float
    private String photoUri;  // Stores the URI of the meal photo

    // Constructor
    public Meal(String mealName, String mealType, float calories, String photoUri) { // Change int to float
        this.mealName = mealName;
        this.mealType = mealType;
        this.calories = calories;  // Initializes calories when creating a Meal object
        this.photoUri = photoUri;  // Initializes photoUri when creating a Meal object
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public float getCalories() { // Change return type to float
        return calories;
    }

    public void setCalories(float calories) { // Change parameter type to float
        this.calories = calories;
    }

    // Getter for photoUri (returns the URI of the meal photo)
    public String getPhotoUri() {
        return photoUri;
    }

    // Setter for photoUri (sets the URI for the meal photo)
    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }
}

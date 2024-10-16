package com.example.foodcaloriemanagementapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MealDao {
    @Insert
    void insert(Meal meal);

    @Query("SELECT * FROM meal_table")
    List<Meal> getAllMeals();
}

package com.example.foodcaloriemanagementapplication;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

import com.example.foodcaloriemanagementapplication.Meal;

@Database(entities = {Meal.class}, version = 2) // Update version number here
public abstract class AppDatabase extends RoomDatabase {
    public abstract MealDao mealDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "meal_database")
                            .fallbackToDestructiveMigration() // Allows destructive migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

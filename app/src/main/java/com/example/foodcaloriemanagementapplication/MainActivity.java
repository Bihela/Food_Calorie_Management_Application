package com.example.foodcaloriemanagementapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    // Define the daily calorie goal
    private static final float DAILY_CALORIE_GOAL = 2000f;

    private AppDatabase database;
    private FoodApiService foodApiService;
    private MealAdapter mealAdapter;
    private RecyclerView mealsRecyclerView;
    private TextView totalCaloriesTextView;
    private float totalCalories = 0f;

    private Uri photoUri;
    private String currentPhotoPath;

    // Firebase Storage variables
    private FirebaseStorage storage;
    private StorageReference storageReference;

    // ImageView to display the captured meal image
    private ImageView mealImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Room Database
        database = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "meal_database").allowMainThreadQueries().build();

        // Initialize Retrofit API service
        foodApiService = RetrofitClient.getClient().create(FoodApiService.class);

        // Initialize views
        EditText mealNameInput = findViewById(R.id.mealNameInput);
        EditText caloriesInput = findViewById(R.id.caloriesInput);
        Button addMealButton = findViewById(R.id.addMealButton);
        Button searchButton = findViewById(R.id.searchButton);
        Button capturePhotoButton = findViewById(R.id.capturePhotoButton);
        mealImageView = findViewById(R.id.mealImageView); // ImageView to display photo
        mealsRecyclerView = findViewById(R.id.mealsRecyclerView);
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView);

        // Set up RecyclerView
        mealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshMealList();  // Refresh meals when the app is opened
        calculateTotalCalories(database.mealDao().getAllMeals()); // Calculate and display total calories

        // Button to capture photo using the camera
        capturePhotoButton.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                Log.d(TAG, "Camera permission granted, launching camera.");
                cameraLauncher.launch(null);
            } else {
                Log.d(TAG, "Camera permission not granted, requesting permission.");
                requestCameraPermission();
            }
        });

        // Add meal button click listener
        addMealButton.setOnClickListener(v -> {
            String mealName = mealNameInput.getText().toString();
            try {
                float calories = Float.parseFloat(caloriesInput.getText().toString());
                Log.d(TAG, "Adding meal with name: " + mealName + " and calories: " + calories);
                uploadPhotoToFirebase(photoUri, mealName, calories);

                mealNameInput.setText("");
                caloriesInput.setText("");
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid calorie input: " + e.getMessage());
                Toast.makeText(this, "Please enter a valid number for calories.", Toast.LENGTH_SHORT).show();
            }
        });

        // Search button click listener for API
        searchButton.setOnClickListener(v -> {
            String foodName = mealNameInput.getText().toString();
            Log.d(TAG, "Searching for food: " + foodName);
            fetchFoodInfo(foodName, caloriesInput);
        });
    }

    // Check if the camera permission is granted
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Request the camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted by user.");
                cameraLauncher.launch(null);
            } else {
                Log.d(TAG, "Camera permission denied by user.");
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ActivityResultLauncher to capture the photo
    ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    Log.d(TAG, "Photo captured successfully.");
                    mealImageView.setImageBitmap(bitmap); // Display the captured image
                    Log.d(TAG, "ImageView updated with captured photo.");
                    saveImageToFile(bitmap);
                } else {
                    Log.e(TAG, "Photo capture failed or bitmap is null.");
                }
            });

    private void saveImageToFile(Bitmap bitmap) {
        try {
            File photoFile = createImageFile();
            FileOutputStream fos = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            photoUri = Uri.fromFile(photoFile);
            Log.d(TAG, "Photo saved successfully at: " + photoUri.toString());
            Toast.makeText(MainActivity.this, "Photo captured!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error saving photo: " + e.getMessage());
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "Image file created at: " + currentPhotoPath);
        return image;
    }

    private void refreshMealList() {
        Log.d(TAG, "Refreshing meal list.");
        List<Meal> updatedMeals = database.mealDao().getAllMeals();
        mealAdapter = new MealAdapter(updatedMeals);
        mealsRecyclerView.setAdapter(mealAdapter);
    }

    private void calculateTotalCalories(List<Meal> meals) {
        totalCalories = 0f;
        for (Meal meal : meals) {
            totalCalories += meal.getCalories();
        }
        Log.d(TAG, "Total calories calculated: " + totalCalories);
        updateCaloriesDisplay();
    }

    // Update the total calories and display comparison to the daily goal
    private void updateCaloriesDisplay() {
        float remainingCalories = DAILY_CALORIE_GOAL - totalCalories;
        String displayText;

        if (remainingCalories > 0) {
            displayText = "Total Calories: " + totalCalories + " kcal\n" +
                    "You have " + remainingCalories + " kcal remaining today.";
        } else {
            displayText = "Total Calories: " + totalCalories + " kcal\n" +
                    "You have exceeded your daily goal by " + Math.abs(remainingCalories) + " kcal.";
        }

        totalCaloriesTextView.setText(displayText);
    }

    private void uploadPhotoToFirebase(Uri photoUri, String mealName, float calories) {
        if (photoUri != null) {
            Log.d(TAG, "Uploading photo to Firebase: " + photoUri.toString());
            StorageReference ref = storageReference.child("meal_photos/" + UUID.randomUUID().toString());
            UploadTask uploadTask = ref.putFile(photoUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String photoUrl = uri.toString();
                    Log.d(TAG, "Photo uploaded successfully. URL: " + photoUrl);

                    // Use AsyncTask to insert into the database off the main thread
                    new InsertMealTask().execute(new Meal(mealName, "", calories, photoUrl));

                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Photo upload failed: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.d(TAG, "No photo selected for upload.");
            Toast.makeText(MainActivity.this, "No photo selected.", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch food information from API
    private void fetchFoodInfo(String foodName, EditText caloriesInput) {
        Call<FoodResponse> call = foodApiService.searchFood(foodName);
        call.enqueue(new Callback<FoodResponse>() {
            @Override
            public void onResponse(Call<FoodResponse> call, Response<FoodResponse> response) {
                if (response.isSuccessful()) {
                    FoodResponse foodResponse = response.body();
                    if (foodResponse != null && !foodResponse.getItems().isEmpty()) {
                        float calories = (float) foodResponse.getItems().get(0).getCalories();
                        Log.d(TAG, "Calories for " + foodName + ": " + calories);
                        caloriesInput.setText(String.valueOf(calories));
                    } else {
                        Log.e(TAG, "No results found for: " + foodName);
                        Toast.makeText(MainActivity.this, "No results found for " + foodName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API response error: " + response.errorBody());
                    Toast.makeText(MainActivity.this, "Failed to fetch food info.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(MainActivity.this, "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class InsertMealTask extends AsyncTask<Meal, Void, Void> {
        @Override
        protected Void doInBackground(Meal... meals) {
            // Change insertMeal() to insert()
            database.mealDao().insert(meals[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshMealList();  // Refresh the meal list after insertion
            calculateTotalCalories(database.mealDao().getAllMeals());  // Recalculate total calories
        }
    }

}

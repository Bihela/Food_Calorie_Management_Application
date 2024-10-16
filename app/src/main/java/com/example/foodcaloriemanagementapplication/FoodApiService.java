package com.example.foodcaloriemanagementapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface FoodApiService {
    @Headers("X-Api-Key: 4LEK6js7SapscULFXFpQ3Q==cb1zkXGsOnE5ORe6\n" +
            "\n")  // Add your API key
    @GET("/v1/nutrition")
    Call<FoodResponse> searchFood(@Query("query") String foodName);  // Change the method name here
}

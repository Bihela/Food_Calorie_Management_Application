package com.example.foodcaloriemanagementapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "https://api.calorieninjas.com/";
    private static final String API_KEY = "4LEK6js7SapscULFXFpQ3Q==cb1zkXGsOnE5ORe6\n" +
            "\n"; // Add your API key here

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}



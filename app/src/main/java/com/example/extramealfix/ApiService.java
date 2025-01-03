package com.example.extramealfix;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("test")  // Adjust this endpoint based on your API route
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}

package com.example.extramealfix;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AttendanceService {
    @FormUrlEncoded
    @POST("attendance/check-in")
    Call<AttendanceResponse> checkIn(
            @Field("EmpID") String empId,
            @Field("SiteName") String siteName,
            @Field("Shift") String shift,
            @Field("Lattitude") String latitude,
            @Field("Longitude") String longitude
    );

    @FormUrlEncoded
    @POST("attendance/check-out")
    Call<AttendanceResponse> checkOut(
            @Field("EmpID") String empId
    );

    @GET("api/attendance/status")
    Call<AttendanceResponse> checkStatus(
            @Query("EmpID") String empId,
            @Query("Date") String date
    );
}

package com.example.extramealfix;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FingerPrintActivity extends AppCompatActivity {
    private String empId;
    private String siteName;
    private String shift;
    private FusedLocationProviderClient fusedLocationClient;
    private AttendanceService attendanceService;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button btnCheckIn, btnCheckOut;
    private TextView tvCurrentTime, tvCurrentDate, tvCheckInStatus, tvCheckOutStatus;
    private final Handler timeHandler = new Handler();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", new Locale("id", "ID"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-d", new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_finger_print);
        initializeViews();
        setupTimeZone();
        startTimeUpdates();
        checkAttendanceStatus();

        empId = getIntent().getStringExtra("Extra_NIK");
        siteName = getIntent().getStringExtra("Extra_SiteName");
        shift = getIntent().getStringExtra("Extra_Shift");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        attendanceService = ApiClient.getClient().create(AttendanceService.class);

        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        // Initially disable check-out button
        btnCheckOut.setEnabled(false);

        btnCheckIn.setOnClickListener(v -> handleCheckIn());
        btnCheckOut.setOnClickListener(v -> handleCheckOut());

        // Check current attendance status when activity starts
        checkAttendanceStatus();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        empId = getIntent().getStringExtra("Extra_NIK");
        siteName = getIntent().getStringExtra("Extra_SiteName");
        shift = getIntent().getStringExtra("Extra_Shift");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        attendanceService = ApiClient.getClient().create(AttendanceService.class);

        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCheckInStatus = findViewById(R.id.tvCheckInStatus);
        tvCheckOutStatus = findViewById(R.id.tvCheckOutStatus);

        btnCheckIn.setOnClickListener(v -> handleCheckIn());
        btnCheckOut.setOnClickListener(v -> handleCheckOut());
        btnCheckOut.setEnabled(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupTimeZone() {
        TimeZone jakartaTimeZone = TimeZone.getTimeZone("Asia/Jakarta");
        timeFormat.setTimeZone(jakartaTimeZone);
        dateFormat.setTimeZone(jakartaTimeZone);
        apiDateFormat.setTimeZone(jakartaTimeZone);
    }

    private void startTimeUpdates() {
        timeHandler.post(new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                tvCurrentTime.setText(timeFormat.format(now));
                tvCurrentDate.setText(dateFormat.format(now));
                timeHandler.postDelayed(this, 1000);
            }
        });
    }

    private void checkAttendanceStatus() {
        String currentDate = apiDateFormat.format(new Date());

        attendanceService.checkStatus(empId, currentDate).enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AttendanceResponse.AttendanceData data = response.body().getData();
                    if (data != null) {
                        updateAttendanceDisplay(data);
                        if (data.getCheckOut() != null) {
                            // Both check-in and check-out done for today
                            btnCheckIn.setEnabled(false);
                            btnCheckOut.setEnabled(false);
                        } else if (data.getCheckIn() != null) {
                            // Only checked in
                            btnCheckIn.setEnabled(false);
                            btnCheckOut.setEnabled(true);
                        }
                    } else {
                        // No attendance record for today
                        tvCheckInStatus.setText("Jam Mulai: --:--:--");
                        tvCheckOutStatus.setText("Jam Selesai: --:--:--");
                        checkIfWithinWorkHours();
                    }
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                Toast.makeText(FingerPrintActivity.this,
                        "Failed to check attendance status: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAttendanceDisplay(AttendanceResponse.AttendanceData data) {
        String checkInTime = data.getCheckIn() != null ? data.getCheckIn() : "--:--:--";
        String checkOutTime = data.getCheckOut() != null ? data.getCheckOut() : "--:--:--";

        tvCheckInStatus.setText("Jam Mulai: " + checkInTime);
        tvCheckOutStatus.setText("Jam Selesai: " + checkOutTime);

        TextView tvThankYou = findViewById(R.id.tvThankYou);
        if (data.getCheckIn() != null && data.getCheckOut() != null) {
            tvThankYou.setVisibility(View.VISIBLE);
        } else {
            tvThankYou.setVisibility(View.GONE);
        }
    }

    private void checkIfWithinWorkHours() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta"));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        // Enable buttons only between 7 AM and end of day
        boolean isWorkHours = hour >= 7;
        btnCheckIn.setEnabled(isWorkHours);
        btnCheckOut.setEnabled(false); // Always start with disabled checkout
    }

    private void handleCheckIn() {
        if (checkLocationPermission()) {
            getCurrentLocation(location -> {
                String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                attendanceService.checkIn(empId, siteName, shift, latitude, longitude)
                        .enqueue(new Callback<AttendanceResponse>() {
                            @Override
                            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    // Update UI langsung setelah check-in berhasil
                                    String currentTime = timeFormat.format(new Date());
                                    tvCheckInStatus.setText("Jam Mulai: " + currentTime);
                                    btnCheckIn.setEnabled(false);
                                    btnCheckOut.setEnabled(true);

                                    Toast.makeText(FingerPrintActivity.this,
                                            response.body().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(FingerPrintActivity.this,
                                            "Check-in failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                                Toast.makeText(FingerPrintActivity.this,
                                        "Network error: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
    }

    private void handleCheckOut() {
        attendanceService.checkOut(empId)
                .enqueue(new Callback<AttendanceResponse>() {
                    @Override
                    public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Update UI langsung setelah check-out berhasil
                            String currentTime = timeFormat.format(new Date());
                            tvCheckOutStatus.setText("Jam Selesai: " + currentTime);
                            btnCheckOut.setEnabled(false);

                            // Tampilkan pesan terima kasih
                            showThankYouMessage();

                            Toast.makeText(FingerPrintActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Toast.makeText(FingerPrintActivity.this,
                                            "Check-out failed: " + errorBody,
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(FingerPrintActivity.this,
                                            "Check-out failed: " + response.code(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(FingerPrintActivity.this,
                                        "Check-out failed: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                        Toast.makeText(FingerPrintActivity.this,
                                "Network error: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showThankYouMessage() {
        // Tambahkan TextView baru di layout dengan id tvThankYou
        TextView tvThankYou = findViewById(R.id.tvThankYou);
        tvThankYou.setVisibility(View.VISIBLE);
        tvThankYou.setText("Terima kasih atas kehadiran Anda hari ini! Semoga harimu menyenangkan!");
    }

    // Other existing methods remain the same
    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacksAndMessages(null);
    }

    private void getCurrentLocation(OnSuccessListener<Location> onSuccess) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, onSuccess)
                .addOnFailureListener(this, e ->
                        Toast.makeText(FingerPrintActivity.this,
                                "Error getting location",
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
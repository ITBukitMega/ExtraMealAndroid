package com.example.extramealfix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText editTextNIK, editTextPassword;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;
    private UserCredentials userCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UserCredentials
        userCredentials = new UserCredentials(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupBiometricLogin();
    }

    private void initializeViews() {
        editTextNIK = findViewById(R.id.editTextNIK);
        editTextPassword = findViewById(R.id.editTextPassword);
        ImageView loginImage = findViewById(R.id.imageView2);
        ImageView fingerprintLogin = findViewById(R.id.fingerprint_login);

        loginImage.setOnClickListener(view -> performLogin());

        fingerprintLogin.setOnClickListener(view -> {
            if (canAuthenticateWithBiometrics()) {
                if (userCredentials.hasCredentials()) {
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Please login with NIK and password first",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this,
                        "Biometric authentication not available",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBiometricLogin() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (userCredentials.hasCredentials()) {
                            proceedToFingerPrintActivity(
                                    userCredentials.getEmpId(),
                                    userCredentials.getSiteName(),
                                    userCredentials.getShift()
                            );
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Please login with NIK and password first",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(MainActivity.this,
                                errString,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(MainActivity.this,
                                "Authentication failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private boolean canAuthenticateWithBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void performLogin() {
        String nik = editTextNIK.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (nik.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(nik, password);

        RetrofitClient.getInstance()
                .getApi()
                .login(loginRequest)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            if (loginResponse.isStatus()) {
                                // Save credentials for future biometric login
                                userCredentials.saveCredentials(
                                        nik,
                                        loginResponse.getData().getSiteName(),
                                        loginResponse.getData().getShift()
                                );

                                proceedToFingerPrintActivity(
                                        nik,
                                        loginResponse.getData().getSiteName(),
                                        loginResponse.getData().getShift()
                                );

                                Toast.makeText(MainActivity.this,
                                        loginResponse.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        loginResponse.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Invalid NIK or Password, Please Try Again",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Log.e("LoginError", "Error occurred: " + t.getMessage(), t); // Logging error
                        Toast.makeText(MainActivity.this,
                                "Please Connect To your Office Wifi",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void proceedToFingerPrintActivity(String nik, String siteName, String shift) {
        Intent intent = new Intent(MainActivity.this, FingerPrintActivity.class);
        intent.putExtra("Extra_NIK", nik);
        intent.putExtra("Extra_SiteName", siteName);
        intent.putExtra("Extra_Shift", shift);
        startActivity(intent);
        finish();
    }
}
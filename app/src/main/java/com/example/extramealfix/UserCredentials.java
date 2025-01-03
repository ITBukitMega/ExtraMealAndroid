package com.example.extramealfix;

import android.content.Context;
import android.content.SharedPreferences;

// Add this class to store user credentials
public class UserCredentials {
    private static final String PREF_NAME = "UserCredentials";
    private static final String KEY_EMP_ID = "emp_id";
    private static final String KEY_SITE_NAME = "site_name";
    private static final String KEY_SHIFT = "shift";
    private final SharedPreferences preferences;

    public UserCredentials(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveCredentials(String empId, String siteName, String shift) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_EMP_ID, empId);
        editor.putString(KEY_SITE_NAME, siteName);
        editor.putString(KEY_SHIFT, shift);
        editor.apply();
    }

    public String getEmpId() {
        return preferences.getString(KEY_EMP_ID, "");
    }

    public String getSiteName() {
        return preferences.getString(KEY_SITE_NAME, "");
    }

    public String getShift(){
        return preferences.getString(KEY_SHIFT, "");
    }

    public boolean hasCredentials() {
        return !getEmpId().isEmpty() && !getSiteName().isEmpty() && !getShift().isEmpty();
    }
}
package com.example.extramealfix;

public class LoginResponse {
    private boolean status;
    private String message;
    private LoginData data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LoginData getData() {
        return data;
    }

    public static class LoginData {
        private String EmpID;
        private String SiteName;
        private String Shift;

        public String getEmpID() {
            return EmpID;
        }

        public String getSiteName() {  // Add this getter
            return SiteName;
        }

        public String getShift(){
            return Shift;
        }
    }
}

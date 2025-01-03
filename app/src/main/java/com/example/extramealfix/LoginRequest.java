package com.example.extramealfix;


public class LoginRequest {
    private String EmpID;
    private String Password;

    public LoginRequest(String EmpID, String Password) {
        this.EmpID = EmpID;
        this.Password = Password;
    }
}

package com.example.extramealfix;

public class AttendanceResponse {
    private boolean status;
    private String message;
    private AttendanceData data;

    // Getters and setters
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public AttendanceData getData() { return data; }
    public void setData(AttendanceData data) { this.data = data; }

    public static class AttendanceData {
        private String EmpID;
        private String SiteName;
        private String Date;
        private String CheckIn;
        private String CheckOut;
        private String Lattitude;
        private String Longitude;
        private String Shift;

        // Getters and setters
        public String getEmpID() { return EmpID; }
        public void setEmpID(String empID) { this.EmpID = empID; }
        public String getSiteName() { return SiteName; }
        public void setSiteName(String siteName) { this.SiteName = siteName; }
        public String getDate() { return Date; }
        public void setDate(String date) { this.Date = date; }
        public String getCheckIn() { return CheckIn; }
        public void setCheckIn(String checkIn) { this.CheckIn = checkIn; }
        public String getCheckOut() { return CheckOut; }
        public void setCheckOut(String checkOut) { this.CheckOut = checkOut; }
        public String getLattitude() { return Lattitude; }
        public void setLattitude(String lattitude) { this.Lattitude = lattitude; }
        public String getLongitude() { return Longitude; }
        public void setLongitude(String longitude) { this.Longitude = longitude; }
        public String getShift(){return Shift;}
        public void setShift(String shift){this.Shift = shift;}
    }
}

package com.example.mobilepalengke.DataClasses;

public class AppInfo {

    private String status, downloadLink;
    private double latestVersion;

    public AppInfo() {
    }

    public String getStatus() {
        return status;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public double getLatestVersion() {
        return latestVersion;
    }

    public double getCurrentVersion() {
        return 0.02;
    }

    public boolean isDeveloper() {
        return false;
    }
}

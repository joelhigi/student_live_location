package com.specialops.tttstudent.models;

import androidx.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    private String role;
    private String route;
    @Nullable
    private String urlPicture;


    public User() {
    }

    public User(String _uid, String _username, @Nullable String _urlPicture) {
        uid = _uid;
        username = _username;
        urlPicture = _urlPicture;
    }

    public User(String _uid, String _username, String _role, @Nullable String _urlPicture) {
        uid = _uid;
        username = _username;
        role = _role;
        urlPicture = _urlPicture;
        route = "Route 1";
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String _uid) {
        uid = _uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String _username) {
        username = _username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String _role) {
        role = _role;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }

    public void setUrlPicture(@Nullable String _urlPicture) {
        urlPicture = _urlPicture;
    }

    public String getRoute()
    {
        return route;
    }

    public void setRoute(String newRoute)
    {
        route = newRoute;
    }
}

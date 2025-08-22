package com.s23010344.parkzone;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String username;
    public String email;
    public List<String> favourite_parks;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
        this.favourite_parks = new ArrayList<>();
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.favourite_parks = new ArrayList<>();
    }

    public User(String username, String email, List<String> favourite_parks) {
        this.username = username;
        this.email = email;
        this.favourite_parks = favourite_parks != null ? favourite_parks : new ArrayList<>();
    }
}

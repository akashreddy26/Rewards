package com.example.rewards;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserProfiles implements Serializable {
    String sid;
    String firstName;
    String lastName;
    String username;
    String department;
    String story;
    String position;
    int pointsToAward;
    boolean admin;
    String imageByteEncoded;
    String location;
    List<Rewards> rewards = new ArrayList<>();

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getPointsToAward() {
        return pointsToAward;
    }

    public void setPointsToAward(int pointsToAward) {
        this.pointsToAward = pointsToAward;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getImageByteEncoded() {
        return imageByteEncoded;
    }

    public void setImageByteEncoded(String imageByteEncoded) {
        this.imageByteEncoded = imageByteEncoded;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


}

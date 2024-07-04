package com.example.zenchat.models;

public class User {
    private  String uId,name,profile,city;

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public User(){

    }

    public String getuId() {
        return uId;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public String getCity() {
        return city;
    }

}

package com.panjacreation.videocall.models;

import java.io.Serializable;

public class User{
    String uid;
    String name;
    String profile;

    public User(String uid, String name, String profile) {
        this.uid = uid;
        this.name = name;
        this.profile = profile;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}

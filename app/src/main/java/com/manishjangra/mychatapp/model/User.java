package com.manishjangra.mychatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private String name;
    private String image;
    private String token;
    private String id;

    private String bio;

    public User(String name, String email, String image) {
        this.name = name;
        this.email = email;
        this.image = image;

    }

    public User(){

    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }


    // Parcelable implementation Methods below to pass the data in the Conversation Fragment -->

//    protected User(Parcel in) {
//        email = in.readString();
//        name = in.readString();
//        image = in.readString();
//        token = in.readString();
//    }
//
//    public static final Creator<User> CREATOR = new Creator<User>() {
//        @Override
//        public User createFromParcel(Parcel in) {
//            return new User(in);
//        }
//
//        @Override
//        public User[] newArray(int size) {
//            return new User[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(@NonNull Parcel dest, int flags) {
//        dest.writeString(email);
//        dest.writeString(name);
//        dest.writeString(image);
//        dest.writeString(token);
//    }
}

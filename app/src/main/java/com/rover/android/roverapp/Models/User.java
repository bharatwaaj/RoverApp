package com.rover.android.roverapp.Models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

// [START blog_user_class]
@IgnoreExtraProperties
public class User implements Serializable {

    public String username;
    public String email;
    public String phone;
    public String password;
    public String userId;

    public User(String userId,String username,String email) {

        this.userId = userId;
        this.username=username;
        this.email = email;


    }

    public User(String username, String email, String phone, String password) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }


}
// [END blog_user_class]

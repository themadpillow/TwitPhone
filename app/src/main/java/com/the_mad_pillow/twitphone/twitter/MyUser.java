package com.the_mad_pillow.twitphone.twitter;


import com.twitter.sdk.android.core.models.User;

public class MyUser {
    private User user;
    private boolean online = false;

    MyUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public MyUser setOnline(boolean isOnline) {
        online = isOnline;

        return this;
    }

    public boolean isOnline() {
        return online;
    }
}

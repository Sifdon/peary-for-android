package com.pearyapp.peary.beans;

import java.util.ArrayList;

/**
 * Created by Alexa on 29.10.2016.
 */

public class Members {

    private String mOwner;
    private ArrayList<String> mFriends = new ArrayList<>();

    public Members() {

    }

    public Members(String owner, ArrayList<String> friends) {
        this.mOwner = owner;
        this.mFriends = friends;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public ArrayList<String> getFriends() {
        return mFriends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.mFriends = friends;
    }

}

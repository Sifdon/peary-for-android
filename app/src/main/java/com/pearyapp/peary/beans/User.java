package com.pearyapp.peary.beans;

/**
 * Created by Alexa on 20.05.2016.
 */
public class User {

    private String mUsername, mEmail, mMainListID;


    /**
     * Required public constructor
     */
    public User() {
    }

    /**
     * Use this constructor to create new User.
     * Takes user's username, email and main list ID as params
     *
     * @param username user's username
     * @param email user's email
     * @param mainListID user's main list ID
     */
    public User(String username, String email, String mainListID) {
        this.mUsername = username;
        this.mEmail = email;
        this.mMainListID = mainListID;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getMainListID() {
        return mMainListID;
    }

    public void setMainListID(String mainListID) {
        this.mMainListID = mainListID;
    }

}

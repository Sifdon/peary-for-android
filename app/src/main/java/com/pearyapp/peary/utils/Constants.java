package com.pearyapp.peary.utils;

/**
 * Created by Alexa on 29.04.2016.
 */
public final class Constants {

    /**
     * Tags
     */
    public static final String LOGIN_TAG = "LoginActivity";

    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_MAIN_LIST_ID = "mainListID";
    public static final String FIREBASE_PROPERTY_EMAIL = "email";
    public static final String FIREBASE_PROPERTY_USERNAME = "username";
    public static final String FIREBASE_PROPERTY_REQUEST = "shareRequestBy";
    public static final String FIREBASE_PROPERTY_MEMBERS = "members";
    public static final String FIREBASE_PROPERTY_OWNER = "owner";

    /**
     * Constant for Firebase Paths
     */
    private static final String FIREBASE_LOCATION_SHOPPING_LIST = "shoppingList";
    private static final String FIREBASE_LOCATION_FRIDGE_LIST = "fridgeList";
    private static final String FIREBASE_LOCATION_USERS = "users";
    private static final String FIREBASE_LOCATION_FRIEND_LIST = "friendList";
    private static final String FIREBASE_LOCATION_DELETED_USERS = "deletedUsers";


    /**
     * Constants for Firebase URL
     */
//    private static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;
    public static final String FIREBASE_URL_SHOPPING_LIST = FIREBASE_LOCATION_SHOPPING_LIST;
    public static final String FIREBASE_URL_FRIDGE_LIST = FIREBASE_LOCATION_FRIDGE_LIST;
    public static final String FIREBASE_URL_USERS = FIREBASE_LOCATION_USERS;
    public static final String FIREBASE_URL_FRIEND_LIST = FIREBASE_LOCATION_FRIEND_LIST;
    public static final String FIREBASE_URL_DELETED_USERS = FIREBASE_LOCATION_DELETED_USERS;
}

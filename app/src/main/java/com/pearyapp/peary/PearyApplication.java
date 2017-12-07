package com.pearyapp.peary;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Alexa on 20.05.2016.
 */
public class PearyApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }

}
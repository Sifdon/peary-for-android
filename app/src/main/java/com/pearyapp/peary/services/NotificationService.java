package com.pearyapp.peary.services;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.pearyapp.peary.MainActivity;
import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.ListItems;
import com.pearyapp.peary.utils.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class NotificationService extends IntentService {

    List<ListItems> mArray = new ArrayList<>();

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//        mArray.add(new ListItems("Butter", 1, "01.05.2016", "02.05.2016", false, 1));

        final DatabaseReference mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIDGE_LIST);
        Query mQueryRef = mFirebaseRef.orderByChild("autorenew").equalTo(false);
        mQueryRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ListItems items = dataSnapshot.getValue(ListItems.class);
                items.setKey(dataSnapshot.getKey());
                mArray.add(items);

                if (mArray.size() > 0) {
                    for (ListItems item : mArray) {
                        try {
                            if (item.getExpiryCounter() == 3 || item.getExpiryCounter() == 2) {
                                fireNotification(item.getProductName(), item.getExpiryCounter(), getApplicationContext().getString(R.string.days));
                            } else if (item.getExpiryCounter() == 1) {
                                fireNotification(item.getProductName(), item.getExpiryCounter(), getApplicationContext().getString(R.string.day));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fireNotification(String productName, long expiryCounter, String dayS) {
        PugNotification.with(this)
                .load()
                .title(this.getString(R.string.content_title, productName))
                .message(this.getString(R.string.content_message, productName))
                .bigTextStyle(this.getString(R.string.content_text, productName, expiryCounter, dayS))
                .smallIcon(R.drawable.pugnotification_ic_launcher)
                .largeIcon(R.drawable.pugnotification_ic_launcher)
                .flags(Notification.DEFAULT_ALL)
                .autoCancel(true)
                .click(MainActivity.class)
                .simple()
                .build();
    }

}

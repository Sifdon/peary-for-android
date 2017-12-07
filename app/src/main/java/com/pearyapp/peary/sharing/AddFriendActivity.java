package com.pearyapp.peary.sharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.User;
import com.pearyapp.peary.login.LoginActivity;
import com.pearyapp.peary.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexa on 29.10.2016.
 */

public class AddFriendActivity extends AppCompatActivity {

    private EditText mEmailInput;
    private AddFriendAdapter mAdapter;
    private String mInput, mEncodedEmail, mListID;
    private RecyclerView mRecyclerView;
    private List<User> mArray = new ArrayList<>();
    private DatabaseReference mUsersRef, mFriendsRef;
    private Query mQueryRef;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ChildEventListener mChildEventListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    finish();
                    startActivity(new Intent(AddFriendActivity.this, LoginActivity.class));
                } else {
                    final FirebaseUser mUser = mAuth.getCurrentUser();
                    if (mUser != null) {

                        setContentView(R.layout.activity_add_friend);
                        mEncodedEmail = getIntent().getStringExtra(Constants.FIREBASE_PROPERTY_EMAIL);
                        mListID = getIntent().getStringExtra(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID);

                        mFriendsRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIEND_LIST).child(mUser.getUid());
                        mUsersRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS);
                        getFriends();

                        initializeScreen();
                        emailInput();
                    }
                }
            }
        };
    }

    public void initializeScreen() {
        mRecyclerView = (RecyclerView) findViewById(R.id.friend_rv);
        setRecyclerView();
        mEmailInput = (EditText) findViewById(R.id.edit_text_email);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void emailInput() {
        mEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mInput = mEmailInput.getText().toString().toLowerCase();

            /* Nullify the adapter data if the input length is less than 2 characters */
//                if (mInput.equals("") || mInput.length() < 2) {
////                    mRecyclerView.setAdapter(null);
////                    mArray.clear();
//
//            /* Define and set the adapter otherwise. */
//                } else {
//
//                    mQueryRef = mUsersRef.orderByChild(Constants.FIREBASE_PROPERTY_EMAIL).startAt(mInput).endAt(mInput + "~").limitToFirst(5);
//                    getData();
//
//                }
            }
        });
    }

    private void getFriends() {
        mFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final String emails = child.getKey();
                    mQueryRef = mUsersRef.orderByChild(Constants.FIREBASE_PROPERTY_EMAIL).equalTo(emails);

                    mAdapter = new AddFriendAdapter(AddFriendActivity.this, mArray, mEncodedEmail, mListID, mUsersRef, mQueryRef);
                    mRecyclerView.setAdapter(mAdapter);
                    getData();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getData() {
        if (mQueryRef != null) {
            mChildEventListener = mQueryRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User user = dataSnapshot.getValue(User.class);
                    mArray.clear();
                    mArray.add(0, user);
                    mAdapter.animateTo(mArray);
                    mRecyclerView.scrollToPosition(0);

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("SPL", "On cancelled: " + firebaseError.getMessage());
                }
            });
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mQueryRef != null) {
            mQueryRef.removeEventListener(mChildEventListener);
        }

        mRecyclerView.onFinishTemporaryDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}


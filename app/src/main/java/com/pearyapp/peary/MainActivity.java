package com.pearyapp.peary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pearyapp.peary.adapters.ViewPagerAdapter;
import com.pearyapp.peary.beans.User;
import com.pearyapp.peary.communicators.ToOtherFragmentCommunicator;
import com.pearyapp.peary.fragments.Fridge;
import com.pearyapp.peary.fragments.ShoppingList;
import com.pearyapp.peary.login.LoginActivity;
import com.pearyapp.peary.services.NotificationService;
import com.pearyapp.peary.utils.Constants;
import com.pearyapp.peary.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ToOtherFragmentCommunicator {

    private static final int FRIDGE = 1;
    private static final int SHOPPING_LIST = 0;
    private static ActionBarDrawerToggle sToggle;
    private DatabaseReference mFirebaseRef, mFriendListRef, mDataRef;
    private String mEncodedEmail;
    private DrawerLayout mDrawer;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private TextView mUserEmail, mUserName;
    private ImageView mUserPic;
    private int mAddedProduct = 0;
    private ShoppingList mShoppingList;
    private Fridge mFridge;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    View v = navigationView.getHeaderView(0);
                    mUserPic = v.findViewById(R.id.user_photo);
                    mUserName = v.findViewById(R.id.user_name);
                    mUserEmail = v.findViewById(R.id.user_email);
                    final FirebaseUser mUser = mAuth.getCurrentUser();
                    if (mUser != null) {
                        /*
                         * Name, email address, and profile photo Url
                         */
                        final String username = mUser.getDisplayName();
                        final String email = mUser.getEmail();
//                        Uri photo = mUser.getPhotoUrl();

                        mUserEmail.setText(email);

                        String firstChar;
                        if (username != null && !username.trim().isEmpty()) {
                            mUserName.setText(username);
                            firstChar = username.substring(0, 1);
                        } else {
                            /*
                             * E-mail can never be null
                             */
                            assert email != null;
                            mUserName.setText(email);
                            firstChar = email.substring(0, 1);
                        }

                        ColorGenerator generator = ColorGenerator.MATERIAL;
                        int charColor = generator.getColor(firstChar);
                        TextDrawable mTextDrawable = TextDrawable.builder().beginConfig().toUpperCase().endConfig().buildRound(firstChar, charColor);
                        mUserPic.setImageDrawable(mTextDrawable);

                        mEncodedEmail = Utils.encodeEmail(email);

                        mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS).child(mUser.getUid());
                        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                /*
                                 * Checks if user has a ListID and if not an ID gets generated
                                 */
                                if (dataSnapshot.getValue() == null) {
                                    User newUser = new User(username, mEncodedEmail, mFirebaseRef.push().getKey());
                                    mFirebaseRef.setValue(newUser);
                                } else {
                                    User user = dataSnapshot.getValue(User.class);
                                    Map<String, Object> updateUser = new HashMap<>();
                                    updateUser.put(Constants.FIREBASE_PROPERTY_USERNAME, username);
                                    updateUser.put(Constants.FIREBASE_PROPERTY_EMAIL, mEncodedEmail);
                                    if (user.getMainListID() == null) {
                                        user.setMainListID(mFirebaseRef.push().getKey());
                                    }
                                    updateUser.put(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID, user.getMainListID());
                                    mFirebaseRef.updateChildren(updateUser);

                                    updateFriendsList(user.getMainListID(), null);
                                }

                                checkForInvitation();

//                                mDataRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_SHOPPING_LIST).child(user.getMainListID());

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MainActivity.this, "Ein unerwarteter Fehler ist aufgetreten. Bitte versuche es später noch einmal!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }
        };


        initializeScreen();
//        notificationService();

    }

    private void checkForInvitation() {
        //TODO make own requestsBy node
        //TODO make opt out of list and copy all data
        //TODO make admin of list
        mFirebaseRef.child(Constants.FIREBASE_PROPERTY_REQUEST).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    final String requestMail = dataSnapshot.getKey();
                    if (!isFinishing()) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.new_invitation))
                                .setMessage(String.format(getString(R.string.do_you_want_to_share), Utils.decodeEmail(requestMail)))
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Searches for requested email, fetches the mainListID and updates the own ListID
                                        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                                                .orderByChild(Constants.FIREBASE_PROPERTY_EMAIL).equalTo(requestMail).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                                    if (child.exists()) {
                                                        User user = child.getValue(User.class);
                                                        final Map<String, Object> changeMainListID = new HashMap<>();
                                                        changeMainListID.put(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID, user.getMainListID());

                                                        mFirebaseRef.updateChildren(changeMainListID);
                                                        mFirebaseRef.child(Constants.FIREBASE_PROPERTY_REQUEST).child(requestMail).removeValue();

                                                        /*
                                                         * updates shopping list und fridge to new data
                                                         */
                                                        mShoppingList = (ShoppingList) mViewPagerAdapter.getItem(0);
                                                        mShoppingList.clearData();
                                                        mFridge = (Fridge) mViewPagerAdapter.getItem(1);
                                                        mFridge.clearData();
                                                        mViewPager.getAdapter().notifyDataSetChanged();
                                                        updateFriendsList(user.getMainListID(), requestMail);


//                                                        mDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                                                            @Override
//                                                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                                                for (DataSnapshot children : dataSnapshot.getChildren()) {
//                                                                    Log.i("CHILDREN", "" + children.getValue());
//                                                                    ListItems items = children.getValue(ListItems.class);
//                                                                    mShoppingList.createShoppingListItem(items.getProductName(), items.getQuantity(), items.hasAutorenew());
//
//                                                                }
//                                                            }
//
//                                                            @Override
//                                                            public void onCancelled(DatabaseError databaseError) {
//
//                                                            }
//                                                        });


                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                })
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        //deletes request
                                        mFirebaseRef.child(Constants.FIREBASE_PROPERTY_REQUEST).child(requestMail).removeValue();
                                    }
                                })
                                .setIcon(R.drawable.ic_person_add_18pt_3x)
                                .setCancelable(false)
                                .show();
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

    private void updateFriendsList(final String mainListID, final String requestMail) {
        /*
         * Update friend list of every user who shares the same mainListID
         */
        FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                .orderByChild(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID)
                .equalTo(mainListID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFriendListRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIEND_LIST);
                final HashMap<String, Object> allFriendsMap = new HashMap<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String UID = child.getKey();
                    final String email = child.child(Constants.FIREBASE_PROPERTY_EMAIL).getValue().toString();
                    allFriendsMap.put(email, true);


                    FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                            .child(UID)
                            .child(Constants.FIREBASE_PROPERTY_EMAIL).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            /*
                             * Clones the allFriendsMap and deletes/filters the user's email so the user doesn't have his own email in his friend list
                             */
                            HashMap filteredEmailMap = (HashMap) allFriendsMap.clone();
                            if (filteredEmailMap.containsKey(dataSnapshot.getValue())) {
                                filteredEmailMap.remove(dataSnapshot.getValue());
                                mFriendListRef.child(dataSnapshot.getRef().getParent().getKey()).updateChildren(filteredEmailMap);
                            }
//                            if(allFriendsMap.containsKey(requestMail)){
//                                allFriendsMap.put(requestMail, Constants.FIREBASE_PROPERTY_OWNER);
//                                FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_SHOPPING_LIST).child(mainListID).child(Constants.FIREBASE_PROPERTY_MEMBERS).updateChildren(allFriendsMap);
//                                FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIDGE_LIST).child(mainListID).child(Constants.FIREBASE_PROPERTY_MEMBERS).updateChildren(allFriendsMap);
//                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    /*
                      sets members in shopping list and fridge
                     */


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void notificationService() {
        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(this, NotificationService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(this, 100, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, AlarmManager.INTERVAL_HALF_HOUR, mPendingIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.nav_home:
                //open Main Activity
                break;
//            case R.id.nav_notification:
//                //open notifications
//                Toast.makeText(this, R.string.notifications, Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.nav_bill:
//                //show bills
//                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void itemToOtherFragment(String data, int number, boolean statement, int fragment) {
        if (FRIDGE == fragment) {
            mFridge = (Fridge) mViewPagerAdapter.getItem(fragment);
            mFridge.createFridgeItem(data, number, null, statement);

            //add Badge Notification
            mAddedProduct++;
            mViewPagerAdapter.setAddedProduct(mAddedProduct);
            mTabLayout.getTabAt(1).setText(mViewPagerAdapter.getPageTitle(1));
        } else if (SHOPPING_LIST == fragment) {
            mShoppingList = (ShoppingList) mViewPagerAdapter.getItem(fragment);
            mShoppingList.createShoppingListItem(data, number, statement);
        }
    }

    public void initializeScreen() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle(getResources().getString(R.string.home));

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        mViewPager = findViewById(R.id.view_layout);
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), this, primaryColor);
        mViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.setupWithViewPager(mViewPager);
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setText(mViewPagerAdapter.getPageTitle(i));
        }
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                //clear Badge Notification
                mAddedProduct = 0;
                if (tab.getPosition() == 1 && mViewPagerAdapter.getAddedProduct() != 0) {
                    mViewPagerAdapter.setAddedProduct(mAddedProduct);
                    mTabLayout.getTabAt(1).setText(mViewPagerAdapter.getPageTitle(1));
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        mDrawer = findViewById(R.id.drawer_layout);
        sToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(sToggle);
        sToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawer.removeDrawerListener(sToggle);
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


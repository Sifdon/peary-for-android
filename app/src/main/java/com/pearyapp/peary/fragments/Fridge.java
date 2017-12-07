package com.pearyapp.peary.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pearyapp.peary.ListItemsViewHolder;
import com.pearyapp.peary.ProductDetails;
import com.pearyapp.peary.R;
import com.pearyapp.peary.SettingsActivity;
import com.pearyapp.peary.adapters.FridgeAdapter;
import com.pearyapp.peary.beans.ListItems;
import com.pearyapp.peary.sharing.AddFriendActivity;
import com.pearyapp.peary.utils.Constants;
import com.pearyapp.peary.utils.Utils;
import com.pearyapp.peary.widgets.RecyclerItemClickListener;
import com.pearyapp.peary.widgets.SimpleTouchCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Alexa on 16.11.2015.
 */
public class Fridge extends Fragment implements SearchView.OnQueryTextListener {

    private static int sProductReq = 1, sUpdateReq = 2;
    private DatabaseReference mFirebaseRef;
    private FloatingActionButton mFab;
    private String mEncodedEmail, mListID;
    private RecyclerView mRecyclerView;
    private FridgeAdapter mAdapter;
    private View mEmptyView;
    private Intent mIntent;
    private Calendar mCalendar = Calendar.getInstance();
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private List<ListItems> mArray = new ArrayList<>();
    private ActionMode mActionMode;
    private AppCompatActivity mActivity;
    private ChildEventListener mChildEventListener;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.item_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                // This is to highlight the status bar and distinguish it from the action bar,
                // as the action bar while in the action mode is colored app_green_dark
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.cardview_dark_background));
            }
            menu.findItem(R.id.action_edit).setVisible(false);
            mFab.setVisibility(View.INVISIBLE);

            // Other stuff...
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            return true;
        }


        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<Integer> selectedItemPositions = mAdapter.getSelectedItems();
            int currPos;
            switch (item.getItemId()) {
                case R.id.action_delete:
                    for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                        currPos = selectedItemPositions.get(i);
                        mFirebaseRef.child(mArray.get(currPos).getKey()).removeValue();
                    }
                    mode.finish();
                    return true;
                case R.id.action_edit:
                    item.setVisible(false);
                    return false;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            for (int i = 0; i < mAdapter.getSelectedItems().size(); i++) {
                int currPos = mAdapter.getSelectedItems().get(i);
                if (mAdapter.getItemCount() > currPos) {
                    if (mAdapter.getItem(currPos).isSelected()) {
                        mAdapter.getItem(currPos).setSelected(false);
                    }
                }
            }

            mAdapter.clearSelections();
            mActivity = (AppCompatActivity) getActivity();
            mActivity.getSupportActionBar().show();
            mActionMode = null;
            mFab.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

            {
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fridgeView = inflater.inflate(R.layout.fridge_layout, container, false);
        mRecyclerView = (RecyclerView) fridgeView.findViewById(R.id.fridge_rv);
//        mEmptyView = fridgeView.findViewById(R.id.empty_view);
        setRecyclerView();
        setHasOptionsMenu(true);

//        mRecyclerView.setItemAnimator(null);
//        mRecyclerView.showIfEmpty(mEmptyView);
//        mRecyclerView.hideIfEmpty(mEmptyView);

        ItemTouchHelper.Callback mCallback = new SimpleTouchCallback(mAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            mEncodedEmail = Utils.encodeEmail(mUser.getEmail());

            FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                    .child(mUser.getUid())
                    .child(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mListID = (String) dataSnapshot.getValue();
                    mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIDGE_LIST).child(mListID);
                    mFirebaseRef.keepSynced(true);
                    getData();
                    itemTouchListener();
                    //        notificationService();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        return fridgeView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFab = (FloatingActionButton) getView().findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIntent = new Intent(Fridge.this.getActivity(), ProductDetails.class);
                startActivityForResult(mIntent, sProductReq);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == sProductReq && resultCode == ProductDetails.RESULT_OK) {
            createFridgeItem(data.getStringExtra("productname"), data.getIntExtra("quantity", 1), data.getStringExtra("expirydate"), data.getBooleanExtra("autorenew", false));
        } else if (requestCode == sUpdateReq && resultCode == ProductDetails.RESULT_OK) {
            //update item
            ListItems item = new ListItems();
            item.setProductName(data.getStringExtra("productname"));
            item.setQuantity(data.getIntExtra("quantity", 1));
            item.setInputDate(data.getStringExtra("inputdate"));
            item.setExpiryDate(data.getStringExtra("expirydate"));
            item.setAutorenew(data.getBooleanExtra("autorenew", false));
            item.setKey(data.getStringExtra("key"));
            mFirebaseRef.child(item.getKey()).setValue(item);
        }
    }

    public void createFridgeItem(String productName, int productQuantity, String expiryDate, boolean autorenew) {
        TimeZone.getDefault();

        ListItems item = new ListItems();

        item.setFragment(1);
        item.setProductName(productName);
        item.setQuantity(productQuantity);
        item.setInputDate(mSimpleDateFormat.format(mCalendar.getTime()));
        item.setAutorenew(autorenew);

        if (expiryDate == null && !autorenew) {
            //add days to current date for expiration time
            mCalendar.add(Calendar.DATE, 14);
            item.setExpiryDate(mSimpleDateFormat.format(mCalendar.getTime()));
        } else if (autorenew) {
            mCalendar.add(Calendar.DATE, 2);
            item.setExpiryDate(mSimpleDateFormat.format(mCalendar.getTime()));
        } else {
            item.setExpiryDate(expiryDate);
        }

        //reset time to current date after adding days
        mCalendar.setTime(new Date());

        item.getExpiryDate();

        mFirebaseRef.push().setValue(item);

    }

    /**
     * Called when clicked on a single item
     *
     * @param name      item product
     * @param quantity  item product
     * @param iDate     date when product was added in refrigerator
     * @param eDate     expiry date of product
     * @param autorenew true or false if product shall be added to shopping list after n days
     * @param mKey      unique ID of product
     */
    public void showProductDetails(String name, int quantity, String iDate, String eDate, boolean autorenew, String mKey) {
        mIntent = new Intent(Fridge.this.getActivity(), ProductDetails.class);
        mIntent.putExtra("productname", name);
        mIntent.putExtra("quantity", quantity);
        mIntent.putExtra("inputdate", iDate);
        mIntent.putExtra("expirydate", eDate);
        mIntent.putExtra("autorenew", autorenew);
        mIntent.putExtra("key", mKey);
        startActivityForResult(mIntent, sUpdateReq);
    }

    public void getData() {
        mChildEventListener = mFirebaseRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ListItems items = dataSnapshot.getValue(ListItems.class);
                items.setKey(dataSnapshot.getKey());
                mArray.add(0, items);
                mAdapter.animateTo(mArray);
                mRecyclerView.scrollToPosition(0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                ListItems newItem = dataSnapshot.getValue(ListItems.class);
                for (ListItems item : mArray) {
                    if (item.getKey().equals(key)) {
                        item.setValues(newItem);
                        break;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                for (ListItems item : mArray) {
                    if (key.equals(item.getKey())) {
                        mArray.remove(item);
                        break;
                    }
                }
                mAdapter.animateTo(mArray);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                ListItems newItem = dataSnapshot.getValue(ListItems.class);
                int index = mArray.indexOf(key);
                mArray.remove(index);
                if (previousChildName == null) {
                    mArray.add(0, newItem);
                } else {
                    int previousIndex = mArray.indexOf(previousChildName);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == mArray.size()) {
                        mArray.add(newItem);
                    } else {
                        mArray.add(nextIndex, newItem);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("SPL", "On cancelled: " + firebaseError.getMessage());
            }
        });
    }

    public void setRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new FridgeAdapter(getActivity(), mArray, Fridge.this, mFirebaseRef);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void itemTouchListener() {
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ListItemsViewHolder viewHolder = new ListItemsViewHolder(view);
                if (view.getId() == R.id.lin_layout) {
                    if (mActionMode != null) {
                        showSelectedTitle(mRecyclerView.getChildAdapterPosition(view));
                        if (mAdapter.getSelectedItemCount() > 1) {
                            mActionMode.getMenu().findItem(R.id.action_edit).setVisible(false);
                        } else {
                            mActionMode.getMenu().findItem(R.id.action_edit).setVisible(true);
                        }
                        if (mAdapter.getSelectedItemCount() <= 0) {
                            mActionMode.finish();
                        }
                        viewHolder.linearLayout.setOnClickListener(null);
                        if (!mAdapter.getItem(position).isSelected()) {
                            mAdapter.getItem(position).setSelected(true);
//                            viewHolder.itemView.setSelected(true);
                        } else {
                            mAdapter.getItem(position).setSelected(false);
//                            viewHolder.itemView.setSelected(false);
                        }

                    } else {
                        mAdapter.getItem(position).setSelected(false);
                        viewHolder.itemView.setSelected(false);
                        viewHolder.linearLayout.setClickable(true);
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemLongClick(View view, final int position) {
                if (mActionMode == null) {
                    mActivity = (AppCompatActivity) getActivity();
                    mActionMode = mActivity.startSupportActionMode(mActionModeCallback);
                    mActivity.getSupportActionBar().hide();
                    showSelectedTitle(mRecyclerView.getChildAdapterPosition(view));
                    mAdapter.getItem(position).setSelected(true);
                    if (mAdapter.getSelectedItemCount() <= 0) {
                        mAdapter.clearSelections();
                        mActionMode.finish();
                    }
                }
            }
        }, mRecyclerView));
    }

    private void showSelectedTitle(int idx) {
        mAdapter.toggleSelection(idx);
        String title = getActivity().getString(R.string.show_days_expiry, mAdapter.getSelectedItemCount(), getActivity().getString(R.string.selected));
        mActionMode.setTitle(title);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        final MenuItem settingsItem = menu.findItem(R.id.action_settings);
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }
        });

        final MenuItem inviteItem = menu.findItem(R.id.action_invite);
        inviteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mIntent = new Intent(getContext(), AddFriendActivity.class);
                mIntent.putExtra(Constants.FIREBASE_PROPERTY_EMAIL, mEncodedEmail);
                mIntent.putExtra(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID, mListID);
                startActivity(mIntent);
                return true;
            }
        });

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getActivity().getString(R.string.action_search));
        searchView.setOnQueryTextListener(this);

        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // Do something when collapsed
//                        mAdapter.flushFilter(mArray);

                        //with animation
                        mAdapter.animateTo(mArray);
                        mRecyclerView.scrollToPosition(0);
                        setItemsVisibility(menu, searchItem, true);
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // Do something when expanded
                        setItemsVisibility(menu, searchItem, false);
                        return true; // Return true to expand action view
                    }
                });
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<ListItems> filteredModelList = setFilter(mArray, query);

//        mAdapter.flushFilter(filteredModelList);

        //with animation
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * filter for list item searching
     *
     * @param items all list items from the RecyclerView
     * @param query search term
     * @return list of filtered items
     */
    private List<ListItems> setFilter(List<ListItems> items, String query) {
        query = query.toLowerCase();

        final List<ListItems> filteredModelList = new ArrayList<>();
        for (ListItems item : items) {
            final String text = item.getProductName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(item);
            }
        }
        return filteredModelList;
    }

    /**
     * disable Soft Input Keyboard if active
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            InputMethodManager input = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //detach Listeners here
        if (mFirebaseRef != null) {
            mFirebaseRef.removeEventListener(mChildEventListener);
        }
        mRecyclerView.onFinishTemporaryDetach();
    }

    public void clearData() {
        mArray.clear();
    }
}

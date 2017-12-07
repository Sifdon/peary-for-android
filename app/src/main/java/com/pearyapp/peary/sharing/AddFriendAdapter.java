package com.pearyapp.peary.sharing;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.pearyapp.peary.ListItemsViewHolder;
import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.User;
import com.pearyapp.peary.utils.Constants;
import com.pearyapp.peary.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexa on 29.10.2016.
 */

public class AddFriendAdapter extends RecyclerView.Adapter<ListItemsViewHolder> {

    private String mEncodedEmail, mListID;
    private Activity mActivity;
    private List<User> mArray;
    private DatabaseReference mUsersRef;
    private Query mQueryRef;

    AddFriendAdapter(Activity activity, List<User> mArray, String encodedEmail, String listID, DatabaseReference mUsersRef, Query mQueryRef) {
        this.mActivity = activity;
        this.mArray = new ArrayList<>(mArray);
        this.mEncodedEmail = encodedEmail;
        this.mListID = listID;
        this.mUsersRef = mUsersRef;
        this.mQueryRef = mQueryRef;
    }

    @Override
    public ListItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_item, parent, false);
        return new ListItemsViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ListItemsViewHolder holder, int position) {
        final User user = mArray.get(position);
        holder.friendName.setText(user.getUsername());
        holder.friendMail.setText(Utils.decodeEmail(user.getEmail()));


//        if(user.getPhoto() != null) {
//
//        } else{
        //Generates picture from first character of the username
        String firstChar = user.getUsername().substring(0, 1);
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int charColor = generator.getColor(firstChar);
        TextDrawable mTextDrawable = TextDrawable.builder().beginConfig().toUpperCase().endConfig().buildRound(firstChar, charColor);
        holder.profilePicture.setImageDrawable(mTextDrawable);
//        }

        /*
         * Set the onClickListener to a single list item
         * If selected email is not friend already and if it is not the
         * current user's email, we add selected user to current user's friends
         */
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                 * If selected user is not current user proceed
                 */
                if (isNotCurrentUser(user)) {

                    /*
                     * Add listener for single value event to perform a one time operation
                     */
                    mQueryRef.addChildEventListener(new ChildEventListener() {

                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            /*
                             * Checks if requested user shares already the same list
                             */
                            if (hasNotSameList(user.getMainListID(), mListID, user.getUsername())) {
                                mUsersRef.child(dataSnapshot.getKey()).child(Constants.FIREBASE_PROPERTY_REQUEST).child(mEncodedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //If user already sent request error, otherwise create request node with email as key & timestamp as value
                                        if (dataSnapshot.getValue() != null) {
                                            String alreadyAsked = String.format(mActivity.getResources().getString(R.string.already_asked), Utils.decodeEmail(user.getEmail()));
                                            Toast.makeText(mActivity, alreadyAsked, Toast.LENGTH_LONG).show();
                                        } else {
                                            Map<String, Object> shareRequest = new HashMap<>();
                                            shareRequest.put(mEncodedEmail, ServerValue.TIMESTAMP);
                                            String UID = dataSnapshot.getRef().getParent().getParent().getKey();

                                            mUsersRef.child(UID).child(Constants.FIREBASE_PROPERTY_REQUEST).updateChildren(shareRequest);

                                            String successfullyAsked = String.format(mActivity.getResources().getString(R.string.successfully_asked), Utils.decodeEmail(user.getEmail()));
                                            Toast.makeText(mActivity, successfullyAsked, Toast.LENGTH_LONG).show();
                                            mActivity.finish();
                                            // send push notification
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


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
                            Toast.makeText(mActivity, mActivity.getString(R.string.error_occured), Toast.LENGTH_LONG).show();
                        }

                    });

                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return mArray != null ? mArray.size() : 0;
    }

    private boolean isNotCurrentUser(User user) {
        if (user.getEmail().equals(mEncodedEmail)) {
            /* Toast appropriate error message if the user is trying to add themselves  */
            Toast.makeText(mActivity,
                    mActivity.getResources().getString(R.string.you_cant_add_yourself),
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean hasNotSameList(String mainListID, String requestedListID, String username) {
        if (mainListID.equals(requestedListID)) {
            /* Toast appropriate error message if the user shares already the same list */
            String friendError = String.format(mActivity.getResources().getString(R.string.shares_already_list), username);

            Toast.makeText(mActivity, friendError, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void animateTo(List<User> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);

    }

    private void applyAndAnimateRemovals(List<User> newItems) {
        for (int i = mArray.size() - 1; i >= 0; i--) {
            final User items = mArray.get(i);
            if (!newItems.contains(items)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<User> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final User items = newItems.get(i);
            if (!mArray.contains(items)) {
                addItem(i, items);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<User> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final User items = newItems.get(toPosition);
            final int fromPosition = mArray.indexOf(items);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private User removeItem(int position) {
        if (position != -1) {
            final User model = mArray.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mArray.size());
            return model;
        } else {
            return null;
        }
    }

    private void addItem(int position, User model) {
        mArray.add(position, model);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mArray.size());
    }

    private void moveItem(int fromPosition, int toPosition) {
        final User model = mArray.remove(fromPosition);
        mArray.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}

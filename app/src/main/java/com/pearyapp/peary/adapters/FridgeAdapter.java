package com.pearyapp.peary.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pearyapp.peary.ListItemsViewHolder;
import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.ListItems;
import com.pearyapp.peary.communicators.ItemTouchHelperAdapter;
import com.pearyapp.peary.communicators.ToOtherFragmentCommunicator;
import com.pearyapp.peary.fragments.Fridge;
import com.pearyapp.peary.utils.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexa on 06.01.2016.
 */
public class FridgeAdapter extends RecyclerView.Adapter<ListItemsViewHolder> implements ItemTouchHelperAdapter {

    private ToOtherFragmentCommunicator mComm;
    private Context mContext;
    private Fridge mFridge;
    private List<ListItems> mArray;
    private DatabaseReference mFirebaseRef;
    private SparseBooleanArray mSelectedItems;


    public FridgeAdapter(Context mContext, List<ListItems> mArray, Fridge mFridge, DatabaseReference mFirebaseRef) {
        this.mContext = mContext;
        this.mFridge = mFridge;
        this.mSelectedItems = new SparseBooleanArray();
        this.mArray = new ArrayList<>(mArray);
        this.mFirebaseRef = mFirebaseRef;
    }


    @Override
    public ListItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fridge_product_item, parent, false);
        mComm = (ToOtherFragmentCommunicator) mContext;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                    .child(mUser.getUid())
                    .child(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String mListID = (String) dataSnapshot.getValue();
                    mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_FRIDGE_LIST).child(mListID);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return new ListItemsViewHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(final ListItemsViewHolder holder, final int position) {
        final ListItems item = mArray.get(position);

        holder.productName.setText(item.getProductName());
        holder.productQuantity.setText(mContext.getString(R.string.show_x_quantity, item.getQuantity()));
        holder.inputDate.setText(item.getInputDate());

        if (item.isSelected()) {
            holder.itemView.setSelected(true);
            mSelectedItems.put(position, true);
            mSelectedItems.keyAt(position);

        } else {
            item.setSelected(false);
            holder.itemView.setSelected(false);
            if (mSelectedItems.get(position, false)) {
                mSelectedItems.delete(position);
                mSelectedItems.clear();
            }
        }

        if (item.hasAutorenew()) {
            holder.expiryDate.setText(R.string.autorenew_short);
            holder.expiryImage.setImageResource(R.drawable.ic_autorenew_black_48dp);
        } else {
            holder.expiryDate.setText(item.getExpiryDate());
            holder.expiryImage.setImageResource(R.drawable.ic_access_time_black_48dp);
        }

        try {
            if (item.getExpiryCounter() == 1 || item.getExpiryCounter() == -1) {
                holder.expiryCounter.setText(mContext.getString(R.string.show_days_expiry, item.getExpiryCounter(), mContext.getString(R.string.day)));
            } else {
                holder.expiryCounter.setText(mContext.getString(R.string.show_days_expiry, item.getExpiryCounter(), mContext.getString(R.string.days)));
            }

            if (!item.hasAutorenew()) {
                if (item.getExpiryCounter() == 2 || item.getExpiryCounter() == 3) {
                    holder.expiryCounter.setTextColor(Color.parseColor("#D6B116"));
                    if (item.getExpiryCounter() == 3) {
                        //Push notification
//                        notificationService();

                    }
                } else if (item.getExpiryCounter() == 1 || item.getExpiryCounter() == 0 || item.getExpiryCounter() <= 0) {
                    holder.expiryCounter.setTextColor(Color.parseColor("#EA2121"));
                    if (item.getExpiryCounter() == 1) {
                        //Push notification
//                        notificationBuild(items.getProductName(), items.getExpiryCounter(), mContext.getString(R.string.day));

                    } else if (item.getExpiryCounter() == 0) {
                        //Push notification
//                        notificationBuild(items.getProductName(), items.getExpiryCounter(), mContext.getString(R.string.days));
                    }
                } else {
                    holder.expiryCounter.setTextColor(Color.parseColor("#4CAF50"));
                }
            } else {
                holder.expiryCounter.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                if (item.getExpiryCounter() <= 0 && item.getKey() != null) {

                    //send item back to shopping list
                    mComm.itemToOtherFragment(item.getProductName(), item.getQuantity(), item.hasAutorenew(), 0);
                    mFirebaseRef.child(item.getKey()).removeValue();


                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFridge.showProductDetails(item.getProductName(), item.getQuantity(), item.getInputDate(), item.getExpiryDate(), item
                        .hasAutorenew(), item.getKey());

            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return (mArray != null ? mArray.size() : 0);
    }

    public ListItems getItem(Integer position) {
        return mArray.get(position);
    }


    public void animateTo(List<ListItems> items) {
        applyAndAnimateRemovals(items);
        applyAndAnimateAdditions(items);
        applyAndAnimateMovedItems(items);

    }

    private void applyAndAnimateRemovals(List<ListItems> newItems) {
        for (int i = mArray.size() - 1; i >= 0; i--) {
            final ListItems items = mArray.get(i);
            if (!newItems.contains(items)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ListItems> newItems) {
        for (int i = 0, count = newItems.size(); i < count; i++) {
            final ListItems items = newItems.get(i);
            if (!mArray.contains(items)) {
                addItem(i, items);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ListItems> newItems) {
        for (int toPosition = newItems.size() - 1; toPosition >= 0; toPosition--) {
            final ListItems items = newItems.get(toPosition);
            final int fromPosition = mArray.indexOf(items);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public ListItems removeItem(int position) {
        if (position != -1) {
            final ListItems model = mArray.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mArray.size());
            return model;
        } else {
            return null;
        }
    }

    public void addItem(int position, ListItems model) {
        mArray.add(position, model);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mArray.size());
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ListItems model = mArray.remove(fromPosition);
        mArray.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }


    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mArray, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mArray, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void toggleSelection(int pos) {
        if (mSelectedItems.get(pos, false)) {
            mSelectedItems.delete(pos);
        } else {
            mSelectedItems.put(pos, true);
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }


    public int getSelectedItemCount() {
        return mSelectedItems != null ? mSelectedItems.size() : 0;
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<Integer>(getSelectedItemCount());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            items.add(mSelectedItems.keyAt(i));
        }
        return items;
    }


}


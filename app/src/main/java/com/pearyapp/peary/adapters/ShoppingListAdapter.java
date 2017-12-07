package com.pearyapp.peary.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

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
import com.pearyapp.peary.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ShoppingListAdapter extends RecyclerView.Adapter<ListItemsViewHolder> implements ItemTouchHelperAdapter {

    private Context mContext;
    private List<ListItems> mArray, mOriginalArray;
    private ToOtherFragmentCommunicator mComm;
    private SparseBooleanArray mSelectedItems;
    private DatabaseReference mFirebaseRef;

    public ShoppingListAdapter(Context context, List<ListItems> mArray, DatabaseReference mFirebaseRef) {
        this.mContext = context;
//        this.mOriginalArray = mArray;
        this.mSelectedItems = new SparseBooleanArray();
        this.mArray = new ArrayList<>(mArray);
        this.mFirebaseRef = mFirebaseRef;
    }


    @Override
    public ListItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shoppinglist_product_item, parent, false);
        mComm = (ToOtherFragmentCommunicator) mContext;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_USERS)
                    .child(mUser.getUid())
                    .child(Constants.FIREBASE_PROPERTY_MAIN_LIST_ID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String mListID = (String) dataSnapshot.getValue();
                    mFirebaseRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_URL_SHOPPING_LIST).child(mListID);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return new ListItemsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ListItemsViewHolder holder, final int position) {
        final ListItems items = mArray.get(position);
        holder.productName.setText(items.getProductName());
        holder.productQuantity.setText(String.valueOf(items.getQuantity()));

        if (items.isSelected()) {
            holder.itemView.setSelected(true);
            mSelectedItems.put(position, true);
            mSelectedItems.keyAt(position);

        } else {
            items.setSelected(false);
            holder.itemView.setSelected(false);
            if (mSelectedItems.get(position, false)) {
                mSelectedItems.delete(position);
                mSelectedItems.clear();
            }
        }


        //in some cases, it will prevent unwanted situations
        holder.itemCheckbox.setOnCheckedChangeListener(null);
        //if true, your checkbox will be selected, else unselected
        holder.itemCheckbox.setChecked(items.isChecked());

        holder.itemCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //set your object's last status
                items.setChecked(isChecked);

                //send item to Fridge
                mComm.itemToOtherFragment(items.getProductName(), items.getQuantity(), items.hasAutorenew(), 1);
                mFirebaseRef.child(items.getKey()).removeValue();
//                mOriginalArray.remove(position);
//                removeItem(position);
//                animateTo(mArray);
//                notifyDataSetChanged();

            }
        });

        if (items.getQuantity() <= 1) {
            holder.decrementBtn.setVisibility(View.INVISIBLE);
            holder.incrementBtn.setVisibility(View.VISIBLE);
        } else if (items.getQuantity() >= 999) {
            holder.incrementBtn.setVisibility(View.GONE);
            holder.decrementBtn.setVisibility(View.VISIBLE);
        } else {
            holder.decrementBtn.setVisibility(View.VISIBLE);
            holder.incrementBtn.setVisibility(View.VISIBLE);
        }

        holder.incrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.productQuantity.setText(String.valueOf(items.incrementQuantity()));
                mFirebaseRef.child(items.getKey()).setValue(items);
//                notifyDataSetChanged();
            }
        });

        holder.decrementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.productQuantity.setText(String.valueOf(items.decrementQuantity()));
                mFirebaseRef.child(items.getKey()).setValue(items);
//                notifyDataSetChanged();
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

    public ListItems getItem(int position) {
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

package com.pearyapp.peary;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Alexa on 04.03.2016.
 */
public class ListItemsViewHolder extends RecyclerView.ViewHolder {

    public TextView productName, productQuantity, inputDate, expiryDate, expiryCounter, friendName, friendMail;
    public Button decrementBtn, incrementBtn;
    public CheckBox itemCheckbox;
    public LinearLayout linearLayout;
    public CardView cardView;
    public ImageView expiryImage, profilePicture;


    public ListItemsViewHolder(View itemView) {
        super(itemView);
        this.productName = (TextView) itemView.findViewById(R.id.product_name);
        this.productQuantity = (TextView) itemView.findViewById(R.id.quantity_field);
        this.itemCheckbox = (CheckBox) itemView.findViewById(R.id.item_checkbox);
        this.decrementBtn = (Button) itemView.findViewById(R.id.decrement_btn);
        this.incrementBtn = (Button) itemView.findViewById(R.id.increment_btn);
        this.inputDate = (TextView) itemView.findViewById(R.id.input_date);
        this.expiryDate = (TextView) itemView.findViewById(R.id.expiry_date);
        this.expiryCounter = (TextView) itemView.findViewById(R.id.expiry_counter);
        this.linearLayout = (LinearLayout) itemView.findViewById(R.id.lin_layout);
        this.cardView = (CardView) itemView.findViewById(R.id.card_view);
        this.expiryImage = (ImageView) itemView.findViewById(R.id.expiry_image);
        this.friendName = (TextView) itemView.findViewById(R.id.friend_name);
        this.friendMail = (TextView) itemView.findViewById(R.id.friend_mail);
        this.profilePicture = (ImageView) itemView.findViewById(R.id.profile_pic);
        itemView.setLongClickable(true);
    }

}

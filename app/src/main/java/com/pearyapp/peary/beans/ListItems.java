package com.pearyapp.peary.beans;


import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Alexa on 04.03.2016.
 */

public class ListItems {

    private int mProductQuantity, mFragment;
    private String mProductName, mInputDate, mExpiryDate, mKey, mAddedBy;
    private boolean mAutorenew, mSelected, mChecked;

    public ListItems() {
        // empty default constructor, necessary for Firebase to be able to deserialize list items
    }

    public ListItems(String mProductName, int mProductQuantity, String mInputDate, String mExpiryDate, boolean mAutorenew, int mFragment) {
        this.mProductName = mProductName;
        this.mProductQuantity = mProductQuantity;
        this.mInputDate = mInputDate;
        this.mExpiryDate = mExpiryDate;
        this.mAutorenew = mAutorenew;
        this.mFragment = mFragment;
    }

    @Exclude
    private static long daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }

        while (eDate.before(sDate)) {
            eDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween--;
        }

        return daysBetween;
    }

    @Exclude
    private static Calendar getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    public String getProductName() {
        return mProductName;
    }

    public void setProductName(String productName) {
        this.mProductName = productName;
    }

    public int getQuantity() {
        return mProductQuantity;
    }

    public void setQuantity(int quantity) {
        this.mProductQuantity = quantity;
    }

    @Exclude
    public int incrementQuantity() {
        mProductQuantity++;
        setQuantity(mProductQuantity);
        return mProductQuantity;
    }

    @Exclude
    public int decrementQuantity() {
        mProductQuantity--;
        setQuantity(mProductQuantity);
        return mProductQuantity;
    }

    public String getInputDate() {
        return mInputDate;
    }

    public void setInputDate(String inputDate) {
        this.mInputDate = inputDate;
    }

    public String getExpiryDate() {
        return mExpiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.mExpiryDate = expiryDate;
    }

    @PropertyName("autorenew")
    public boolean hasAutorenew() {
        return mAutorenew;
    }

    public void setAutorenew(boolean autorenew) {
        this.mAutorenew = autorenew;
    }

    @Exclude
    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        this.mSelected = selected;
    }

    @Exclude
    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    @Exclude
    public int getFragment() {
        return mFragment;
    }

    public void setFragment(int fragment) {
        this.mFragment = fragment;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getmAddedBy() {
        return mAddedBy;
    }

    public void setmAddedBy(String addedBy) {
        this.mAddedBy = addedBy;
    }

    @Exclude
    public long getExpiryCounter() throws ParseException {
        TimeZone.getDefault();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date todayDate = new Date();
        Date exDate = dateFormat.parse(mExpiryDate);

        return daysBetween(todayDate, exDate);

    }

    public void setValues(ListItems newItem) {
        this.mProductName = newItem.mProductName;
        this.mProductQuantity = newItem.mProductQuantity;
        this.mInputDate = newItem.mInputDate;
        this.mExpiryDate = newItem.mExpiryDate;
        this.mAutorenew = newItem.mAutorenew;
        this.mFragment = newItem.mFragment;
    }
}

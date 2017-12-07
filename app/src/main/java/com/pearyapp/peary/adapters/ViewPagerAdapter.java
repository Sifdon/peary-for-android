package com.pearyapp.peary.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import com.pearyapp.peary.R;
import com.pearyapp.peary.beans.ListItems;
import com.pearyapp.peary.fragments.Fridge;
import com.pearyapp.peary.fragments.ShoppingList;

import java.util.ArrayList;
import java.util.List;

import cn.nekocode.badge.BadgeDrawable;

/**
 * Created by Alexa on 16.11.2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private int[] mTabIcons = {R.drawable.selector_list, R.drawable.selector_fridge};
    private ShoppingList mShoppingList;
    private Fridge mFridge;
    private Context mContext;
    private int addedProduct, primaryColor;
    private List<ListItems> mArray = new ArrayList<>();

    public ViewPagerAdapter(android.support.v4.app.FragmentManager manager, Context mContext, int primaryColor) {
        super(manager);
        this.mShoppingList = new ShoppingList();
        this.mFridge = new Fridge();
        this.mContext = mContext;
        this.primaryColor = primaryColor;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mShoppingList;
            case 1:
                return mFridge;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        Drawable image = ResourcesCompat.getDrawable(mContext.getResources(), mTabIcons[position], null);
        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
        image.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        // Replace blank spaces with image icon
        SpannableString mSpannableString;
        if (position == 1 && getAddedProduct() > 0) {
            BadgeDrawable mBadgeDrawable = new BadgeDrawable.Builder().type(BadgeDrawable.TYPE_NUMBER).number(getAddedProduct()).badgeColor(Color.WHITE).textColor(primaryColor).build();
            mSpannableString = new SpannableString(TextUtils.concat("  ", mBadgeDrawable.toSpannable()));
        } else {
            mSpannableString = new SpannableString(" ");
        }
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        mSpannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return mSpannableString;
    }

    public int getIcon(int position) {
        return mTabIcons[position];
    }

    public int getAddedProduct() {
        return addedProduct;
    }

    public void setAddedProduct(int addedProduct) {
        this.addedProduct = addedProduct;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}

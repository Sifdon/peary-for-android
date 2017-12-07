package com.pearyapp.peary.widgets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.pearyapp.peary.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexa on 29.03.2016.
 */
public class RecyclerViewObserver extends RecyclerView {

    private List<View> mNonEmptyViews = Collections.emptyList();
    private List<View> mEmptyViews = Collections.emptyList();

    private AdapterDataObserver mObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            toogleViews();

        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            toogleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            toogleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toogleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toogleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            toogleViews();
        }
    };

    private void toogleViews() {
        if (getAdapter() != null && !mEmptyViews.isEmpty() && !mNonEmptyViews.isEmpty()) {
            if (getAdapter().getItemCount() == 0) {
                //show all empty views
                Utils.showViews(mEmptyViews);

                //hide RecyclerView
                setVisibility(View.GONE);

                //hide views which are meant to be hidden
                Utils.hideViews(mNonEmptyViews);
            } else {
                //hide all empty views
                Utils.showViews(mNonEmptyViews);

                //show RecyclerView
                setVisibility(View.VISIBLE);

                Utils.hideViews(mEmptyViews);
            }
        }
    }

    public RecyclerViewObserver(Context context) {
        super(context);
    }

    public RecyclerViewObserver(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewObserver(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
        mObserver.onChanged();
    }

    public void hideIfEmpty(View... views) {
        mNonEmptyViews = Arrays.asList(views);
    }

    public void showIfEmpty(View... emptyViews) {
        mEmptyViews = Arrays.asList(emptyViews);
    }
}

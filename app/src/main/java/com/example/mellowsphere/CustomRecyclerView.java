package com.example.mellowsphere;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class CustomRecyclerView extends RecyclerView {
    private ViewPager2 viewPager2;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setViewPager2(ViewPager2 viewPager2) {
        this.viewPager2 = viewPager2;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // Disable ViewPager2 scrolling when RecyclerView is being touched
        if (viewPager2 != null) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewPager2.setUserInputEnabled(false);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    viewPager2.setUserInputEnabled(true);
                    break;
            }
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean performClick() {
        // Dummy implementation to satisfy lint check
        return super.performClick();
    }
}


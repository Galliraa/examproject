package com.example.kenneth.examproject;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

//https://stackoverflow.com/questions/15013535/how-to-make-a-scrollable-fragment-based-on-the-default-android-pageview

public class EventMapFragment extends SupportMapFragment {
    private OnTouchListener mapListener;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View layout = super.onCreateView(layoutInflater, viewGroup, bundle);

        TouchableWrapper frameLayout = new TouchableWrapper(getActivity());

        frameLayout.setBackgroundColor(Color.TRANSPARENT);

        ((ViewGroup) layout).addView(frameLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return layout;
    }

    public void setListener(OnTouchListener listener) {
        mapListener = listener;
    }

    public interface OnTouchListener {
        void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {

        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mapListener.onTouch();
                    break;
                case MotionEvent.ACTION_UP:
                    mapListener.onTouch();
                    break;
            }
            return super.dispatchTouchEvent(event);
        }
    }
}

package com.example.kenneth.examproject;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Field;

/**

 */
public class DetailsFragment extends Fragment implements OnMapReadyCallback{

    private TextView nameTV;
    private TextView dateTV;
    private TextView timeTV;
    private TextView addressTV;
    private TextView descTV;
    private NetworkImageView eventIV;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;

    private double latti = 56.16984751;
    private double longi = 10.20561455;

    private GoogleMap eMap;

    private EventSelectorInterface eventSelector;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        nameTV = (TextView) view.findViewById(R.id.eventNameTV);
        dateTV = (TextView) view.findViewById(R.id.eventDateTV);
        timeTV = (TextView) view.findViewById(R.id.eventTimeTV);
        addressTV = (TextView) view.findViewById(R.id.eventAddressTV);
        descTV = (TextView) view.findViewById(R.id.eventDescTV);
        eventIV = (NetworkImageView) view.findViewById(R.id.eventIV);

        mRequestQueue = Volley.newRequestQueue(getContext());
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get("url");
            }
        });
       // updateEvents();
        setUpMap();

        Log.d("DETAILSFRAGMENT", "onCreateView: Called");

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            eventSelector = (EventSelectorInterface) context;
        }catch (ClassCastException ex)
        {
            throw new ClassCastException(context.toString() + " must implement EventSelectorInterface");
        }
    }

    public void updateEvents(){
        if(eventSelector != null)
        {
            setEvent(eventSelector.getCurrentSelection());
        }
    }

    public void setEvent(Event event)
    {
        if(nameTV !=null && dateTV != null && timeTV != null)
        {
            nameTV.setText(event.getName());
            addressTV.setText(event.getAddress());
            descTV.setText(event.getDescrition());
            eventIV.setImageUrl(event.getEventImage(), mImageLoader);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
             /*NOTE: this is a small hack to fix appearant bug with support library that puts
         * fragment in an illegal state
         * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
         */

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        eMap = googleMap;
        // Check if we were successful in obtaining the map.
        if (eMap != null) {
            eMap.addMarker(new MarkerOptions().position(new LatLng(latti, longi)).title("You are here!"));
            zoomToUser();
        }

    }

    public void setUpMap(){
        if (eMap == null){
            ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.MapFragment))
                    .getMapAsync(this);
        }
    }

    private void zoomToUser(){

            eMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latti, longi), 12));
    }
}

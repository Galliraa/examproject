package com.example.kenneth.examproject;

import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.kenneth.examproject.Adapters.DayListAdapter;
import com.example.kenneth.examproject.DatabaseHelpers.DatabaseHelper;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DayFragment extends Fragment {

    private ListView eventListView;
    private DayListAdapter dayListAdapter;
    private List<Event> events;
    private DatabaseHelper database;

    private EventSelectorInterface eventSelector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        eventListView = (ListView) view.findViewById(R.id.eventLV);

        //database = new DatabaseHelper(getActivity().getApplicationContext());
        dayListAdapter = new DayListAdapter(getActivity(), events);
        eventListView.setAdapter(dayListAdapter);

        updateEvents();
        return view;
    }


    // in this must be implemented sorting by date
    public void updateEvents(){
        if(eventSelector != null)
        {
            events = eventSelector.getEventList();
        }
        if (events != null)
        {
            dayListAdapter = new DayListAdapter(getActivity().getBaseContext(), events);
            eventListView.setAdapter(dayListAdapter);

            eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onEventSelected(position);

                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEvents();
    }

    public void setEvents(ArrayList<Event> eventList){
        events = (ArrayList<Event>) eventList.clone();
    }

    private void onEventSelected(int position) {
        if(eventSelector !=null) {
            eventSelector.onEventSelected(position);
        }
    }


    public void setEvents(Event event)
    {

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
}

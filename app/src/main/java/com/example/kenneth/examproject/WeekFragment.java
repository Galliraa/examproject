package com.example.kenneth.examproject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.kenneth.examproject.Adapters.WeekListAdapter;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class WeekFragment extends Fragment {


    private ListView eventListView;
    private WeekListAdapter weekListAdapter;
    private ArrayList<Event> events;

    private EventSelectorInterface eventSelector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_week, container, false);
        eventListView = (ListView) view.findViewById(R.id.eventLV);
        updateEvents();
        return view;
    }


    public void updateEvents(){
        if(eventSelector != null)
        {
            events = eventSelector.getEventList();
        }
        if (events != null)
        {
            weekListAdapter = new WeekListAdapter(getActivity(), events);
            eventListView.setAdapter(weekListAdapter);

            eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onSongSelected(position);

                }
            });
        }
    }

    public void setEvents(ArrayList<Event> eventList){
        events = (ArrayList<Event>) eventList.clone();
    }

    private void onSongSelected(int position) {
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

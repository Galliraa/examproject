package com.example.kenneth.examproject;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.kenneth.examproject.Adapters.DayListAdapter;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Interfaces.ForceUiUpdateInterface;
import com.example.kenneth.examproject.Models.Event;

import java.lang.reflect.Field;
import java.util.List;

public class DayFragment extends Fragment implements ForceUiUpdateInterface {

    private ListView eventListView;
    private DayListAdapter dayListAdapter;
    private List<Event> events;
    private ProgressBar spinner;
    private boolean searchDone = false;

    private EventSelectorInterface eventSelector;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

            View view = inflater.inflate(R.layout.fragment_day, container, false);
            eventListView = (ListView) view.findViewById(R.id.eventLV);
            spinner = (ProgressBar) view.findViewById(R.id.progressBarDay);

        if(savedInstanceState != null) {
            searchDone = savedInstanceState.getBoolean("searchState");
            if(searchDone)
                spinner.setVisibility(View.GONE);
        }

        return view;
    }

    public void updateEvents(){
        if(eventSelector != null)
        {
            events = eventSelector.getEventListDay();
        }
        if (events != null) {
            dayListAdapter = new DayListAdapter(getActivity().getBaseContext(), events);

            if (eventListView != null) {
                eventListView.setAdapter(dayListAdapter);
                eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        onEventSelected(position);

                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void onEventSelected(int position) {
        if(eventSelector !=null) {
            eventSelector.onEventSelected(position);
        }
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

    public void stopSpinner()
    {
        spinner.setVisibility(View.GONE);
        searchDone = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("searchState", searchDone);
    }
}
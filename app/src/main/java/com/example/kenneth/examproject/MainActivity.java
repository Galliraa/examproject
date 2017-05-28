package com.example.kenneth.examproject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kenneth.examproject.Adapters.PageAdapter;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Interfaces.ForceUiUpdateInterface;
import com.example.kenneth.examproject.Models.Event;
import com.example.kenneth.examproject.Services.EventService;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;


// sources:
// https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter

public class MainActivity extends AppCompatActivity implements EventSelectorInterface{

    private final static String EVENT_TAG = "EVENT";
    private final static String RESUME_TAG = "ONRESUME";
    private static final String DETAILS_FRAG = "details_fragment";


    private enum PhoneMode {PORTRAIT, LANDSCAPE}
    private enum UserMode {LIST_VIEW, DETAILS_VIEW}

    FragmentPagerAdapter adapterViewPager;

    private EventService eventService;
    private DetailsFragment eventDetails;
    private List<Event> events;
    private PhoneMode phoneMode;
    private UserMode userMode;
    private int selectedEventIndex;
    private boolean isBound;
    private ViewPager vpPager;
    private LinearLayout detailsContainer;

    @Override
    public void onEventSelected(int position) {
        if(eventDetails!=null){
            Event selectedEvent = events.get(position);
            if(selectedEvent!=null) {
                selectedEventIndex = position;
                eventDetails.setEvent(selectedEvent);
            }
        }
        updateFragmentViewState(UserMode.DETAILS_VIEW);
    }

    @Override
    public List<Event> getEventList() {
        return events;
    }

    @Override
    public List<Event> getEventListDay() {
        if(events != null) {
            List<Event> list = new Vector<>();
            Calendar c = Calendar.getInstance();
            String month;
            String day;

            if((c.get(Calendar.MONTH)+1) < 10)
                month = "0"+(c.get(Calendar.MONTH)+1);
            else
                month = String.valueOf(c.get(Calendar.MONTH)+1);

            if((c.get(Calendar.DAY_OF_MONTH)+1) < 10)
                day = "0"+(c.get(Calendar.DAY_OF_MONTH));
            else
                day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));


            String compare = String.valueOf(c.get(Calendar.YEAR)) + "-" + month + "-" + day;
            for (int i = 0; i < events.size(); i++) {
                if (!events.get(i).getStartTime().substring(0, 10).equals(compare)) {
                    if (i == 0)
                        return null;
                    else
                        return list;
                }
                list.add(events.get(i));
            }
        }
        return null;
    }

    @Override
    public List<Event> getEventListWeek() {
        if(events != null) {
            List<Event> list = new Vector<>();
            Calendar c = Calendar.getInstance();
            for (int i = 0; i < events.size(); i++) {
                if ((Integer.parseInt(events.get(i).getStartTime().substring(8, 10)) > (c.get(Calendar.DAY_OF_MONTH)+7)) || Integer.parseInt(events.get(i).getStartTime().substring(5, 7)) != (c.get(Calendar.MONTH))+1) {
                        if (i == 0)
                            return null;
                        else
                            return list;
                    }
                    list.add(events.get(i));
                }
        }
        return events;
    }


    @Override
    public Event getCurrentSelection() {
        if(events != null)
            return events.get(selectedEventIndex);
        else
            return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views);

        final ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        vpPager = (ViewPager) findViewById(R.id.list_container);
        adapterViewPager = new PageAdapter(getSupportFragmentManager(), getApplicationContext());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setOffscreenPageLimit(2);
        detailsContainer = (LinearLayout)findViewById(R.id.details_container);

        //determine orientation
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            phoneMode = PhoneMode.PORTRAIT;
        } else {
            phoneMode = PhoneMode.LANDSCAPE;
        }

        if(savedInstanceState == null) {

            selectedEventIndex = 0;

            eventDetails = new DetailsFragment();

            if(userMode == null){
                userMode = UserMode.LIST_VIEW;  //default
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details_container, eventDetails, DETAILS_FRAG)
                    .commit();

        } else {
            //got restarted, probably due to orientation change
            selectedEventIndex = savedInstanceState.getInt("event_position");
            userMode = (UserMode) savedInstanceState.getSerializable("user_mode");

            if (userMode == null) {
                userMode = UserMode.LIST_VIEW;  //default
            }

            eventDetails = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILS_FRAG);
            if(eventDetails ==null){
                eventDetails = new DetailsFragment();
            }
        }



            // Attach the page change listener inside the activity
            vpPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {

                // This method will be invoked when a new page becomes selected.
                @Override
                public void onPageSelected(int position) {
                    Fragment fragment = ((PageAdapter)vpPager.getAdapter()).getFragment(position);

                    if (fragment != null)
                    {
                        ((ForceUiUpdateInterface)fragment).updateEvents();
                    }
                }

                // This method will be invoked when the current page is scrolled
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // Code goes here
                }

                // Called when the scroll state changes:
                // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
                @Override
                public void onPageScrollStateChanged(int state) {
                    // Code goes here
                }
            });
    }

    @Override
    public void onBackPressed() {
        if(phoneMode == PhoneMode.LANDSCAPE){
            //finish on back press in landscape, go to search page
                setResult(RESULT_OK);
                finish();
        } else {
            if (userMode == UserMode.DETAILS_VIEW) {
                //go back to last used listview
                updateFragmentViewState(UserMode.LIST_VIEW);
            } else if (userMode == UserMode.LIST_VIEW) {
                //go to search activity from listview
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("event_position", selectedEventIndex);
        outState.putSerializable("user_mode", userMode);
        super.onSaveInstanceState(outState);
    }

    private void updateFragmentViewState(UserMode targetMode){

        if(targetMode == UserMode.LIST_VIEW) {
            userMode = UserMode.LIST_VIEW;
            switchFragment(targetMode);
        }
        if(targetMode == UserMode.DETAILS_VIEW) {
            userMode = UserMode.DETAILS_VIEW;
            switchFragment(targetMode);
        } else {
            //ignore
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(onEventServiceResult,
                new IntentFilter(EventService.EVENT_UPDATED_EVENT));
        bindToEventService();

        updateFragmentViewState(userMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(RESUME_TAG, "onResume: run");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindFromEventService();
    }

    private boolean switchFragment(UserMode targetMode){
        if(phoneMode == PhoneMode.PORTRAIT) {
            if (targetMode == UserMode.LIST_VIEW) {
                vpPager.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.GONE);
            } else if (targetMode == UserMode.DETAILS_VIEW) {
                vpPager.setVisibility(View.GONE);
                detailsContainer.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    private ServiceConnection eventServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            EventService.EventServiceBinder eventServiceBinder = (EventService.EventServiceBinder) service;
            eventService = eventServiceBinder.getService();
            isBound = true;
            events = eventService.getAllEvents();
            Fragment fragment = ((PageAdapter)vpPager.getAdapter()).getFragment(vpPager.getCurrentItem());
            if (fragment != null)
            {
                ((ForceUiUpdateInterface)fragment).updateEvents();
            }
            if(events!=null && events.size() != 0)
                eventDetails.setEvent(events.get(selectedEventIndex));
            Log.d(EVENT_TAG, "onServiceConnected: got events");
        }
    };


    public void bindToEventService(){
        bindService(new Intent(MainActivity.this,
                EventService.class), eventServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromEventService(){
        unbindService(eventServiceConnection);
    }

    private BroadcastReceiver onEventServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean result = intent.getBooleanExtra(EventService.GET_EVENT_TASK_RESULT, false);

            Log.d("MAIN", "onReceive: GetTask result received " + result);
            if (result) {
                events = eventService.getAllEvents();

                Fragment fragment = ((PageAdapter)vpPager.getAdapter()).getFragment(vpPager.getCurrentItem());
                if (fragment != null)
                {
                    ((ForceUiUpdateInterface)fragment).updateEvents();
                    for (int i = 0; i<3; i++)
                    {
                        Fragment fragment1 = ((PageAdapter)vpPager.getAdapter()).getFragment(i);
                        if (fragment1 != null)
                            ((ForceUiUpdateInterface)fragment1).stopSpinner();
                    }
                }

                if(phoneMode == PhoneMode.LANDSCAPE) {
                    eventDetails.setEvent(events.get(selectedEventIndex));
                }

                if(events == null)
                    Toast.makeText(getBaseContext(), "No new data", Toast.LENGTH_SHORT).show();
            }

        }
    };

}

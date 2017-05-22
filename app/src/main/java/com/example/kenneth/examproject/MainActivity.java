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
import com.example.kenneth.examproject.DatabaseHelpers.DatabaseHelper;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;
import com.example.kenneth.examproject.Services.EventService;

import java.util.List;


// sources:
// https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter

public class MainActivity extends AppCompatActivity implements EventSelectorInterface{

    @Override
    public void onEventSelected(int position) {

    }

    @Override
    public List<Event> getEventList() {
        return events;
    }

    @Override
    public Event getCurrentSelection() {
        return null;
    }

    private enum PhoneMode {PORTRAIT, LANDSCAPE}
    private enum UserMode {LIST_VIEW, DETAILS_VIEW}

    private static final String WEEK_FRAG = "week_fragment";
    private static final String MONTH_FRAG = "month_fragment";
    private static final String DAY_FRAG = "day_fragment";
    private static final String DETAILS_FRAG = "details_fragment";

    FragmentPagerAdapter adapterViewPager;


    private ServiceConnection eventServiceConnection;
    private EventService eventService;

    private DetailsFragment eventDetails;
    private List<Event> events;
    private DatabaseHelper db;

    private PhoneMode phoneMode;
    private UserMode userMode;

    private int selectedEventIndex;

    private ViewPager vpPager;
    private LinearLayout detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views);

        db = new DatabaseHelper(getApplicationContext());

        final ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        setupConnectionToWeatherService();
        startWeatherService();
        bindToWeatherService();

        vpPager = (ViewPager) findViewById(R.id.list_container);
        adapterViewPager = new PageAdapter(getSupportFragmentManager(), getApplicationContext());
        DayFragment dayfrag = (DayFragment) adapterViewPager.getItem(0);

        vpPager.setAdapter(adapterViewPager);

        detailsContainer = (LinearLayout)findViewById(R.id.details_container);

        //load Events from web
        //movies = new MovieLoader(this).getMovieList();

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

        } else {
            //got restarted, probably due to orientation change

            selectedEventIndex = savedInstanceState.getInt("event_position");
            userMode = (UserMode) savedInstanceState.getSerializable("user_mode");

            if (userMode == null) {
                userMode = UserMode.LIST_VIEW;  //default
            }
        }
            // Attach the page change listener inside the activity
            vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                // This method will be invoked when a new page becomes selected.
                @Override
                public void onPageSelected(int position) {
                    Toast.makeText(MainActivity.this,
                            "Selected page position: " + position, Toast.LENGTH_SHORT).show();
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



        //updateFragmentViewState(userMode);
    }

    @Override
    public void onBackPressed() {
        if(phoneMode == PhoneMode.LANDSCAPE){
            //finish on back press in landscape, go to search page
                finish();
        } else {
            if (userMode == UserMode.DETAILS_VIEW) {
                //go back to last used listview
                updateFragmentViewState(UserMode.LIST_VIEW);
            } else if (userMode == UserMode.LIST_VIEW) {
                //go to search activity from listview
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
        LocalBroadcastManager.getInstance(this).registerReceiver(onWeatherServiceResult,
                new IntentFilter(EventService.EVENT_UPDATED_EVENT));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindFromWeatherService();
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
    private void startWeatherService() {
        startService(new Intent(this, EventService.class));
    }

    private void stopWeatherService() {
        stopService(new Intent(getBaseContext(), EventService.class));
    }

    private void setupConnectionToWeatherService(){
        eventServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                //ref: http://developer.android.com/reference/android/app/Service.html
                eventService = ((EventService.EventServiceBinder)service).getService();
                Log.d("MAIN", "Weather service bound");

            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                //ref: http://developer.android.com/reference/android/app/Service.html
                eventService = null;
                Log.d("MAIN", "Weather service unbound");
            }
        };
    }

    private void bindToWeatherService(){
        bindService(new Intent(MainActivity.this,
                EventService.class), eventServiceConnection, Context.BIND_ABOVE_CLIENT);
    }

    private void unbindFromWeatherService(){
        unbindService(eventServiceConnection);
    }

    private BroadcastReceiver onWeatherServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean result = intent.getBooleanExtra(EventService.GET_EVENT_TASK_RESULT, false);

            /*if (firstUpdate)
            {
                events = eventService.getAllEvents();
                updateEvents();
                firstUpdate = false;
            }
*/
            Log.d("MAIN", "onReceive: GetWeatherTask result received " + result);
            if (result) {
                events = eventService.getAllEvents();
                updateEvents();
            }
            else
                Toast.makeText(getBaseContext(), "No new data", Toast.LENGTH_SHORT).show();
            //fab.setClickable(true);
        }
    };
    public void updateEvents(){

        if (events != null) {

            //dayView
            DayFragment dayfrag = (DayFragment) adapterViewPager.getItem(0);
            dayfrag.updateEvents();
            vpPager.setAdapter(adapterViewPager);
            //weekView
            //weekEventList.updateEvents();

            //monthView
            //monthEventList.updateEvents();

        }
        else
            Log.d("MAIN", "updateWeathers: weathers = null");

    }
}

package com.example.kenneth.examproject;

import android.content.res.Configuration;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kenneth.examproject.Adapters.PageAdapter;
import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;

import java.util.ArrayList;


// sources:
// https://github.com/codepath/android_guides/wiki/ViewPager-with-FragmentPagerAdapter

public class MainActivity extends AppCompatActivity implements EventSelectorInterface{

    @Override
    public void onEventSelected(int position) {

    }

    @Override
    public ArrayList<Event> getEventList() {
        return null;
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


    private UserMode lastViewState;
    private DayFragment dayEventList;
    private DetailsFragment eventDetails;
    private MonthFragment monthEventList;
    private WeekFragment weekEventList;
    private ArrayList<Event> events;

    private PhoneMode phoneMode;
    private UserMode userMode;

    private int selectedEventIndex;

    private LinearLayout listContainer;
    private LinearLayout detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views);

        ViewPager vpPager = (ViewPager) findViewById(R.id.list_container);
        adapterViewPager = new PageAdapter(getSupportFragmentManager(), getApplicationContext());
        vpPager.setAdapter(adapterViewPager);

        //listContainer = (LinearLayout)findViewById(R.id.list_container);
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

            dayEventList = new DayFragment();
            eventDetails = new DetailsFragment();
            monthEventList = new MonthFragment();
            weekEventList = new WeekFragment();

            //load events to lists
            //dayEventList.setMovies(movies);
            //eventDetails.setMovie(movies.get(selectedEventIndex));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details_container, eventDetails, DAY_FRAG)
                    .add(R.id.list_container, weekEventList, WEEK_FRAG)
                    .add(R.id.list_container, monthEventList, MONTH_FRAG)
                    .replace(R.id.list_container, dayEventList, DAY_FRAG)
                    .commit();
        } else {
            //got restarted, probably due to orientation change

            selectedEventIndex = savedInstanceState.getInt("event_position");
            userMode = (UserMode) savedInstanceState.getSerializable("user_mode");

            if(userMode == null){
                userMode = UserMode.LIST_VIEW;  //default
            }

            //check if FragmentManager already holds instance of Fragments
            dayEventList = (DayFragment) getSupportFragmentManager().findFragmentByTag(DAY_FRAG);
            if(dayEventList ==null){
                dayEventList = new DayFragment();
            }
            weekEventList = (WeekFragment)getSupportFragmentManager().findFragmentByTag(WEEK_FRAG);
            if(weekEventList ==null){
                weekEventList = new WeekFragment();
            }
            monthEventList = (MonthFragment) getSupportFragmentManager().findFragmentByTag(MONTH_FRAG);
            if(monthEventList ==null){
                monthEventList = new MonthFragment();
            }
            eventDetails = (DetailsFragment) getSupportFragmentManager().findFragmentByTag(DETAILS_FRAG);
            if(eventDetails ==null){
                eventDetails = new DetailsFragment();
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

        }

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

    private boolean switchFragment(UserMode targetMode){
        if(phoneMode == PhoneMode.PORTRAIT) {
            if (targetMode == UserMode.LIST_VIEW) {
                listContainer.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.GONE);
            } else if (targetMode == UserMode.DETAILS_VIEW) {
                listContainer.setVisibility(View.GONE);
                detailsContainer.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }
}

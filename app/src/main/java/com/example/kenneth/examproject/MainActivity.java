package com.example.kenneth.examproject;

//rimelig nice forsøg

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.kenneth.examproject.Interfaces.EventSelectorInterface;
import com.example.kenneth.examproject.Models.Event;

import java.util.ArrayList;

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

    public enum PhoneMode {PORTRAIT, LANDSCAPE}
    public enum UserMode {WEEK_VIEW, MONTH_VIEW, DAY_VIEW, DETAILS_VIEW}

    private static final String WEEK_FRAG = "week_fragment";
    private static final String MONTH_FRAG = "month_fragment";
    private static final String DAY_FRAG = "day_fragment";
    private static final String DETAILS_FRAG = "details_fragment";


    private UserMode lastViewState;
    private DayFragment dayEventList;
    private DetailsFragment eventDetails;
    private MonthFragment monthEventList;
    private WeekFragment weekEventList;
    private ArrayList<Event> events;

    private PhoneMode phoneMode;
    private UserMode userMode;

    private int selectedMovieIndex;

    private LinearLayout listContainer;
    private LinearLayout detailsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views);

        listContainer = (LinearLayout)findViewById(R.id.list_container);
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

            selectedMovieIndex = 0;
            userMode = UserMode.WEEK_VIEW;

            dayEventList = new DayFragment();
            eventDetails = new DetailsFragment();
            monthEventList = new MonthFragment();
            weekEventList = new WeekFragment();

            //load events to lists
            //dayEventList.setMovies(movies);
            //eventDetails.setMovie(movies.get(selectedMovieIndex));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details_container, eventDetails, DAY_FRAG)
                    .add(R.id.list_container, weekEventList, WEEK_FRAG)
                    .add(R.id.list_container, monthEventList, MONTH_FRAG)
                    .replace(R.id.list_container, dayEventList, DAY_FRAG)
                    .commit();
        } else {
            //got restarted, probably due to orientation change

            selectedMovieIndex = savedInstanceState.getInt("movie_position");
            userMode = (UserMode) savedInstanceState.getSerializable("user_mode");
            lastViewState = (UserMode) savedInstanceState.getSerializable("last_user_mode");

            if(userMode == null){
                userMode = UserMode.DAY_VIEW;  //default
                lastViewState = UserMode.DAY_VIEW;
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

        }

        updateFragmentViewState(userMode);
    }

    @Override
    public void onBackPressed() {
        if(phoneMode == PhoneMode.LANDSCAPE){
            //finish on back press in landscape, go to search page
                finish();
        } else {
            if (userMode == UserMode.DETAILS_VIEW) {
                //go back to last used listview
                updateFragmentViewState(lastViewState);
            } else if (userMode == UserMode.DAY_VIEW) {
                //go to search activity from listview
                finish();
            } else if (userMode == UserMode.WEEK_VIEW){
                //go to search activity from listview
                finish();
            } else if (userMode == UserMode.MONTH_VIEW){
                //go to search activity from listview
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("movie_position", selectedMovieIndex);
        outState.putSerializable("user_mode", userMode);
        outState.putSerializable("last_user_mode", lastViewState);
        super.onSaveInstanceState(outState);
    }

    private void updateFragmentViewState(UserMode targetMode){

        if(targetMode == UserMode.DAY_VIEW) {
            userMode = UserMode.DAY_VIEW;
            lastViewState = UserMode.DAY_VIEW;
            switchFragment(targetMode);
        } if(targetMode == UserMode.WEEK_VIEW) {
            userMode = UserMode.WEEK_VIEW;
            lastViewState = UserMode.WEEK_VIEW;
            switchFragment(targetMode);
        } if(targetMode == UserMode.MONTH_VIEW) {
            userMode = UserMode.MONTH_VIEW;
            lastViewState = UserMode.MONTH_VIEW;
            switchFragment(targetMode);
        } if(targetMode == UserMode.DETAILS_VIEW) {
            userMode = UserMode.DETAILS_VIEW;
            switchFragment(targetMode);
        } else {
            //ignore
        }

    }

    private boolean switchFragment(UserMode targetMode){
        if(phoneMode == PhoneMode.PORTRAIT) {
            if (targetMode == UserMode.DAY_VIEW) {
                listContainer.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.GONE);
                changeListContainerFragment(UserMode.DAY_VIEW);
            } else if (targetMode == UserMode.WEEK_VIEW) {
                listContainer.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.GONE);
                changeListContainerFragment(targetMode);
            } else if (targetMode == UserMode.MONTH_VIEW) {
                listContainer.setVisibility(View.VISIBLE);
                detailsContainer.setVisibility(View.GONE);
                changeListContainerFragment(targetMode);
            } else if (targetMode == UserMode.DETAILS_VIEW) {
                listContainer.setVisibility(View.GONE);
                detailsContainer.setVisibility(View.VISIBLE);
                changeListContainerFragment(targetMode);
            }
        } else {
                changeListContainerFragment(targetMode);
            }
        return true;
    }

    @SuppressWarnings("ResourceType")
    private void changeListContainerFragment(UserMode targetMode){
        switch(targetMode) {
            case DAY_VIEW:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                        .replace(R.id.list_container, dayEventList, DAY_FRAG)
                        .commit();
                break;

            case WEEK_VIEW:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                        .replace(R.id.list_container, weekEventList, WEEK_FRAG)
                        .commit();
                break;

            case MONTH_VIEW:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.slide_in, R.animator.slide_out)
                        .replace(R.id.list_container, monthEventList, MONTH_FRAG)
                        .commit();
                break;
        }
    }
}

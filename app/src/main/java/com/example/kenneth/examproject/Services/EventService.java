package com.example.kenneth.examproject.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.kenneth.examproject.DatabaseHelpers.DatabaseHelper;
import com.example.kenneth.examproject.Models.Event;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EventService extends Service {

    public static final String EVENT_UPDATED_EVENT = "EventUpdated-event";
    public static final String GET_EVENT_TASK_RESULT = "GetEventTaskResult";
    private final static String EVENTSERVICE_TAG = "EVENT SERVICE";
    private final static int INTERVAL = 1000 * 60 * 30;
    private final static int TEST_INTERVAL = 10000;

    public DatabaseHelper database;

    private boolean started = false;
    private Timer timer;


    public class EventServiceBinder extends Binder {

        public EventService getService() {
            return EventService.this;
        }
    }

    private final IBinder binder = new EventServiceBinder();

    public EventService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        database = DatabaseHelper.getInstance(this);
        Log.d(EVENTSERVICE_TAG, "onCreate: EventService Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent!=null) {
            if(!started){
                started = true;
                scheduleEventTask();
                Log.d(EVENTSERVICE_TAG, "onStartCommand: Service Started");
            }
            else {
                getEventTask task = new getEventTask(getBaseContext(), database);
                task.execute();
            }
        }
        else {
            Log.d(EVENTSERVICE_TAG, "onStartCommand: Service already running");
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        started = false;
        timer.cancel();
        Log.d(EVENTSERVICE_TAG, "onDestroy: Service Stopped");
    }

    public List<Event> getAllEvents(){
        return database.getAllEvents();
    }

    // http://stackoverflow.com/questions/6531950/how-to-execute-async-task-repeatedly-after-fixed-time-intervals
    private void scheduleEventTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            getEventTask task = new getEventTask(getBaseContext(), database);
                            task.execute();

                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, INTERVAL);
    }

}

class getEventTask extends AsyncTask<Void,Void,Void> {

    private static final String GET_EVENT_TASK_TAG = "GetEventTask";
    private static final String City = "Aarhus";

    private DatabaseHelper _database;
    private Context _context;
    private JSONObject data = null;

    private List<String> IDList = new ArrayList<String>();
    private List <String> eventIDs = new ArrayList <String>();

    public getEventTask(Context context, DatabaseHelper database){
        this._context = context;
        this._database = database;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(GET_EVENT_TASK_TAG, "doInBackground: Fetching data from facebook");
        try {
            getEvents();
        } catch (Exception e) {
            System.out.println("Exception "+ e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void Void) {
        super.onPostExecute(Void);
            try
            {

                broadcastTaskResult(true);
                return;

            } catch (Exception e) {

                System.out.println("Exception "+ e.getMessage());
            }
    }

    private void broadcastTaskResult(Boolean result){
        Log.d(GET_EVENT_TASK_TAG, "broadcastTaskResult: Broadcasting result");

        Intent intent = new Intent(EventService.EVENT_UPDATED_EVENT);
        intent.putExtra(EventService.GET_EVENT_TASK_RESULT, result);
        LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
    }
    protected void getEvents()
    {
        Bundle params = new Bundle(3);
        params.putString("type", "place");
        params.putString("center", "56.162939,10.203921");
        params.putString("distance", "5000");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "search",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response != null)
                        {
                            try
                            {
                                JSONArray data = response.getJSONObject().getJSONArray("data");

                                for (int i = 0; i<data.length(); i++)
                                    IDList.add(data.getJSONObject(i).getString("id"));

                                Log.d("onCompleted", "got ID of places");

                                getEventIDs();
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }

    protected void getEventIDs()
    {
        long unixTime = System.currentTimeMillis() / 1000L;
        final Bundle params = new Bundle(2);
        params.putString("ids", TextUtils.join(",", IDList));
        params.putString("fields", "events.fields(name,id).since(" + unixTime + ")");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback(){
                    public void onCompleted(GraphResponse response) {
                        if (response != null)
                        {
                            try
                            {
                                JSONObject ID = response.getJSONObject();
                                JSONArray dataArray;

                                for (int i = 0; i<IDList.size(); i++)
                                {
                                    if (ID.getJSONObject(IDList.get(i)).has("events")) {
                                        dataArray = ID.getJSONObject(IDList.get(i)).getJSONObject("events").getJSONArray("data");

                                        for (int j = 0; j < dataArray.length(); j++)
                                            eventIDs.add(dataArray.getJSONObject(j).getString("id"));
                                    }
                                }

                                Log.d("onCompleted", "got event IDs");

                                getEventData();
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }

    protected void getEventData()
    {
        final Bundle params = new Bundle(1);
        params.putString("ids", TextUtils.join(",", eventIDs));

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback(){
                    public void onCompleted(GraphResponse response) {
                        if (response != null)
                        {
                            try
                            {
                                Event event = new Event();

                                String description = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("description");
                                String start_time = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("start_time");
                                String end_time = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("end_time");
                                String name = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("name");
                                double latitude = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getDouble("latitude");
                                double longitude = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getDouble("longitude");
                                String address;

                                if (response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").has("street")) {
                                    address = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getString("street");
                                    event.setAddress(address);
                                }

                                event.setDescrition(description);
                                event.setStartTime(start_time);
                                event.setEndTime(end_time);
                                event.setName(name);
                                event.setLatitude(latitude);
                                event.setLongitude(longitude);
                                _database.addEvent(event);

                                Log.d("onCompleted", "got event data");
                            }
                            catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }
}

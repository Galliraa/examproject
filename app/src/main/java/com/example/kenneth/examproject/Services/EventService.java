package com.example.kenneth.examproject.Services;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.kenneth.examproject.DatabaseHelpers.DatabaseHelper;
import com.example.kenneth.examproject.Models.Event;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class EventService extends Service {

    public static final String EVENT_UPDATED_EVENT = "EventUpdated-event";
    public static final String GET_EVENT_TASK_RESULT = "GetEventTaskResult";
    private final static String EVENTSERVICE_TAG = "EVENT SERVICE";
    private final static int INTERVAL = 1000 * 60 * 30;
    private final static int TEST_INTERVAL = 10000;

    public List<Event> events;

    public DatabaseHelper database;

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


                scheduleEventTask();
                Log.d(EVENTSERVICE_TAG, "onStartCommand: Service Started");
                events = new Vector<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        Log.d(EVENTSERVICE_TAG, "onDestroy: Service Stopped");
    }

    public List<Event> getAllEvents(){
        return events;
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
                            database.deleteAll();
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

    class getEventTask extends AsyncTask<Void,Void,Void> {

        private static final String GET_EVENT_TASK_TAG = "GetEventTask";
        private static final String City = "Aarhus";

        private DatabaseHelper _database;
        private Context _context;
        private JSONObject data = null;

        private List<String> IDList = new ArrayList<String>();
        private List <String> eventIDs = new ArrayList <String>();
        private List <String> imageURLs = new ArrayList <String>();

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

                events = _database.getAllEvents();
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
            params.putString("center", "37.76,-122.427"); //(center, latitude, longitude)
            params.putString("distance", "5000");
            params.putString("limit", "100");

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
                                        //getEventData();

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
            for(int f = 0; f < ((IDList.size()+49)/50); f++) {
                final List<String> IDs = IDList.subList(f * 50, ((f + 1) * 50));
                long unixTime = System.currentTimeMillis() / 1000L;
                final Bundle params = new Bundle(2);
                params.putString("ids", TextUtils.join(",", IDs));
                params.putString("fields", "events.fields(name,id,cover).since(" + unixTime + ")");

                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/",
                        params,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        JSONObject ID = response.getJSONObject();
                                        JSONArray dataArray;

                                        for (int i = 0; i < (IDs.size()); i++) {
                                            if (ID.getJSONObject(IDs.get(i)).has("events")) {
                                                dataArray = ID.getJSONObject(IDs.get(i)).getJSONObject("events").getJSONArray("data");

                                                for (int j = 0; j < dataArray.length(); j++) {
                                                    eventIDs.add(dataArray.getJSONObject(j).getString("id"));
                                                    if (dataArray.getJSONObject(j).has("cover"))
                                                        imageURLs.add(dataArray.getJSONObject(j).getJSONObject("cover").getString("source"));
                                                    else
                                                        imageURLs.add(null);
                                                }
                                            }
                                        }

                                        Log.d("onCompleted", "got event IDs");

                                        getEventData();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                ).executeAsync();
            }
        }

        protected void getEventData() {
            for (int f = 0; f < ((eventIDs.size() + 49) / 50); f++) {
                int j = 0;
                if(f == ((eventIDs.size() + 49) / 50)-1)
                {
                    j = 50-(eventIDs.size()%50);
                }

                final List<String> event50IDs = eventIDs.subList(f * 50, ((f + 1) * 50-j));

                final Bundle params = new Bundle(1);
                params.putString("ids", TextUtils.join(",", event50IDs));

                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/",
                        params,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        for (int i = 0; i < event50IDs.size(); i++) {
                                            Event event = new Event();

                                            String description = response.getJSONObject().getJSONObject(event50IDs.get(i)).getString("description");
                                            String start_time = response.getJSONObject().getJSONObject(event50IDs.get(i)).getString("start_time");
                                            String end_time = response.getJSONObject().getJSONObject(event50IDs.get(i)).getString("end_time");
                                            String name = response.getJSONObject().getJSONObject(event50IDs.get(i)).getString("name");
                                            double latitude = response.getJSONObject().getJSONObject(event50IDs.get(i)).getJSONObject("place").getJSONObject("location").getDouble("latitude");
                                            double longitude = response.getJSONObject().getJSONObject(event50IDs.get(i)).getJSONObject("place").getJSONObject("location").getDouble("longitude");
                                            String address;

                                            if (response.getJSONObject().getJSONObject(event50IDs.get(i)).getJSONObject("place").getJSONObject("location").has("street")) {
                                                address = response.getJSONObject().getJSONObject(event50IDs.get(i)).getJSONObject("place").getJSONObject("location").getString("street");
                                                event.setAddress(address);
                                            }

                                            event.setDescrition(description);
                                            event.setStartTime(start_time);
                                            event.setEndTime(end_time);
                                            event.setName(name);
                                            event.setLatitude(latitude);
                                            event.setLongitude(longitude);
                                            event.setEventImage(imageURLs.get(i));
                                            _database.addEvent(event);
                                        }
                                        Log.d("onCompleted", "got event data");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                ).executeAsync();
            }
        }
    }
}




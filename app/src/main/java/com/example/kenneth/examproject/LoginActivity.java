package com.example.kenneth.examproject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.kenneth.examproject.Services.EventService;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final int VIEW_REQUEST_CODE = 2;

    private ServiceConnection eventServiceConnection;

    private CallbackManager callbackManager;

    private TextView info;
    private EditText cityText;
    private LoginButton loginButton;
    private Button searchCity;
    private Button searchLocation;
    private SeekBar slider;
    private float distance = 5;

    private EventService eventService;

    private KeyListener cityTextKeyListener;

    private RequestQueue mRequestQueue;

    private double lat;
    private double lng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();


        setContentView(R.layout.activity_login);

        searchCity = (Button) findViewById(R.id.cityButton);
        searchLocation = (Button) findViewById(R.id.locationButton);
        cityText = (EditText) findViewById(R.id.cityText);
        cityTextKeyListener = cityText.getKeyListener();
        info = (TextView) findViewById(R.id.info);
        slider = (SeekBar) findViewById(R.id.seekBar);
        loginButton = (LoginButton) findViewById(R.id.login_button);


        // https://developer.android.com/training/volley/requestqueue.html
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());
        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);
        // Start the queue
        mRequestQueue.start();


        if (AccessToken.getCurrentAccessToken() == null) {
            disableViews();
        }



        slider.setProgress(45);
        slider.setMax(90);
        info.setText(getString(R.string.slider_distance) + ": " + distance + " km");

        //https://code.tutsplus.com/tutorials/quick-tip-add-facebook-login-to-your-android-app--cms-23837
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
            enableViews();
            }
            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
            }
        });




        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = (float) progress/10 + (float) 1;
                info.setText(getString(R.string.slider_distance) + ": " + String.valueOf(distance) + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        searchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    searchCityFunc();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        });
        searchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocationFunc();
            }
        });
    }

    private void searchCityFunc() throws IllegalAccessException, InstantiationException {
        final Intent i = new Intent(this, MainActivity.class);
        //initiate service to search by city name here....

        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + cityText.getText();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try
                {
                    if (response != null)
                    {
                        JSONObject data = response.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                        lat = data.getDouble("lat");
                        lng = data.getDouble("lng");

                        info.setText(String.valueOf(lat) + "," + String.valueOf(lng));
                    }

                    setupConnectionToEventService();
                    bindToEventService(lat, lng, distance);

                    startActivityForResult(i, VIEW_REQUEST_CODE);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        mRequestQueue.add(jsonObjectRequest);
    }

    private void searchLocationFunc() {
        Intent i = new Intent(this, MainActivity.class);
        //initiate service to search by current location here...
        setupConnectionToEventService();
        bindToEventService(lat, lng, distance);
        startActivityForResult(i, VIEW_REQUEST_CODE);
    }

    private void enableViews() {
        searchCity.setEnabled(true);
        searchLocation.setEnabled(true);
        cityText.setKeyListener(cityTextKeyListener);
        cityText.setEnabled(true);
        slider.setEnabled(true);
    }

    private void disableViews() {
        searchCity.setEnabled(false);
        searchLocation.setEnabled(false);
        cityText.setKeyListener(null);
        cityText.setEnabled(false);
        slider.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                unbindFromEventService();
            }
        }
    }
    public void setupConnectionToEventService(){
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

    public void bindToEventService(double lat, double lng, float distance){

        Intent i = new Intent(LoginActivity.this, EventService.class);

        i.putExtra("lat", lat);
        i.putExtra("lng", lng);
        i.putExtra("distance", distance);
        bindService(i, eventServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromEventService(){
        unbindService(eventServiceConnection);
    }
}


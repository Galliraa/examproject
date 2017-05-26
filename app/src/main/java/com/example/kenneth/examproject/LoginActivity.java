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
import android.widget.TextView;

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

    private ServiceConnection eventServiceConnection;
    private EventService eventService;

    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

    private TextView info;
    private EditText cityText;
    private LoginButton loginButton;
    private Button searchCity;
    private Button searchLocation;

    private KeyListener cityTextKeyListener;

    List<String> IDList = new ArrayList<String>();
    List<String> eventIDs = new ArrayList<String>();

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

        if (AccessToken.getCurrentAccessToken() == null) {
            disableViews();
        }

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    disableViews();
                } else {
                    enableViews();
                }
            }
        };

        info = (TextView) findViewById(R.id.info);

        loginButton = (LoginButton) findViewById(R.id.login_button);


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                info.setText(
                        "User ID: "
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                "Auth Token: "
                                + loginResult.getAccessToken().getToken()
                );
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");
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
        Intent i = new Intent(this, MainActivity.class);
        //initiate service to search by city name here....
        setupConnectionToEventService();
        bindToEventService();

        startActivity(i);
    }

    private void searchLocationFunc() {
        Intent i = new Intent(this, MainActivity.class);
        //initiate service to search by current location here...

        startActivity(i);
    }

    private void enableViews() {
        searchCity.setEnabled(true);
        searchLocation.setEnabled(true);
        cityText.setKeyListener(cityTextKeyListener);
        cityText.setEnabled(true);
    }

    private void disableViews() {
        searchCity.setEnabled(false);
        searchLocation.setEnabled(false);
        cityText.setKeyListener(null);
        cityText.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    public void bindToEventService(){
        bindService(new Intent(LoginActivity.this,
                EventService.class), eventServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromEventService(){
        unbindService(eventServiceConnection);
    }
}


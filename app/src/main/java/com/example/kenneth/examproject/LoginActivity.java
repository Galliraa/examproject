package com.example.kenneth.examproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
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

    private CallbackManager callbackManager;

    private TextView info;
    private LoginButton loginButton;
    List<String> IDList = new ArrayList<String>();
    List <String> eventIDs = new ArrayList <String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);
        setContentView(R.layout.activity_login);
        info = (TextView)findViewById(R.id.info);
        loginButton = (LoginButton)findViewById(R.id.login_button);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
                                String description = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("description");
                                String start_time = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("start_time");
                                String end_time = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("end_time");
                                String name = response.getJSONObject().getJSONObject(eventIDs.get(1)).getString("name");
                                double latitude = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getDouble("latitude");
                                double longitude = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getDouble("longitude");
                                String adrress;

                                if (response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").has("street"))
                                    adrress = response.getJSONObject().getJSONObject(eventIDs.get(1)).getJSONObject("place").getJSONObject("location").getString("street");


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
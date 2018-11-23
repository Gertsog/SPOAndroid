package com.android.example.weatherapp;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    SQLiteDatabase db = MainActivity.db;
    String url = MainActivity.url;
    int i = 0;

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Service has stopped", Toast.LENGTH_SHORT).show();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        final int time = intent.getIntExtra("time", 0);
        Toast.makeText(getApplicationContext(), "Service has started", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            public void run() {
                for (i = 0; i < 10; i++)
                {
                    getData();
                    if (i == 10)
                    {
                        stopSelf();
                    }
                    try {
                        TimeUnit.SECONDS.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void getData ()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject location = response.getJSONObject("location");
                        String city = location.getString("name");
                        JSONObject forecast = response.getJSONObject("forecast");
                        JSONArray forecastday = forecast.getJSONArray("forecastday");
                        JSONObject oneday = forecastday.getJSONObject(i);

                        String date = oneday.getString("date");
                        JSONObject day = oneday.getJSONObject("day");
                        double temp = day.getDouble("avgtemp_c");
                        double wind = day.getDouble("maxwind_kph");
                        double humidity = day.getDouble("avghumidity");
                        JSONObject condition = day.getJSONObject("condition");
                        String text = condition.getString("text");
                        db.execSQL("INSERT INTO weather VALUES ('" + city + "','" + date + "'," + temp + "," + wind + "," + humidity + ",'" + text + "');");
                        Toast.makeText(getApplicationContext(), "В базу добавлена запись", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
        requestQueue.add(request);
    }
}

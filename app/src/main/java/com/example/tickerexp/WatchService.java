package com.example.tickerexp;

import static androidx.core.content.ContextCompat.getSystemService;

import static com.example.tickerexp.CryptoConfig.CHANNEL_ID;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayDeque;
import java.util.Deque;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.os.VibrationEffect;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class WatchService extends Service {
    private HistoryCollection historyCollection = new HistoryCollection();
    private static final String CHANNEL_ID = "PRICE_ALERTS";
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private Context context;
    private static WatchService instance;
    String[] cryptoNames = CryptoConfig.CRYPTO_NAMES;

    private final String url = "https://api.binance.com/api/v3/ticker/price";
    private boolean hasVibrated = false;

    public static WatchService getInstance() {
        return instance;
    }

    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WatchService", "onStartCommand: Start");
        // Start a background thread for continuous price monitoring


        Thread backgroundThread = new Thread(() -> {
            startPriceMonitoring();
        });
        backgroundThread.start();

        // Create the Foreground Service Notification
        Notification notification = createMainNotification();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);

        instance = this;

        // Return a value based on your needs
        return START_STICKY; // or other options like START_NOT_STICKY, START_REDELIVER_INTENT
    }

    private Notification createMainNotification() {
        // Create a Notification Channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CryptoConfig.CONST_CHANNEL_ID,
                    "Foreground Service",
                    NotificationManager.IMPORTANCE_LOW // Change IMPORTANCE_DEFAULT to IMPORTANCE_LOW
            );
            channel.setSound(null, null); // Désactivez le son
            channel.enableVibration(false); // Désactivez la vibration

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create and return the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CryptoConfig.CONST_CHANNEL_ID)
                .setContentTitle("Watching tickers")
                .setPriority(NotificationCompat.PRIORITY_LOW) // Change PRIORITY_DEFAULT to PRIORITY_LOW
                .setSound(null) // Désactivez le son pour les versions inférieures à Android 8.0
                .setVibrate(new long[]{0L}); // Désactivez la vibration pour les versions inférieures à Android 8.0

        // You can add additional actions, styles, etc., to the notification if needed

        return builder.build();
    }


    private void startPriceMonitoring() {
        Log.d("WatchService", "startPriceMonitoring: Start");
        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.binance.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        Log.d("WatchService", "Retrofit: Initialized");

        BinanceService service = retrofit.create(BinanceService.class);
        Log.d("WatchService", "BinanceService: Created");

        new Thread(() -> {
            Log.d("WatchService", "Thread: Start");

            while (true) {

                service.getAllPrices(url).enqueue(new Callback<List<AssetPrice>>() {
                    @Override
                    public void onResponse(Call<List<AssetPrice>> call, Response<List<AssetPrice>> response) {

                        if (response.body() != null) {
                            historyCollection.addCurrentValues(response.body());
                        }else{
                            Log.d("WatchService", "onResponse: Null response");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<AssetPrice>> call, Throwable t) {
                        Log.d("WatchService", "onFailure: Failed");
                    }

                });

                try {
                    Thread.sleep(CryptoConfig.FETCH_INTERVAL); //1 second
                } catch (InterruptedException e) {
                    Log.e("WatchService", "Thread Interrupted", e);
                }
            }
        }).start();
    }

    private final IBinder binder = (IBinder) new LocalBinder();

    // Class used for the client Binder.
    public class LocalBinder extends Binder {
        WatchService getService() {
            return WatchService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

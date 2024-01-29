package com.example.tickerexp;

import static androidx.core.content.ContextCompat.getSystemService;

import static com.example.tickerexp.CryptoConfig.CHANNEL_ID;

import static java.lang.Integer.parseInt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.ArrayDeque;
import java.util.Deque;

public class AssetHistory {
    private String name;
    private long notificationId;
    private boolean isNotificationUp;
    private long lastNotificationTimestamp;
    private int windowLength;
    public int minWindowLength;
    public Pair<Double, Double> lastVariations;

    private Deque<AssetPrice> history = new ArrayDeque<AssetPrice>();

    private AssetHistory(){}

    public long getLastNotificationTimestamp() {
        return lastNotificationTimestamp;
    }

    public void setLastNotificationTimestamp() {
        this.lastNotificationTimestamp = System.currentTimeMillis();
    }

    public AssetHistory(String name, int windowLength) {
        this.name = name;
        this.windowLength = windowLength;
        this.minWindowLength = (int)(0.2*windowLength);
        this.isNotificationUp = false;
        this.notificationId = System.currentTimeMillis();
        this.lastNotificationTimestamp = 0;
    }

    public String getName() {
        return name;
    }

    public long getNotificationId() {
        return notificationId;
    }

    public boolean isNotificationUp() {
        return isNotificationUp;
    }

    private void setNotificationUp(boolean notificationUp) {
        isNotificationUp = notificationUp;
    }

    public Deque<AssetPrice> getHistory() {
        return history;
    }

    public void addPrice(AssetPrice assetPrice){
        this.history.addLast(assetPrice);

        if (this.history.size() > this.windowLength) {
            this.history.removeFirst();
        }

        if(this.history.size() > this.minWindowLength) {
            Pair<Double, Double> downUpVariations = checkBehavior();
            lastVariations = downUpVariations;

            if (Math.max(Math.abs(lastVariations.getFirst()), Math.abs(lastVariations.getSecond())) >= CryptoConfig.VARIATION_THRESHOLD) {
                if (System.currentTimeMillis() - lastNotificationTimestamp >= CryptoConfig.NOTIFICATION_INTERVAL) {
                    if (isNotificationUp) {
                        AppNotification.deleteNotification(notificationId);
                    }

                    String variationString = Math.abs(lastVariations.getFirst()) > Math.abs(lastVariations.getSecond()) ? (String.format("%.2f", lastVariations.getFirst() * 100) + "% ") :  (String.format("%.2f", lastVariations.getSecond() * 100) + "%");

                    notificationId = AppNotification.sendNotification("Price variation !", assetPrice.getSymbol() + " has changed " + variationString + " in under " + String.format("%.0f", ((double)(CryptoConfig.WINDOW_LENGTH * CryptoConfig.FETCH_INTERVAL)/(1000*60))) + " minutes.\n Current price is now " + assetPrice.getPrice());
                    AppNotification.vibrate();
                    setNotificationUp(true);
                    setLastNotificationTimestamp();
                }
            }
        }
    }

    public Pair<AssetPrice, AssetPrice> getHistoryMinMax() {
        if (history.isEmpty()) {
            return new Pair<>(); // Retourne null ou lancez une exception si la deque est vide
        }

        AssetPrice maxObject = history.peek(); // Commencez avec le premier objet
        AssetPrice minObject = history.peek(); // Commencez avec le premier objet

        for (AssetPrice obj : history) {
            if (obj.getPrice() > maxObject.getPrice()) {
                maxObject = obj;
            }

            if (obj.getPrice() < minObject.getPrice()) {
                minObject = obj;
            }
        }


        return new Pair<AssetPrice, AssetPrice>(minObject, maxObject);
    }

    public Pair<Double, Double> checkBehavior() {
        Pair<AssetPrice, AssetPrice> minMaxPrices = getHistoryMinMax();
        double newPrice = history.peekLast().getPrice();

        double upVariation = (newPrice - minMaxPrices.getFirst().getPrice()) / minMaxPrices.getFirst().getPrice();
        double downVariation = (newPrice - minMaxPrices.getSecond().getPrice()) / minMaxPrices.getSecond().getPrice();


        return new Pair<>(downVariation, upVariation);
    }

}

package com.example.tickerexp;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryCollection {
    private Map<String, AssetHistory> histories = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Pair<AssetHistory, AssetHistory> getDownUpVariations(){
        Pair<AssetHistory, AssetHistory> downUp = new Pair<>();
        histories.forEach((key, value) -> {
            if(downUp.getFirst() == null || value.lastVariations.getFirst() < downUp.getFirst().lastVariations.getFirst()){
                downUp.setFirst(value);
            }

            if(downUp.getSecond() == null || value.lastVariations.getSecond() > downUp.getSecond().lastVariations.getSecond()){
                downUp.setSecond(value);
            }
        });

        return downUp;
    };

    private String generateNotificationMessage(Pair<AssetHistory, AssetHistory> downUp){
        if(downUp.getFirst() != null && downUp.getSecond() != null){
            String downVariationName = downUp.getFirst().getName();
            double downVariation = downUp.getFirst().lastVariations.getFirst();
            String upVariationName = downUp.getSecond().getName();
            double upVariation = downUp.getSecond().lastVariations.getSecond();


            return "Max up variation " + upVariationName + " : " + String.format("%.2f", upVariation * 100) + "%\nMin down variation " + downVariationName + " : " + String.format("%.2f", downVariation * 100) + "%";
        }else{
            return "Fetching variations.";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addCurrentValues(List<AssetPrice> assetPriceList){

        int currentPrices  = 0;

        for (AssetPrice assetPrice : assetPriceList) {

            String assetSymbol = assetPrice.getSymbol();

            if (Utils.isStringInArray(CryptoConfig.CRYPTO_NAMES, assetSymbol)){

                double newPrice = assetPrice.getPrice();

                if(!histories.containsKey(assetSymbol)){
                    histories.put(assetSymbol, new AssetHistory(assetSymbol, CryptoConfig.WINDOW_LENGTH));
                }

                AssetHistory assetHistory = histories.get(assetSymbol);
                assetHistory.addPrice(assetPrice);

                currentPrices = assetHistory.getHistory().size();

                if(assetSymbol.equals("BTCUSDT")) {
                    Log.d("HistoryCollection", "HistoryCollection size : " + assetHistory.getHistory().size() + " Element : Price " + newPrice + " for pair " + assetPrice.getSymbol() + " timestamp : " + assetPrice.getTimestamp());
                }
            }
        }

        if(currentPrices >= CryptoConfig.WINDOW_LENGTH) {
            AppNotification.updateNotification(CryptoConfig.FOREGROUND_NOTIFICATION_ID, "Watching tickers", generateNotificationMessage(getDownUpVariations()));
        }else{
            AppNotification.updateNotification(CryptoConfig.FOREGROUND_NOTIFICATION_ID, "Watching tickers", "Loading : " + String.format("%.2f", ((double)currentPrices)/((double)CryptoConfig.WINDOW_LENGTH) * 100) + "%");
        }
    }
}

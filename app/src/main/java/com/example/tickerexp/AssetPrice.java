package com.example.tickerexp;

public class AssetPrice {
    private String symbol;
    private double price;

    private long timestamp;

    public AssetPrice(){
        timestamp = System.currentTimeMillis();
    }
    // Getters et setters

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getTimestamp(){return timestamp; }
}
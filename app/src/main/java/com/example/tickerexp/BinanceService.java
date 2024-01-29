package com.example.tickerexp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

import java.util.List;

public interface BinanceService {
    @GET
    Call<List<AssetPrice>> getAllPrices(@Url String url);
}



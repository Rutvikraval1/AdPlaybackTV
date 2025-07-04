package com.example.playback_tv.tv.network;

import com.example.playback_tv.tv.model.AdManifest;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("manifest")
    Call<AdManifest> getAdManifest(@Query("location") String location);
    
    @GET("manifest")
    Call<AdManifest> getAdManifestWithDevice(@Query("location") String location, 
                                            @Query("device_id") String deviceId);
}
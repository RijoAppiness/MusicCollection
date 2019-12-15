package com.example.musiccollection.repository.network_data

import com.example.musiccollection.repository.models.Data
import com.example.musiccollection.repository.models.Song
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface SongsAPI{
    @GET(SONGS_API_END_POINT)
    fun getSongs(): Call<Data>
}
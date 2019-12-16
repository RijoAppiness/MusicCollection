package com.example.musiccollection.repository.network_data

import android.content.Context
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APIservice{
    companion object {
        private var instance:SongsAPI?=null
        fun getSongsAPI():SongsAPI?{
            if(instance==null){
              val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(
                  SONGS_API_BASE_URL).build()
                instance = retrofit.create(SongsAPI::class.java)
                return instance
            }
            else
                return instance
        }
    }

}
package com.example.musiccollection.repository.songs_repository

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.musiccollection.repository.local_data.DatabaseInteractor
import com.example.musiccollection.repository.local_data.songsDao
import com.example.musiccollection.repository.models.Data
import com.example.musiccollection.repository.models.Song
import com.example.musiccollection.repository.network_data.APIservice
import com.example.musiccollection.repository.network_data.NETWORK_ERROR
import com.example.musiccollection.repository.network_data.NETWORK_NOT_AVAILABLE
import com.example.musiccollection.repository.network_data.SERVER_ERROR
import retrofit2.Call
import retrofit2.Response

class SongsRepository(context: Context) {
    var networkAvailable = false
        set(value) {
            field = value
            if (value) {
                loadSongs()
            }
        }
    private val songDao = DatabaseInteractor.getInstance(context)?.getDao()

    val requestForArtists = MutableLiveData<Boolean>()
    val requestForAlbums = MutableLiveData<Boolean>()
    val requestSongsOnAlbum = MutableLiveData<String>()
    val requestSongsByArtist = MutableLiveData<String>()
    val networkError: MutableLiveData<Int> = MutableLiveData()

    val albums = Transformations.switchMap(requestForAlbums) {
        songDao?.getAlbums()
    }
    val artists = Transformations.switchMap(requestForArtists) {
        songDao?.getArtist()
    }
    val songsByArtist = Transformations.switchMap(requestSongsByArtist) {
        songDao?.getSongsByArtist(it)
    }
    val songsOnAlbum = Transformations.switchMap(requestSongsOnAlbum) {
       songDao?.getSongsOnAlbum(it)
    }


    fun loadSongs() {
        if (networkAvailable) {
            val call = APIservice.getSongsAPI()?.getSongs()
            call?.enqueue(object : retrofit2.Callback<Data> {
                override fun onFailure(call: Call<Data>, t: Throwable) {
                    networkError.postValue(NETWORK_ERROR)
                    t.printStackTrace()
                }

                override fun onResponse(call: Call<Data>, response: Response<Data>) {

                    if (response.isSuccessful) {
                        ClearSongs(songDao).execute()
                        InsertSongs(songDao).execute(response.body()?.data)

                    } else {
                        networkError.postValue(SERVER_ERROR)
                    }

                }

            })
        } else {
            networkError.postValue(NETWORK_NOT_AVAILABLE)
        }

    }

    class ClearSongs(val songDao: songsDao?) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            songDao?.clearSongs()
            return null
        }

    }

    class InsertSongs(val songDao: songsDao?) : AsyncTask<List<Song>, Void, Void>() {
        override fun doInBackground(vararg params: List<Song>?): Void? {
            songDao?.insertSongs(params[0])
            return null
        }

    }


}
package com.example.musiccollection.viewsongs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.musiccollection.repository.songs_repository.SongsRepository

class SongsViewModel(application:Application): AndroidViewModel(application) {
    private val repository = SongsRepository(application)
    val songsObservable = repository.songsData
    val ErrorObsevable = repository.networkError
    var hasNetwork = false
    set(value) {
        field = value
        repository.networkAvailable = value
        repository.loadSongs()
    }
}
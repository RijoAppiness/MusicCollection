package com.example.musiccollection.viewsongs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.musiccollection.repository.songs_repository.SongsRepository

class SongsViewModel(application:Application): AndroidViewModel(application) {
    private val repository = SongsRepository(application)
    val ObservableAlbums = repository.albums
    val ObservableArtists = repository.artists
    val ObsevableError = repository.networkError
    val requestSongsByArtist = repository.requestSongsByArtist
    val requestSongsOnAlbum = repository.requestSongsOnAlbum

    val requestForArtists = repository.requestForArtists
    val requestForAlbums = repository.requestForAlbums

    val ObservableSongsByArtist = repository.songsByArtist
    val ObservableSongsOnAlbum = repository.songsOnAlbum
    var hasNetwork = false
    set(value) {
        field = value
        repository.networkAvailable = value
        repository.loadSongs()
    }
}
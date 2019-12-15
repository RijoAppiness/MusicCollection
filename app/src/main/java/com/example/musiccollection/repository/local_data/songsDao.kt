package com.example.musiccollection.repository.local_data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.musiccollection.repository.models.Song


@Dao
interface songsDao {
    @Insert
    fun insertSongs(songs: List<Song>?)

    @Query("SELECT * FROM Song")
    fun getSongs(): LiveData<List<Song>>

    @Query("DELETE FROM Song")
    fun clearSongs()

}
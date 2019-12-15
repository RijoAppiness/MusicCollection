package com.example.musiccollection.repository.local_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.musiccollection.repository.models.Song

@Database(entities = arrayOf(Song::class),version = 1)
abstract class DatabaseInteractor:RoomDatabase(){
    companion object {
        private var instance:DatabaseInteractor?=null
        fun getInstance(context:Context):DatabaseInteractor?{
            if(instance==null){
                instance = Room.databaseBuilder(context,DatabaseInteractor::class.java,"songsDB")
                    .fallbackToDestructiveMigration().build()
                return instance
            }
            else{
                return instance
            }
        }
    }
  abstract fun getDao():songsDao

}
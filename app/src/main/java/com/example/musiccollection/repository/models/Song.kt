package com.example.musiccollection.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Song(
 @PrimaryKey(autoGenerate = true)
 val songId:Long,
 @SerializedName("Name")
 val name:String,
 @SerializedName("Artist")
 val artist:String,
 @SerializedName("Album")
 val album:String
)
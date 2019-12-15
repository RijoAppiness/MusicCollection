package com.example.musiccollection.viewsongs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musiccollection.R
import com.example.musiccollection.Utils.ConnectionStateMonitor
import com.example.musiccollection.repository.models.Song
import com.example.musiccollection.repository.network_data.NETWORK_ERROR
import com.example.musiccollection.repository.network_data.NETWORK_NOT_AVAILABLE
import com.example.musiccollection.repository.network_data.SERVER_ERROR
import com.facebook.stetho.Stetho
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_songs.*

class SongsActivity : AppCompatActivity() {

    private var mViewModel: SongsViewModel? = null
    private val viewSongAdapter: ViewSongAdapter = ViewSongAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)
        Stetho.initializeWithDefaults(this)
        mViewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)


        ConnectionStateMonitor(this).observe(this, Observer {
            mViewModel?.hasNetwork = it
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = viewSongAdapter
        mViewModel?.songsObservable?.observe(this, Observer {
            viewSongAdapter.submitList(it)
        })
        mViewModel?.ErrorObsevable?.observe(this, Observer {

            when (it) {
                SERVER_ERROR -> Snackbar.make(coordinatorLayout, R.string.server_error, Snackbar.LENGTH_SHORT).show()
                NETWORK_ERROR -> Snackbar.make(coordinatorLayout, R.string.network_error, Snackbar.LENGTH_SHORT).show()
                NETWORK_NOT_AVAILABLE -> Snackbar.make(
                    coordinatorLayout,
                    R.string.network_not_avalable,
                    Snackbar.LENGTH_SHORT
                ).show()
            }

        })
    }

    private class ViewSongAdapter : ListAdapter<Song, ViewSongAdapter.ViewHolder>(DIFF_CALL_BACK) {
        companion object {
            private val DIFF_CALL_BACK = object : DiffUtil.ItemCallback<Song>() {
                override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                    if (oldItem.name == newItem.name)
                        return true
                    else
                        return false
                }

                override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                    if (oldItem.name == newItem.name && oldItem.artist == newItem.artist && oldItem.album == newItem.album)
                        return true
                    else
                        return false
                }


            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.content_design, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.name.text = getItem(position).name
            holder.artist.text = getItem(position).artist
            holder.album.text = getItem(position).album
        }


        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val name = itemView.findViewById<TextView>(R.id.name)
            val artist = itemView.findViewById<TextView>(R.id.artist)
            val album = itemView.findViewById<TextView>(R.id.album)
        }

    }


}

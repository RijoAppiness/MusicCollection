package com.example.musiccollection.viewsongs

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.*
import com.example.musiccollection.R
import com.example.musiccollection.Utils.ConnectionStateMonitor
import com.example.musiccollection.repository.network_data.NETWORK_ERROR
import com.example.musiccollection.repository.network_data.NETWORK_NOT_AVAILABLE
import com.example.musiccollection.repository.network_data.SERVER_ERROR
import com.facebook.stetho.Stetho
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_songs.*


class SongsActivity : AppCompatActivity() {

    private var mViewModel: SongsViewModel? = null
    private lateinit var artistOrAlbumAdapter: ArtistOrAlbumAdapter
    private val songsAdapterMap: MutableMap<String, SongsAdapter> = HashMap()
    private var span = 1
    private var groupBy = "Artist"
    private var preferences:SharedPreferences?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_songs)
        Stetho.initializeWithDefaults(this)
        preferences = getSharedPreferences(this.packageName, Context.MODE_PRIVATE)
        initPreference()
        val groupbyAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,resources.getStringArray(R.array.group_by))
        groupbyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        groupby_spinner.adapter = groupbyAdapter

        val spanAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,resources.getStringArray(R.array.span))
        groupbyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        span_spinner.adapter =spanAdapter

        groupby_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
               preferences?.edit()?.putString("groupBy",groupby_spinner.selectedItem.toString())
                groupBy = groupby_spinner.selectedItem.toString()
                requestData()
            }

        }

        span_spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferences?.edit()?.putInt("span",position+1)
                span = position+1
                artistOrAlbumAdapter.span = span
                recyclerView.adapter = artistOrAlbumAdapter
                requestData()
            }

        }

        groupby_spinner.setSelection(run{
            val groupBy = preferences?.getString("groupBy","Album")
            when(groupBy){
                "Album"-> 0
                "Artist"->1
                else -> 0
            }
        })

        span_spinner.setSelection(run{
            val span = preferences?.getInt("span",3)
            span!! -1
        })



        mViewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        artistOrAlbumAdapter = ArtistOrAlbumAdapter(this, songsAdapterMap, span)

        ConnectionStateMonitor(this).observe(this, Observer {
            mViewModel?.hasNetwork = it
            requestData()
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = artistOrAlbumAdapter

        mViewModel?.ObservableAlbums?.observe(this, Observer {
            it.forEach {
                songsAdapterMap.put(it, SongsAdapter())
            }
            artistOrAlbumAdapter.submitList(it)
            requestSongsByAlbum(it)

        })

        mViewModel?.ObservableArtists?.observe(this, Observer {
            it.forEach {
                songsAdapterMap.put(it, SongsAdapter())
            }
            artistOrAlbumAdapter.submitList(it)
            requestSongsByArtist(it)
        })
        mViewModel?.ObservableSongsByArtist?.observe(this, Observer {
            val list: MutableList<String> = ArrayList()
            it.forEach {
                list.add(it.name)
            }
            if (it.size > 0)
                songsAdapterMap[it[0].artist]?.submitList(list)

        })
        mViewModel?.ObservableSongsOnAlbum?.observe(this, Observer {
            val list: MutableList<String> = ArrayList()
            it.forEach {
                list.add(it.name)
            }
            if (it.size > 0)
                songsAdapterMap[it[0].album]?.submitList(list)
        })

        mViewModel?.ObsevableError?.observe(this, Observer {

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

       requestData()

    }



    private fun initPreference(){
        if(preferences!!.contains("groupBy")){
           groupBy = preferences?.getString("groupBy","Album").toString()

        }
        else{
           preferences?.edit()?.putString("groupBy","Album")?.commit()
            groupBy = "Album"
        }

        if(preferences!!.contains("span")){
           span = preferences?.getInt("span",3)!!
        }
        else{
            preferences?.edit()?.putInt("span",3)?.commit()
            span = 3
        }
    }


    private fun requestData(){
        when(groupBy){
            "Album"-> mViewModel?.requestForAlbums?.value = true
            "Artist"-> mViewModel?.requestForArtists?.value = true
        }
    }

    private fun requestSongsByAlbum(albumOrArtistlist: List<String>) {
        val runnable = object : Runnable {
            override fun run() {
                albumOrArtistlist.forEach {
                    mViewModel?.requestSongsOnAlbum?.postValue(it)
                    Thread.sleep(100)
                }
            }

        }
        val thread = Thread(runnable)
        thread.start()
    }

    private fun requestSongsByArtist(albumOrArtistlist: List<String>) {
        val runnable = object : Runnable {
            override fun run() {
                albumOrArtistlist.forEach {
                    mViewModel?.requestSongsByArtist?.postValue(it)
                    Thread.sleep(100)
                }
            }

        }
        val thread = Thread(runnable)
        thread.start()
    }

    private class ArtistOrAlbumAdapter(
        val context: Context, val songsAdapterMap: MutableMap<String, SongsAdapter>,
        var span: Int
    ) : ListAdapter<String, ArtistOrAlbumAdapter.ViewHolder>(DIFF_CALL_BACK) {
        companion object {
            private val DIFF_CALL_BACK = object : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }


            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.content_design_album_or_artist, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.artistOrAlbum.text = getItem(position)
            val gridLayoutManager = GridLayoutManager(context,span)
            gridLayoutManager.orientation = GridLayoutManager.HORIZONTAL
            holder.songsRV.layoutManager = gridLayoutManager
            holder.songsRV.adapter = songsAdapterMap[getItem(position)]
            val item = getItem(position)
            Log.d("=>", item)

        }


        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val artistOrAlbum = itemView.findViewById<TextView>(R.id.artistOrAlbum)
            val songsRV = itemView.findViewById<RecyclerView>(R.id.songsRV)
        }

    }

    private class SongsAdapter : ListAdapter<String, SongsAdapter.ViewHolder>(DIFF_CALL_BACK) {
        companion object {
            private val DIFF_CALL_BACK = object : DiffUtil.ItemCallback<String>() {
                override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                    return oldItem == newItem
                }


            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.content_design_songs, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder) {
                song.text = getItem(position)
            }
        }


        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val song = itemView.findViewById<TextView>(R.id.songname)
        }

    }


}

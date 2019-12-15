package com.example.musiccollection.Utils

import android.content.Context
import androidx.lifecycle.LiveData
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.content.Intent
import android.content.BroadcastReceiver
import android.net.NetworkInfo
import android.net.Network
import android.net.ConnectivityManager
import android.os.Build
import android.content.IntentFilter
import android.net.NetworkCapabilities
import android.net.NetworkRequest


class ConnectionStateMonitor(private val mContext: Context) : LiveData<Boolean>() {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var networkReceiver: NetworkReceiver? = null
    private val connectivityManager: ConnectivityManager?

    init {
        connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = NetworkCallback(this)
        } else {
            networkReceiver = NetworkReceiver()
        }
    }

    override fun onActive() {
        super.onActive()
        updateConnection()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager!!.registerDefaultNetworkCallback(networkCallback)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            connectivityManager!!.registerNetworkCallback(networkRequest, networkCallback)
        } else {
            mContext.registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    override fun onInactive() {
        super.onInactive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager!!.unregisterNetworkCallback(networkCallback)
        } else {
            mContext.unregisterReceiver(networkReceiver)
        }
    }

    internal inner class NetworkCallback(private val mConnectionStateMonitor: ConnectionStateMonitor) :
        ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network?) {
            if (network != null) {
                mConnectionStateMonitor.postValue(true)
            }
        }

        override fun onLost(network: Network) {
            mConnectionStateMonitor.postValue(false)
        }
    }

    private fun updateConnection() {
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetworkInfo
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
                postValue(true)
            } else {
                postValue(false)
            }
        }

    }

    internal inner class NetworkReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                updateConnection()
            }
        }
    }
}
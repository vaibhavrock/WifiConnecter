package app.poc.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.net.wifi.WifiConfiguration

import android.R.string.no




class MainActivity : AppCompatActivity() , WifiListAdapter.AdapterCallback{

    lateinit  var context :Context
    var wifiManager: WifiManager? = null
    private val REQUESTED_PERMISSIONS = arrayOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.WRITE_SETTINGS
    )
 /*   val executors = AppExecutors()
    var scanResultSelected: ScanResult? = null
    var selectedSSID = "NETWORK"*/

    lateinit var rvWifiList: RecyclerView
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        rvWifiList = findViewById(R.id.rv_wifi_list)
        progressBar = findViewById(R.id.pb_progress_bar)
        rvWifiList.layoutManager = LinearLayoutManager(this)
        rvWifiList.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        applicationContext.registerReceiver(wifiScanReceiver, intentFilter)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, 121)
        } else {
            scanForWifi()
        }


    }

    private fun scanForWifi() {
        val success = wifiManager!!.startScan()
        if (!success) {
            scanFailure()
        }
    }

    override fun onResume() {
        super.onResume()
    }


    val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 121) {
            scanForWifi()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun scanSuccess() {
        progressBar.visibility = View.GONE
        val results = wifiManager!!.scanResults
        val adapter = WifiListAdapter(results, this)
        rvWifiList.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        val results = wifiManager!!.scanResults
    }


    override fun onNetworkClick(data: ScanResult) {
        if(Common.checkWifiType(data.capabilities) != "OPEN"){
            showDialog(data)
        }
        else{
            connectWifi(data, "")
        }


    }
    private fun showDialog(scanResult: ScanResult) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_ssid_password_layout)
        val ssidName = dialog.findViewById(R.id.tv_ssid_name) as AppCompatTextView
        val password = dialog.findViewById(R.id.tv_ssid_password) as AppCompatEditText
        val submitBtn = dialog.findViewById(R.id.btnConnect) as CardView
        ssidName.text = scanResult.SSID
        val noBtn = dialog.findViewById(R.id.btnCancel) as CardView
        submitBtn.setOnClickListener {
            if (password.text!!.length > 0) {
                dialog.dismiss()
                connectWifi(scanResult, password.text.toString())
            } else {
                Toast.makeText(context, "Please enter password!", Toast.LENGTH_SHORT).show()
            }
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    private fun connectWifi(data: ScanResult, password: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                // Android 10 (API level 29) -- Android Q
                android10SpecifiedWithOldCode(data, data.SSID, password, data.capabilities)
            } else {
                // Android 11 (API level 30) -- Android R and more
                codeForAndroid11andMore(data, data.SSID, password, data.capabilities)
                //android10andMoreVersions(data, data.SSID, password, data.capabilities)
            }
        } else {
            // Android 9 (API level 28) -- Android P and lower
            android9AndPreviousVersion(data, data.SSID, password, data.capabilities)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun codeForAndroid11andMore(data: ScanResult,
                                        wifiSSID: String,
                                        wifiPassword: String,
                                        capabilities: String) {
        val suggestion2 = WifiNetworkSuggestion.Builder()
            .setSsid(data.SSID) // SSID of network
            .setWpa2Passphrase(wifiPassword) // password is network is not open
            //.setIsAppInteractionRequired(true) // Optional (Needs location permission)
            .build()

        val suggestionsList = listOf(suggestion2);

        val wifiManager =
            applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val list = wifiManager.configuredNetworks
        for (i in list) {
            if (i.SSID != null && i.SSID == "\"" + wifiSSID + "\"") {
                val isDisconnected = wifiManager.disconnect()
                val isEnabled = wifiManager.enableNetwork(i.networkId, true)
                val isReconnected = wifiManager.reconnect()
                break
            }
            else{
                wifiManager.removeNetwork(i.networkId)
                wifiManager.saveConfiguration()
            }
        }

        val status = wifiManager.addNetworkSuggestions(suggestionsList);
        Log.e("NETWORK", "Status:"+status)

        if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS){
            // already connected with another network
            val status1 = wifiManager.removeNetworkSuggestions(suggestionsList)
            Log.e("NETWORK", "Remove:"+status1)
        }

        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // do error handling here
            Log.e("NETWORK", "Error")
            //Toast.makeText(context, "Error:"+status, Toast.LENGTH_SHORT).show()
        }

        // Optional (Wait for post connection broadcast to one of your suggestions)
        val intentFilter =
            IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.e("NETWORK", "broadcastReceiver")

                if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                    Toast.makeText(context, "Post connection broadcastReceiver:"+intent, Toast.LENGTH_SHORT).show()

                    return;
                }
                // do post connect processing here
                //Toast.makeText(context, "Post connection:"+intent, Toast.LENGTH_SHORT).show()
                Log.e("NETWORK", "post connect")
            }
        };
        registerReceiver(broadcastReceiver, intentFilter)

        //android10andMoreVersions(data, data.SSID, wifiPassword, data.capabilities)
    }

    private fun android9AndPreviousVersion(
        scanResult: ScanResult,
        wifiSSID: String,
        wifiPassword: String,
        capabilities: String
    ) {
        val conf = WifiConfiguration()
        conf.SSID =
            "\"" + wifiSSID + "\"" // Please note the quotes. String should contain ssid in quotes
        conf.status = WifiConfiguration.Status.ENABLED
        conf.priority = 40

        if (Common.checkWifiType(capabilities) == "WEP") {
            Log.e("NETWORK", "Configuring WEP")
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            if (wifiPassword.matches(Regex("^[0-9a-fA-F]+$"))) {
                conf.wepKeys[0] = wifiPassword
            } else {
                conf.wepKeys[0] = "\"" + wifiPassword + "\""
            }
            conf.wepTxKeyIndex = 0
        } else if (Common.checkWifiType(capabilities) == "WPA") {
            Log.e("NETWORK", "Configuring WPA")
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            conf.preSharedKey = "\"" + wifiPassword + "\""
        } else {
            Log.e("NETWORK", "Configuring OPEN network")
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wifiManager.addNetwork(conf)
        Log.e("NETWORK", "Add result $networkId")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val list = wifiManager.configuredNetworks
        for (i in list) {
            if (i.SSID != null && i.SSID == "\"" + wifiSSID + "\"") {
                Log.e("NETWORK", "WifiConfiguration SSID " + i.SSID)
                val isDisconnected = wifiManager.disconnect()
                Log.e("NETWORK", "isDisconnected : $isDisconnected")
                val isEnabled = wifiManager.enableNetwork(i.networkId, true)
                Log.e("NETWORK", "isEnabled : $isEnabled")
                val isReconnected = wifiManager.reconnect()
                Log.e("NETWORK", "isReconnected : $isReconnected")
                break
            }
        }
        //val connectionInfo: WifiInfo = wifiManager.getConnectionInfo()
        gotoNextScreen(scanResult, wifiManager)
    }

    private fun android10andMoreVersions( scanResult: ScanResult,
                                           wifiSSID: String,
                                           wifiPassword: String,
                                           capabilities: String){
        // Android 10 (API level 29) -- Android Q (Android 10)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
                .setSsid(wifiSSID)
                //.setSsidPattern(PatternMatcher(wifiSSID, PatternMatcher.PATTERN_PREFIX))
                .setWpa2Passphrase(wifiPassword)
                .build()

            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                //.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                //.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .setNetworkSpecifier(wifiNetworkSpecifier)
                .build()
            val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


            val networkCallback = object : ConnectivityManager.NetworkCallback() {
               /* override fun onAvailable(network: Network) {
                    Log.d("NETWORK", "Network available")
                    // To make sure that requests don't go over mobile data
                    connectivityManager.bindProcessToNetwork(network)
                    //unregister network callback
                    //connectivityManager.unregisterNetworkCallback(this)
                    gotoNextScreen(scanResult, wifiManager)

                    super.onAvailable(network)
                }*/

              /*  override fun onUnavailable() {
                    Log.d("NETWORK", "Network unavailable")
                    Toast.makeText(this@MainActivity, "Network unavailable!", Toast.LENGTH_SHORT).show()
                }*/

                override fun onAvailable(network: Network) {
                    Log.d("NETWORK", "Network available")
                    super.onAvailable(network)

                    // To make sure that requests don't go over mobile data
                    connectivityManager.bindProcessToNetwork(network)

                    //unregister network callback
                    connectivityManager.unregisterNetworkCallback(this)
                    connectivityManager.bindProcessToNetwork(null)

                    gotoNextScreen(scanResult, wifiManager)


                }

                override fun onUnavailable() {
                    Log.d("NETWORK", "Network unavailable")
                    super.onUnavailable()
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    Log.d("NETWORK", "onLosing")
                    super.onLosing(network, maxMsToLive)
                }

                override fun onLost(network: Network) {
                    Log.d("NETWORK", "onLost")
                    super.onLost(network)

                    connectivityManager.bindProcessToNetwork(null)
                    connectivityManager.unregisterNetworkCallback(this)
                }

            }
            connectivityManager.requestNetwork(networkRequest, networkCallback)
            val builder = NetworkRequest.Builder()
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
            //connectivityManager.registerNetworkCallback(networkRequest, networkCallback) // For listen
        }
    }

    private fun android10SpecifiedWithOldCode(
        scanResult: ScanResult,
        wifiSSID: String,
        wifiPassword: String,
        capabilities: String
    ) {

        val conf = WifiConfiguration()
        conf.SSID =
            "\"" + wifiSSID + "\"" // Please note the quotes. String should contain ssid in quotes
        conf.status = WifiConfiguration.Status.ENABLED
        conf.priority = 40

        if (Common.checkWifiType(capabilities) == "WEP") {
            Log.e("NETWORK", "Configuring WEP")
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            if (wifiPassword.matches(Regex("^[0-9a-fA-F]+$"))) {
                conf.wepKeys[0] = wifiPassword
            } else {
                conf.wepKeys[0] = "\"" + wifiPassword + "\""
            }
            conf.wepTxKeyIndex = 0
        } else if (Common.checkWifiType(capabilities) == "WPA") {
            Log.e("NETWORK", "Configuring WPA")
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            conf.preSharedKey = "\"" + wifiPassword + "\""
        } else {
            Log.e("NETWORK", "Configuring OPEN network")
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        }
        val wifiManager =
            this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networkId = wifiManager.addNetwork(conf)
        Log.e("NETWORK", "Data " + wifiManager.dhcpInfo)


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val list = wifiManager.configuredNetworks
        for (i in list) {
            if (i.SSID != null && i.SSID == "\"" + wifiSSID + "\"") {
                Log.e("NETWORK", "WifiConfiguration SSID " + i.SSID)
                val isDisconnected = wifiManager.disconnect()
                Log.e("NETWORK", "isDisconnected : $isDisconnected")
                val isEnabled = wifiManager.enableNetwork(i.networkId, true)
                Log.e("NETWORK", "isEnabled : $isEnabled")
                val isReconnected = wifiManager.reconnect()
                Log.e("NETWORK", "isReconnected : $isReconnected")
                break
            }
        }
        val thread = object : Thread() {
            override fun run() {
                Thread.sleep(1000)
                Log.e("NETWORK", "Thread ...........................")

                if (Build.VERSION.SDK_INT >= 21) {

                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    //val connectionInfo: WifiInfo = wifiManager.getConnectionInfo()
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    val builder = NetworkRequest.Builder()
                    builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    //builder.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED)
                    connectivityManager.registerNetworkCallback(
                        builder.build(),
                        object : ConnectivityManager.NetworkCallback() {
                            override fun onUnavailable() {
                                super.onUnavailable()
                                Log.e("NETWORK", "Thread onUnavailable...........................")
                            }

                            override fun onLosing(network: Network, i: Int) {
                                super.onLosing(network, i)
                                Log.e("NETWORK", "Thread onLosing...........................")

                            }
                            override fun onAvailable(network: Network) {
                                super.onAvailable(network)
                                Log.e("NETWORK", "Thread onAvailable...........................")
                                // forcefully wifi usages
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    connectivityManager.bindProcessToNetwork(network)
                                    gotoNextScreen(scanResult, wifiManager)
                                } else {
                                    ConnectivityManager.setProcessDefaultNetwork(network)
                                    gotoNextScreen(scanResult, wifiManager)
                                }
                                connectivityManager.unregisterNetworkCallback(this)

                                if (Build.VERSION.SDK_INT >= 23) {
                                    connectivityManager.bindProcessToNetwork(null as Network?)
                                } else if (Build.VERSION.SDK_INT >= 21) {
                                    ConnectivityManager.setProcessDefaultNetwork(null as Network?)
                                }    }

                            override fun onLost(network: Network) {
                                super.onLost(network)
                                Log.e("NETWORK", "Thread onLost...........................")
                                connectivityManager.unregisterNetworkCallback(this)
                                if (Build.VERSION.SDK_INT >= 23) {
                                    connectivityManager.bindProcessToNetwork(null as Network?)
                                } else if (Build.VERSION.SDK_INT >= 21) {
                                    ConnectivityManager.setProcessDefaultNetwork(null as Network?)
                                }
                            }
                        })
                }
            }
        }
        thread.start()
    }

    private fun gotoNextScreen(connectedWifi: ScanResult, wifiManager: WifiManager) {
        Log.e("check", "wifi:"+wifiManager.connectionInfo)
        Log.e("check", "connection:"+connectedWifi.SSID)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(
                Intent(this, WebViewActivity::class.java).putExtra(
                    "url",
                    "https://www.google.co.in"
                )
            )
        }, 10000.toLong())


    }

}
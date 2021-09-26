package app.poc.wifi

import android.net.wifi.ScanResult
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiListViewHolder(view: View, val viewHolderCallback: ViewHolderCallback): RecyclerView.ViewHolder(view) {

    val tvWifiName: TextView = view.findViewById(R.id.tvWifiName)
    val tvWifiType: TextView = view.findViewById(R.id.tvWifiNameSecondary)

    fun setData(data: ScanResult){
        tvWifiName.text = data.SSID
        tvWifiType.text = Common.checkWifiType(data.capabilities)

        tvWifiName.setOnClickListener {
            viewHolderCallback.onNetworkClick(data)
        }
    }

    public interface ViewHolderCallback{
        fun onNetworkClick(data: ScanResult)
    }



}
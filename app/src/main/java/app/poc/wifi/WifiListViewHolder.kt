package app.poc.wifi

import android.net.wifi.ScanResult
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WifiListViewHolder(view: View, private val viewHolderCallback: ViewHolderCallback): RecyclerView.ViewHolder(view) {

    private val rootLayout: LinearLayout = view.findViewById(R.id.rootLayout)
    private val tvWifiName: TextView = view.findViewById(R.id.tvWifiName)
    private val tvWifiType: TextView = view.findViewById(R.id.tvWifiNameSecondary)

    fun setData(data: ScanResult){
        tvWifiName.text = data.SSID
        tvWifiType.text = Common.checkWifiType(data.capabilities)

        rootLayout.setOnClickListener {
            viewHolderCallback.onNetworkClick(data)
        }
    }

    public interface ViewHolderCallback{
        fun onNetworkClick(data: ScanResult)
    }



}

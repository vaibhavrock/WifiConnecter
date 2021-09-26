package app.poc.wifi

import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class WifiListAdapter(private val list: List<ScanResult>, val adapterCallback: AdapterCallback):
    RecyclerView.Adapter<WifiListViewHolder>(),
    WifiListViewHolder.ViewHolderCallback {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WifiListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.adapter_scanned_wifi_listitem, parent, false)
        return WifiListViewHolder(view, this)
    }

    override fun onBindViewHolder(holder: WifiListViewHolder, position: Int) {
        holder.setData(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onNetworkClick(data: ScanResult) {
        adapterCallback.onNetworkClick(data)
    }

    public interface AdapterCallback{
        fun onNetworkClick(data: ScanResult);
    }
}
package com.fictivestudios.boatbasin

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_device.view.*

class DevicesAdapter(
    context:Context,
    deviceList: ArrayList<BluetoothDevice>,
    val mItemClickListener: ItemClickListener
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

private var devices = deviceList
    private var context = context


//    ArrayList<Device>()


    interface ItemClickListener{
        fun onItemClick(position: BluetoothDevice)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {

        val v = LayoutInflater
            .from(parent.context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(v)
    }

    override fun getItemCount(): Int {
     return devices.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.setData(devices[position])
    }


    /*fun setItems(devices: ArrayList<Device>){
        this.devices = devices
        notifyDataSetChanged()
    }
*/

    inner class DeviceViewHolder( itemView: View): RecyclerView.ViewHolder(itemView){
        fun setData(device: BluetoothDevice){

            if (device.name.isNullOrEmpty())
            {
                itemView.deviceName.text="Unnamed Device"
            }
            else{
                itemView.deviceName.text=device.name
            }
            itemView.deviceAddress.text=device.address



        }
        init {
            itemView.setOnClickListener {
             mItemClickListener.onItemClick(devices[adapterPosition])
            }
        }
    }

}
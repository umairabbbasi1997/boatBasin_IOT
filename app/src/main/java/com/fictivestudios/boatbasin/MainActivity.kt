package com.fictivestudios.boatbasin

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity(), DevicesAdapter.ItemClickListener {


    var devicesList = ArrayList<BluetoothDevice>()

    var isLightOn = false
    var isLedflasingOn = false
    var isLed9wOn = false
    var isHornOn = false
    var isLedFlashing2hz = false
    var isHorn20 = false



    var mBluetoothDevice: BluetoothDevice? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setDeviceBluetoothDiscoverable()
        allowLocationDetectionPermissions()

        if (bluetoothAdapter.isEnabled) {
            scanLeDevice(true) //make sure scan function won't be called several times
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            FINE_LOCATION_PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    scanLeDevice(true)
                } else {

                }
                return
            }
        }
    }


    private fun allowLocationDetectionPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_PERMISSION_REQUEST
            )
        }

    }


    private fun setDeviceBluetoothDiscoverable() {
        //no need to request bluetooth permission if  discoverability is requested
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(
            BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
            0
        )// 0 to keep it always discoverable
        startActivity(discoverableIntent)
    }


    private fun scanLeDevice(enable: Boolean) {
        when (enable) {
            true -> {
                // Stops scanning after a pre-defined scan period.
                Handler().postDelayed({
                    mScanning = false
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)

                   main_layout.visibility=View.INVISIBLE

                    rv_list.visibility = View.VISIBLE
                    if (!devicesList.isNullOrEmpty()) {
                        val adapter = DevicesAdapter(this@MainActivity, devicesList, this)
                        rv_list.adapter = adapter
                        adapter.notifyDataSetChanged()

                    }


                }, 4000)
                mScanning = true
                bluetoothAdapter?.bluetoothLeScanner?.startScan(mLeScanCallback)

            }
            else -> {
                mScanning = false
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(mLeScanCallback)
            }
        }


    }


    private var mLeScanCallback: ScanCallback =
        object : ScanCallback() {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                super.onScanResult(callbackType, result)

                result?.device?.let { devicesList.add(it) }

            }

            override fun onBatchScanResults(results: List<ScanResult?>?) {
                super.onBatchScanResults(results)


                Log.w("mylog", "device")


            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)


            }


        }






    fun writeCharacteristic(Data: String, gatt: BluetoothGatt) {


        val value: ByteArray = Data.decodeHex()
        val mCustomService: BluetoothGattService =
            gatt.getService(UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
        if (mCustomService == null) {
            Log.w("mylog", "Custom BLE Service not found")
            return
        }
        Log.w("mylog", "service uuid: $mCustomService")
        /*get the read characteristic from the service*/
        val characteristic =
            mCustomService.getCharacteristic(UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb"))
        characteristic.value = value
        Log.w("mylog", "service uuid: $characteristic")
        gatt.writeCharacteristic(characteristic)
    }




    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("mylog", "Successfully connected to $deviceAddress")

                    Handler(Looper.getMainLooper()).post {

                        Toast.makeText(
                            applicationContext,
                            "Connected Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        rv_list.visibility = View.INVISIBLE
                        main_layout.visibility=View.VISIBLE


                        val ans: Boolean = gatt.discoverServices()
                        Log.d("mylog", "Discover Services started: $ans")
                    }




                    btn_light.setOnClickListener {

                        try {
                            if (isLightOn) {
                                isLightOn = false
                                btn_light.setText("on")
                                writeCharacteristic("AA55F100BB", gatt)
                            } else {
                                btn_light.setText("off")
                                isLightOn = true
                                writeCharacteristic("AA55F101BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                                                    }


                    }


                    btn_Led_blink.setOnClickListener {

                        try {
                            if (isLedflasingOn) {
                                isLedflasingOn = false
                                btn_Led_blink.setText("on")
                                writeCharacteristic("AA55F201BB", gatt)
                            } else {
                                btn_Led_blink.setText("off")
                                isLedflasingOn = true
                                writeCharacteristic("AA55F200BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                        }


                    }


                    btn_led_9w.setOnClickListener {

                        try {
                            if (isLed9wOn) {
                                isLed9wOn = false
                                btn_led_9w.setText("on")
                                writeCharacteristic("AA55F301BB", gatt)
                            } else {
                                btn_led_9w.setText("off")
                                isLed9wOn = true
                                writeCharacteristic("AA55F300BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                        }


                    }

                    btn_horn.setOnClickListener {

                        try {
                            if (isHornOn) {
                                isHornOn = false
                                btn_horn.setText("on")
                                writeCharacteristic("AA55F401BB", gatt)
                            } else {
                                btn_horn.setText("off")
                                isHornOn = true
                                writeCharacteristic("AA55F401BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                        }


                    }

                    btn_led_flash_2hz.setOnClickListener {

                        try {
                            if (isLedFlashing2hz) {
                                isLedFlashing2hz = false
                                btn_led_flash_2hz.setText("on")
                                writeCharacteristic("AA55F502BB", gatt)
                            } else {
                                btn_led_flash_2hz.setText("on")
                                isLedFlashing2hz = true
                                writeCharacteristic("AA55F502BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                        }


                    }


                    btn_horn_20.setOnClickListener {

                        try {
                            if (isHorn20) {
                                isHorn20 = false
                                btn_horn_20.setText("on")
                                writeCharacteristic("AA55F602BB", gatt)
                            } else {
                                btn_horn_20.setText("on")
                                isHorn20 = true
                                writeCharacteristic("AA55F602BB", gatt)

                            }
                        }
                        catch (e:Exception)
                        {
                        }


                    }


                    // AddFragment()

                    //   initScanning(bluetoothAdapter?.bluetoothLeScanner)


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("mylog", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("mylog", "Error $status encountered for $deviceAddress! Disconnecting...")
                Toast.makeText(
                    applicationContext,
                    "Error $status encountered for $deviceAddress! Disconnecting...",
                    Toast.LENGTH_SHORT
                ).show()
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (gattService in gatt!!.services) {
                    Log.i(
                        "mylog",
                        "Service UUID Found: " + gattService.uuid.toString() + "name: " + gattService.type.toString()
                    )
                }

            }


        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }


    }

    private fun AddFragment() {
        val fragtrans =
            supportFragmentManager.beginTransaction().add(R.id.container, DeviceConnectedFragment())
                .commit()

    }

    companion object {
        private const val FINE_LOCATION_PERMISSION_REQUEST = 1001
    }


    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    override fun onItemClick(deviceAddress: BluetoothDevice) {

        mBluetoothDevice = deviceAddress
        connectwithDevice(deviceAddress)


    }

    private fun connectwithDevice(deviceAddress: BluetoothDevice) {

/*        if (deviceAddress.address == "0B:07:04:03:02:01")*/

        with(deviceAddress) {
            android.util.Log.w("mylog", "Connecting to $deviceAddress")
            Toast.makeText(applicationContext, "Connecting to $deviceAddress", Toast.LENGTH_SHORT)
                .show()
            this!!.connectGatt(this@MainActivity, false, gattCallback)


        }
    }


    var serviceUUIDsList: List<UUID> = ArrayList()
    var characteristicUUIDsList: List<UUID> = ArrayList()
    var descriptorUUIDsList: List<UUID> = ArrayList()

    private fun initScanning(bleScanner: BluetoothLeScanner) {

        bleScanner.startScan(getScanCallback())
    }

    private fun getScanCallback(): ScanCallback? {
        return object : ScanCallback() {
            override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                super.onScanResult(callbackType, scanResult)
                serviceUUIDsList = getServiceUUIDsList(scanResult)

                Log.i("mylog", "Service UUID Found: " + serviceUUIDsList.toString())
            }
        }
    }

    private fun getServiceUUIDsList(scanResult: ScanResult): List<UUID> {
        val parcelUuids = scanResult.scanRecord!!.serviceUuids
        val serviceList: MutableList<UUID> = ArrayList()
        for (i in parcelUuids.indices) {
            val serviceUUID = parcelUuids[i].uuid
            if (!serviceList.contains(serviceUUID)) serviceList.add(serviceUUID)
        }
        return serviceList
    }


/*    fun writeCharacteristic(mBluetoothGatt:BluetoothGatt):Boolean{

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        var Service = mBluetoothGatt.getService(your Services);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }
        var charac = Service
                .getCharacteristic(your characteristic);
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        var value =

        charac.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        return status;
    }*/

/*    fun write(){
        try{
            //convert seek bar value to byte value.
          //  val convertValue = convertSeekBarValueBoardValue(binding.seekBar.progress)
        //    val byteValue = convertValue.toByte()
            //create service
            val service = mBluetoothGatt!!.getService(UUID.fromString(getString(R.string.light_up_demo_uuid)))
            if(service == null){
                Toast.makeText(this,getString(R.string.error_invalid_service_uuid),Toast.LENGTH_SHORT).show()
                return
            }
            val bleChar = service.getCharacteristic(UUID.fromString(MainApplication.LIGHT_UP_SERVICE_UUID))
            if(bleChar == null){
                Toast.makeText(this,getString(R.string.error_service_not_found),Toast.LENGTH_SHORT).show()
            }
            //if enableLight == false then light off(0)
            if(enableLight){
                bleChar.setValue(byteArrayOf(byteValue))
            }else{
                bleChar.setValue(byteArrayOf(0))
            }
            mBluetoothGatt!!.writeCharacteristic(bleChar)
        }catch (e:NumberFormatException){
            Toast.makeText(this,
                getString(R.string.error_light_up_range),Toast.LENGTH_SHORT).show()
        }
    }*/

}




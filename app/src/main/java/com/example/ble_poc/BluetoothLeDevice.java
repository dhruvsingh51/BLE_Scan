package com.example.ble_poc;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Dhruv on 2/2/17.
 */
public class BluetoothLeDevice {
    private int RSSI;
    private BluetoothDevice device;
    private int mRSSICount = Constant.MIN_RSSI_COUNT;

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getmRSSICount() {
        return mRSSICount;
    }

    public void setmRSSICount(int mRSSICount) {
        this.mRSSICount = mRSSICount;
    }


    public void updateRSSIcount() {
        mRSSICount++;
    }

    public void addRSSIValue(int rssi) {
        RSSI = RSSI + rssi;
    }
}

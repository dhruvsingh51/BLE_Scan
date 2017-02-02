package com.example.ble_poc;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dhruv on 2/2/17.
 */

public final class BluetoothUtils {
    public final static int REQUEST_ENABLE_BT = 2001;
    private final Activity mActivity;
    private final BluetoothAdapter mBluetoothAdapter;
    private Map<String, BluetoothLeDevice> mDeviceMap;

    public BluetoothUtils(final Activity activity) {
        mActivity = activity;
        final BluetoothManager bluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mDeviceMap = new HashMap<String, BluetoothLeDevice>();
    }

    public void askUserToEnableBluetoothIfNeeded() {
        if (isBluetoothLeSupported() && (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public boolean isBluetoothLeSupported() {
        return mActivity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean isBluetoothOn() {
        if (mBluetoothAdapter == null) {
            return false;
        } else {
            return mBluetoothAdapter.isEnabled();
        }
    }

    public void storeDevice(BluetoothLeDevice device) {
        if (mDeviceMap.containsKey(device.getDevice().getAddress())) {
            mDeviceMap.get(device.getDevice().getAddress()).updateRSSIcount();
            mDeviceMap.get(device.getDevice().getAddress()).addRSSIValue(device.getRSSI());
        } else {
            mDeviceMap.put(device.getDevice().getAddress(), device);
        }
    }

    public Map<String, BluetoothLeDevice> getDeviceMap() {
        return mDeviceMap;
    }
}

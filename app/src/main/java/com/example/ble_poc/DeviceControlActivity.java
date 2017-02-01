/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ble_poc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DeviceControlActivity extends Activity {
	private final static String TAG = DeviceControlActivity.class.getSimpleName();

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	// private static final String MLDP_PRIVATE_SERVICE =
	// "00035b03-58e6-07dd-021a-08123a000300";

	private TextView mConnectionState;
	private TextView mDataField;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private boolean mConnected = false;
	private BluetoothGattCharacteristic mNotifyCharacteristic;
	private BluetoothGattCharacteristic mDataMDLP;

	private final String LIST_NAME = "NAME";
	private final String LIST_UUID = "UUID";
	private static final String RING_SERVICE_UUID = "49F01460-02C7-11E5-a322-1697f925ec7b";

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				updateConnectionState(R.string.connected);
				invalidateOptionsMenu();
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				updateConnectionState(R.string.disconnected);
				invalidateOptionsMenu();
				clearUI();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the
				// user interface.
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	// If a given GATT characteristic is selected, check for supported features.
	// This sample
	// demonstrates 'Read' and 'Notify' features. See
	// http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for
	// the complete
	// list of supported characteristic features.

	private void clearUI() {
		mDataField.setText(R.string.no_data);
	}

	Button readbtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);

		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		readbtn = (Button) findViewById(R.id.readbtn);
		readbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = readbtn.getText().toString();
				// for(int i=0;i<str.length();i++){
				// mDataMDLP.setValue(str.charAt(i)+"");//"AT+VBAT?"
				mDataMDLP.setValue("AT+VBAT?");
				mBluetoothLeService.writeCharacteristic(mDataMDLP);

				// }

			}
		});
		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

		mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);

		// getActionBar().setTitle(mDeviceName);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		mBluetoothLeService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);
		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			mBluetoothLeService.connect(mDeviceAddress);
			return true;
		case R.id.menu_disconnect:
			mBluetoothLeService.disconnect();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateConnectionState(final int resourceId) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mConnectionState.setText(resourceId);
			}
		});
	}

	private void displayData(String data) {
		if (data != null) {
			mDataField.append(data);
		}
	}

	// Demonstrates how to iterate through the supported GATT
	// Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the
	// ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = getResources().getString(R.string.unknown_service);
		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {

			if (gattService.getUuid().toString().equalsIgnoreCase(RING_SERVICE_UUID)) {

				Log.e("Service Discoverd", gattService.getUuid().toString());

				Iterator localIterator2 = gattService.getCharacteristics().iterator();
				while (localIterator2.hasNext()) {
					BluetoothGattCharacteristic localBluetoothGattCharacteristic = (BluetoothGattCharacteristic) localIterator2
							.next();
					String str = localBluetoothGattCharacteristic.getUuid().toString();
					if (str.equalsIgnoreCase("49F016FE-02C7-11E5-a322-1697f925ec7b")) {
						this.mNotifyCharacteristic = localBluetoothGattCharacteristic;
						int i = localBluetoothGattCharacteristic.getProperties();

						if ((i | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
							mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
						}
						if ((i | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
							mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
							mBluetoothLeService.readCharacteristic(localBluetoothGattCharacteristic);
						}

						if ((i | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mNotifyCharacteristic = localBluetoothGattCharacteristic;
							mBluetoothLeService.setCharacteristicNotification(localBluetoothGattCharacteristic, true);
							mBluetoothLeService.readCharacteristic(localBluetoothGattCharacteristic);
						}
					} else if (str.equalsIgnoreCase("49F01848-02C7-11E5-a322-1697f925ec7b")) {
						mDataMDLP = localBluetoothGattCharacteristic;
						int i = localBluetoothGattCharacteristic.getProperties();
						if ((i | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
							mBluetoothLeService.setCharacteristicNotification(mDataMDLP, true);
							mDataMDLP.setValue("AT+VBAT?");
							mBluetoothLeService.writeCharacteristic(mDataMDLP);

						}
						/*
						if ((i | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

							mBluetoothLeService.setCharacteristicNotification(mDataMDLP, true);
							mBluetoothLeService.readCharacteristic(localBluetoothGattCharacteristic);
						}

						if ((i | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
							mDataMDLP = localBluetoothGattCharacteristic;
							mBluetoothLeService.setCharacteristicNotification(mDataMDLP, true);
						}
						 */					}

				}
			}

		}

	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
}

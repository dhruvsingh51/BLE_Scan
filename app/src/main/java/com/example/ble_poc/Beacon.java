package com.example.ble_poc;

public class Beacon {

	public String deviceName;
	public String macId;

	public Beacon(String deviceName, String macId) {
		super();
		this.deviceName = deviceName;
		this.macId = macId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getMacId() {
		return macId;
	}
	public void setMacId(String macId) {
		this.macId = macId;
	}
}

package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeResponse;

/**
 * 
 * @author Andrew Rapp
 *
 */
public abstract class RxBaseResponse extends XBeeResponse {

	private XBeeAddress sourceAddress;
	
	// arbitrary strength classification; RSSI range is -40 to -100
	public final static int STRONG_RSSI = -60;
	public final static int AVERAGE_RSSI = -80;
	
	private int rssi;
	private int options;
	
	public RxBaseResponse() {

	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public int getOptions() {
		return options;
	}

	public void setOptions(int options) {
		this.options = options;
	}
	
	public String getRssiStrength() {
		if (rssi < STRONG_RSSI) {
			return "STRONG";
		} else if (rssi < AVERAGE_RSSI) {
			return "AVERAGE";
		} else {
			return "WEAK";
		}
	}

	public XBeeAddress getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(XBeeAddress sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
	
	public String toString() {
		return super.toString() + ",sourceAddress=" + this.getSourceAddress().toString() + ",rssi=" + this.getRssi() + ",options=" + this.getOptions();
	}
}
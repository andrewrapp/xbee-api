/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.api.XBeeAddress;
import com.rapplogic.xbee.api.XBeeResponse;

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
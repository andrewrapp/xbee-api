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

import org.apache.log4j.Logger;

import com.rapplogic.xbee.util.ByteUtils;

public class RxResponseIoSample extends RxBaseResponse {

	private final static Logger log = Logger.getLogger(RxResponseIoSample.class);
	
	private IoSample[] samples;
	
	private int adcChannelIndicator;
	private int dioChannelIndicator;

	public RxResponseIoSample() {
		
	}

	public IoSample[] getSamples() {
		return samples;
	}

	public void setSamples(IoSample[] samples) {
		this.samples = samples;
	}
	
	public boolean isD0Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 1);
	}

	public boolean isD1Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 2);
	}

	public boolean isD2Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 3);
	}

	public boolean isD3Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 4);
	}
	
	public boolean isD4Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 5);
	}
	
	public boolean isD5Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 6);
	}
	
	public boolean isD6Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 7);
	}
	
	public boolean isD7Enabled() {
		return ByteUtils.getBit(dioChannelIndicator, 8);
	}	

	public boolean isD8Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 1);
	}	
	
	public boolean isA0Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 2);
	}

	public boolean isA1Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 3);
	}
	
	public boolean isA2Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 4);
	}
	
	public boolean isA3Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 5);
	}
	
	public boolean isA4Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 6);
	}
	
	public boolean isA5Enabled() {
		return ByteUtils.getBit(adcChannelIndicator, 7);
	}	
	
	public int getAdcChannelIndicator() {
		return adcChannelIndicator;
	}

	public void setAdcChannelIndicator(int adcChannelIndicator) {
		this.adcChannelIndicator = adcChannelIndicator;
	}

	public int getDioChannelIndicator() {
		return dioChannelIndicator;
	}

	public void setDioChannelIndicator(int dioChannelIndicator) {
		this.dioChannelIndicator = dioChannelIndicator;
	}

	/**
	 * Return true if this packet contains at least one analog sample
	 */
	public boolean containsAnalog() {
		// ADC is active if any of bits 2-7 are on
		return (this.getAdcChannelIndicator() >= 2 && !ByteUtils.getBit(this.getAdcChannelIndicator(), 8));
	}
	
	/**
	 * Returns true if this packet contains at least one digital sample
	 * 
	 * @return
	 */
	public boolean containsDigital() {
		// DIO 8 occupies the first bit of the adcHeader
		return (this.getDioChannelIndicator() > 0 || ByteUtils.getBit(this.getAdcChannelIndicator(), 1));
	}
	
	public String toString() {
		
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < samples.length; i++) {
			sb.append("Sample" + i + ": " + samples[i].toString());
		}
		
		return super.toString() + ",adcHeader=" + ByteUtils.toBase2(adcChannelIndicator) + ", dioHeader=" + ByteUtils.toBase2(dioChannelIndicator) + "#samples=" + this.samples.length + "," + sb.toString();
	}
}
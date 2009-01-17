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
	
	public final static int ADC_CHANNEL1 = 0x7e; //01111110
	public final static int DIO_CHANNEL1 = 0x1; //00000001
	
	private IoSample[] samples;
	
	private int channelIndicator1;
	private int channelIndicator2;

	public RxResponseIoSample() {
		
	}

	public IoSample[] getSamples() {
		return samples;
	}

	public void setSamples(IoSample[] samples) {
		this.samples = samples;
	}
	
	public boolean isD0Enabled() {
		return ByteUtils.getBit(channelIndicator2, 1);
	}

	public boolean isD1Enabled() {
		return ByteUtils.getBit(channelIndicator2, 2);
	}

	public boolean isD2Enabled() {
		return ByteUtils.getBit(channelIndicator2, 3);
	}

	public boolean isD3Enabled() {
		return ByteUtils.getBit(channelIndicator2, 4);
	}
	
	public boolean isD4Enabled() {
		return ByteUtils.getBit(channelIndicator2, 5);
	}
	
	public boolean isD5Enabled() {
		return ByteUtils.getBit(channelIndicator2, 6);
	}
	
	public boolean isD6Enabled() {
		return ByteUtils.getBit(channelIndicator2, 7);
	}
	
	public boolean isD7Enabled() {
		return ByteUtils.getBit(channelIndicator2, 8);
	}	

	public boolean isD8Enabled() {
		return ByteUtils.getBit(channelIndicator1, 1);
	}	
	
	public boolean isA0Enabled() {
		return ByteUtils.getBit(channelIndicator1, 2);
	}

	public boolean isA1Enabled() {
		return ByteUtils.getBit(channelIndicator1, 3);
	}
	
	public boolean isA2Enabled() {
		return ByteUtils.getBit(channelIndicator1, 4);
	}
	
	public boolean isA3Enabled() {
		return ByteUtils.getBit(channelIndicator1, 5);
	}
	
	public boolean isA4Enabled() {
		return ByteUtils.getBit(channelIndicator1, 6);
	}
	
	public boolean isA5Enabled() {
		return ByteUtils.getBit(channelIndicator1, 7);
	}

	public int getChannelIndicator1() {
		return channelIndicator1;
	}

	public void setChannelIndicator1(int channelIndicator1) {
		this.channelIndicator1 = channelIndicator1;
	}

	public int getChannelIndicator2() {
		return channelIndicator2;
	}

	public void setChannelIndicator2(int channelIndicator2) {
		this.channelIndicator2 = channelIndicator2;
	}

	/**
	 * Return true if this packet contains at least one analog sample
	 */
	public boolean containsAnalog() {
		// ADC is active if > 0 after channel mask is applied
		return (this.channelIndicator1 & ADC_CHANNEL1) > 0;
	}
	
	/**
	 * Returns true if this packet contains at least one digital sample
	 * 
	 * @return
	 */
	public boolean containsDigital() {
		// DIO 8 occupies the first bit of the adcHeader
		return (this.channelIndicator1 & DIO_CHANNEL1) > 0 || this.channelIndicator2 > 0;
	}
	
	public String toString() {
		
		String cr = "\n";
		
		try {
			cr = System.getProperty("line.separator");
		} catch (Exception e) {}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(super.toString());
		
		sb.append(",channelIndicator1=" + ByteUtils.toBase2(channelIndicator1));
		sb.append(",channelIndicator2=" + ByteUtils.toBase2(channelIndicator2));
		sb.append(",#samples=" + this.samples.length);
		
		for (int i = 0; i < samples.length; i++) {
			sb.append(cr + "Sample" + i + ": " + samples[i].toString());
		}
		
		return sb.toString();
	}
}
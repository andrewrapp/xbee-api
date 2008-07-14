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

package com.rapplogic.xbee.api.zigbee;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.XBeeParseException;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IIntArrayInputStream;

/**
 * ZNet does not seem to support multiple samples (IT) per packet
 * 
 * TODO toString
 * 
 * @author andrew
 *
 */
public class ZNetRxIoSampleResponse extends ZNetRxBaseResponse {

	@SuppressWarnings("unused")
	private final static Logger log = Logger.getLogger(ZNetRxIoSampleResponse.class);
	
	private int digitalChannelMask1;
	private int digitalChannelMask2;
	private int analogChannelMask;

	private int dioMsb;
	private int dioLsb;
	
	private int analog0;
	private int analog1;
	private int analog2;
	private int analog3;
	private int supplyVoltage;
	
	public ZNetRxIoSampleResponse() {
		
	}


	/**
	 * This method is a bit non standard since it needs to parse a sample
	 * from either a io sample or remote at response (IS).
	 * 
	 * @param ps
	 * @throws IOException
	 */
	public void parse(IIntArrayInputStream ps) throws IOException {
		// eat sample size.. always 1
		int size = ps.read("ZNet RX IO Sample Size");
		
		if (size != 1) {
			throw new XBeeParseException("Sample size is not supported if > 1 for ZNet I/O");
		}
		
		this.setDigitalChannelMask1(ps.read("ZNet RX IO Sample Digital Mask 1"));
		this.setDigitalChannelMask2(ps.read("ZNet RX IO Sample Digital Mask 2"));
		this.setAnalogChannelMask(ps.read("ZNet RX IO Sample Analog Channel Mask"));
		
		if (this.containsDigital()) {
			log.info("response contains digital data");
			// next two bytes are digital
			this.setDioMsb(ps.read("ZNet RX IO DIO MSB"));
			this.setDioLsb(ps.read("ZNet RX IO DIO LSB"));
		} else {
			log.info("response does not contain digital data");
		}
		
		// parse 10-bit analog values

		int analog = 0;
		
		if (this.isA0Enabled()) {
			log.info("response contains analog0");
			this.setAnalog0(ByteUtils.parse10BitAnalog(ps, analog));
			analog++;
		}
		
		if (this.isA1Enabled()) {
			this.setAnalog1(ByteUtils.parse10BitAnalog(ps, analog));
			analog++;
		}		

		if (this.isA2Enabled()) {
			this.setAnalog2(ByteUtils.parse10BitAnalog(ps, analog));
			analog++;
		}

		if (this.isA3Enabled()) {
			this.setAnalog3(ByteUtils.parse10BitAnalog(ps, analog));
			analog++;
		}
		
		if (this.isSupplyVoltageEnabled()) {
			this.setSupplyVoltage(ByteUtils.parse10BitAnalog(ps, analog));
			analog++;
		}
		
		log.debug("There are " + analog + " analog inputs in this packet");
	}
	
	public int getDigitalChannelMask1() {
		return digitalChannelMask1;
	}

	public void setDigitalChannelMask1(int digitalChannelMask1) {
		this.digitalChannelMask1 = digitalChannelMask1;
	}

	public int getDigitalChannelMask2() {
		return digitalChannelMask2;
	}

	public void setDigitalChannelMask2(int digitalChannelMask2) {
		this.digitalChannelMask2 = digitalChannelMask2;
	}

	public int getAnalogChannelMask() {
		return analogChannelMask;
	}

	public void setAnalogChannelMask(int analogChannelMask) {
		this.analogChannelMask = analogChannelMask;
	}
	
	public boolean isD0Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 1);
	}

	public boolean isD1Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 2);
	}

	public boolean isD2Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 3);
	}

	public boolean isD3Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 4);
	}

	public boolean isD4Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 5);
	}

	public boolean isD5Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 6);
	}

	public boolean isD6Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 7);
	}

	public boolean isD7Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask2, 8);
	}

	// TODO where is D8?? it's in the command ref.
	
	public boolean isD10Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask1, 3);
	}
	
	public boolean isD11Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask1, 4);
	}
	
	public boolean isD12Enabled() {
		return ByteUtils.getBit(this.digitalChannelMask1, 5);
	}
	
	public boolean isA0Enabled() {
		return ByteUtils.getBit(this.analogChannelMask, 1);
	}

	public boolean isA1Enabled() {
		return ByteUtils.getBit(this.analogChannelMask, 2);
	}
	
	public boolean isA2Enabled() {
		return ByteUtils.getBit(this.analogChannelMask, 3);
	}
	
	public boolean isA3Enabled() {
		return ByteUtils.getBit(this.analogChannelMask, 4);
	}

	public boolean isSupplyVoltageEnabled() {
		return ByteUtils.getBit(this.analogChannelMask, 8);
	}
	
	/**
	 * Returns true if digital 0 is HIGH (ON); false if it is LOW (OFF).  This is only meaningful to call
	 * if isD0Enabled returns true. 
	 * 
	 * @return
	 */
	public boolean isD0On() {
		return ByteUtils.getBit(dioLsb, 1);
	}

	public boolean isD1On() {
		return ByteUtils.getBit(dioLsb, 2);
	}
	
	public boolean isD2On() {
		return ByteUtils.getBit(dioLsb, 3);
	}
	
	public boolean isD3On() {
		return ByteUtils.getBit(dioLsb, 4);
	}
	
	public boolean isD4On() {
		return ByteUtils.getBit(dioLsb, 5);
	}

	public boolean isD5On() {
		return ByteUtils.getBit(dioLsb, 6);
	}
	
	public boolean isD6On() {
		return ByteUtils.getBit(dioLsb, 7);
	}
	
	public boolean isD7On() {
		return ByteUtils.getBit(dioLsb, 8);
	}
	
	public boolean isD10On() {
		return ByteUtils.getBit(dioMsb, 3);
	}

	public boolean isD11On() {
		return ByteUtils.getBit(dioMsb, 4);
	}
	
	public boolean isD12On() {
		return ByteUtils.getBit(dioMsb, 5);
	}	

	/**
	 * Returns true if this sample contains data from digital inputs
	 * 
	 * See manual page 68 for byte bit mapping
	 * 
	 * @return
	 */
	public boolean containsDigital() {
		if (this.isD0Enabled() || this.isD1Enabled() || this.isD2Enabled() || this.isD3Enabled() ||
				this.isD4Enabled() || this.isD5Enabled() || this.isD6Enabled() || this.isD7Enabled() ||
				this.isD10Enabled() || this.isD11Enabled() || this.isD12Enabled()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if this sample contains data from analog inputs or supply voltage
	 * 
	 * See manual page 68 for byte bit mapping
	 * 
	 * @return
	 */
	public boolean containsAnalog() {
		if (this.isA0Enabled() || this.isA1Enabled() || this.isA2Enabled() || this.isA3Enabled() || 
				this.isSupplyVoltageEnabled()) {
			return true;
		}
		
		return false;
	}

	public int getDioMsb() {
		return dioMsb;
	}

	public void setDioMsb(int dioMsb) {
		this.dioMsb = dioMsb;
	}

	public int getDioLsb() {
		return dioLsb;
	}

	public void setDioLsb(int dioLsb) {
		this.dioLsb = dioLsb;
	}

	public int getAnalog0() {
		return analog0;
	}

	public void setAnalog0(int analog0) {
		this.analog0 = analog0;
	}

	public int getAnalog1() {
		return analog1;
	}

	public void setAnalog1(int analog1) {
		this.analog1 = analog1;
	}

	public int getAnalog2() {
		return analog2;
	}

	public void setAnalog2(int analog2) {
		this.analog2 = analog2;
	}

	public int getAnalog3() {
		return analog3;
	}

	public void setAnalog3(int analog3) {
		this.analog3 = analog3;
	}

	public int getSupplyVoltage() {
		return supplyVoltage;
	}

	public void setSupplyVoltage(int supplyVoltage) {
		this.supplyVoltage = supplyVoltage;
	}
}
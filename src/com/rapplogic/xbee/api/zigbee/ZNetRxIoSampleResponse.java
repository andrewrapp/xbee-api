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
 * Provides access to the XBee's 4 Analog (0-4), 11 Digital (0-7,10-12), and 1 Supply Voltage pins
 * 
 * ZNet does not support multiple samples (IT) per packet
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

	// all values that may not be in the packet use Integer to distinguish between null and non-null
	private Integer dioMsb;
	private Integer dioLsb;
	
	private Integer analog0;
	private Integer analog1;
	private Integer analog2;
	private Integer analog3;
	private Integer supplyVoltage;
	
	public ZNetRxIoSampleResponse() {
		
	}

	/**
	 * This method is a bit non standard since it needs to parse an IO sample
	 * from either a RX response or a Remote AT/AT response (IS).
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
		
		// zero out n/a bits
		this.analogChannelMask = this.analogChannelMask & 0x8f; //10001111
		// zero out all but bits 3-5
		this.digitalChannelMask1 = this.digitalChannelMask1 & 0x1c; //11100
		
//		if (!this.containsDigital() && (this.getDigitalChannelMask1() > 0 || this.getDigitalChannelMask2() > 0)) {
//			throw new XBeeParseException("containsDigital and channal masks are conflicting");
//		}
//		
//		if (!this.containsAnalog() && this.getAnalogChannelMask() > 0) {
//			throw new XBeeParseException("containsAnalog and channal masks are conflicting");
//		}
		
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

	private void setDigitalChannelMask1(int digitalChannelMask1) {
		this.digitalChannelMask1 = digitalChannelMask1;
	}

	public int getDigitalChannelMask2() {
		return digitalChannelMask2;
	}

	private void setDigitalChannelMask2(int digitalChannelMask2) {
		this.digitalChannelMask2 = digitalChannelMask2;
	}

	public int getAnalogChannelMask() {
		return analogChannelMask;
	}

	private void setAnalogChannelMask(int analogChannelMask) {
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

	/**
	 * (from the spec) The voltage supply threshold is set with the V+ command.  If the measured supply voltage falls 
	 * below or equal to this threshold, the supply voltage will be included in the IO sample set.  V+ is 
	 * set to 0 by default (do not include the supply voltage). 

	 * @return
	 */
	public boolean isSupplyVoltageEnabled() {
		return ByteUtils.getBit(this.analogChannelMask, 8);
	}
	
	/**
	 * If digital I/O line (DIO0) is enabled: returns true if digital 0 is HIGH (ON); false if it is LOW (OFF).
	 * If digital I/O line is not enabled this method returns null as it has no value.
	 * 
	 * Digital I/O pins seem to report high when open circuit (unconnected)
	 * 
	 * @return
	 */
	public Boolean isD0On() {
		if (this.isD0Enabled()) {
			return ByteUtils.getBit(dioLsb, 1);	
		}
		
		return null;
	}

	// consider using underscore for readability (isD1_On)
	public Boolean isD1On() {
		if (this.isD1Enabled()) {
			return ByteUtils.getBit(dioLsb, 2);	
		}
		
		return null;
	}
	
	public Boolean isD2On() {
		if (this.isD2Enabled()) {
			return ByteUtils.getBit(dioLsb, 3);	
		}
		
		return null;
	}
	
	public Boolean isD3On() {
		if (this.isD3Enabled()) {
			return ByteUtils.getBit(dioLsb, 4);	
		}
		
		return null;
	}
	
	public Boolean isD4On() {
		if (this.isD4Enabled()) {
			return ByteUtils.getBit(dioLsb, 5);	
		}
		
		return null;
	}

	public Boolean isD5On() {
		if (this.isD5Enabled()) {
			return ByteUtils.getBit(dioLsb, 6);	
		}
		
		return null;
	}
	
	public Boolean isD6On() {
		if (this.isD6Enabled()) {
			return ByteUtils.getBit(dioLsb, 7);	
		}
		
		return null;
	}
	
	public Boolean isD7On() { 
		if (this.isD7Enabled()) {
			return ByteUtils.getBit(dioLsb, 8);	
		}
		
		return null;
	}
	
	public Boolean isD10On() {
		if (this.isD10Enabled()) {
			return ByteUtils.getBit(dioMsb, 3);	
		}
		
		return null;
	}

	public Boolean isD11On() {
		if (this.isD11Enabled()) {
			return ByteUtils.getBit(dioMsb, 4);	
		}
		
		return null;
	}
	
	public Boolean isD12On() {
		if (this.isD12Enabled()) {
			return ByteUtils.getBit(dioMsb, 5);	
		}
		
		return null;
	}	

	/**
	 * Returns true if this sample contains data from digital inputs
	 * 
	 * See manual page 68 for byte bit mapping
	 * 
	 * @return
	 */
	public boolean containsDigital() {
        return this.getDigitalChannelMask1() > 0 || this.getDigitalChannelMask2() > 0;

    }
	
	/**
	 * Returns true if this sample contains data from analog inputs or supply voltage
	 * 
	 * How does supply voltage get enabled??
	 * 
	 * See manual page 68 for byte bit mapping
	 * 
	 * @return
	 */
	public boolean containsAnalog() {
        return this.getAnalogChannelMask() > 0;

    }

	/**
	 * Returns the DIO MSB, only if sample contains digital; null otherwise
	 * 
	 * @return
	 */
	public Integer getDioMsb() {
		return dioMsb;
	}

	private void setDioMsb(Integer dioMsb) {
		this.dioMsb = dioMsb;
	}

	/**
	 * Returns the DIO LSB, only if sample contains digital; null otherwise
	 * 
	 * @return
	 */
	public Integer getDioLsb() {
		return dioLsb;
	}

	private void setDioLsb(Integer dioLsb) {
		this.dioLsb = dioLsb;
	}

	/**
	 * Returns a 10 bit value of ADC line 0, if enabled.
	 * Returns null if ADC line 0 is not enabled.
	 * 
	 * The range of Digi XBee series 2 ADC is 0 - 1.2V and although I couldn't find this in the spec 
	 * a few google searches seems to confirm.  When I connected 3.3V to just one of the ADC pins, it 
	 * displayed it's displeasure by reporting all ADC pins at 1023.
	 * 
	 * Analog pins seem to float around 512 when open circuit
	 * 
	 * The reason this returns null is to prevent bugs in the event that you thought you were reading the 
	 * actual value when the pin is not enabled.
	 * 
	 * @return
	 */
	public Integer getAnalog0() {
		return analog0;
	}

	private void setAnalog0(Integer analog0) {
		this.analog0 = analog0;
	}

	public Integer getAnalog1() {
		return analog1;
	}

	private void setAnalog1(Integer analog1) {
		this.analog1 = analog1;
	}

	public Integer getAnalog2() {
		return analog2;
	}

	private void setAnalog2(Integer analog2) {
		this.analog2 = analog2;
	}

	public Integer getAnalog3() {
		return analog3;
	}

	private void setAnalog3(Integer analog3) {
		this.analog3 = analog3;
	}

	public Integer getSupplyVoltage() {
		return supplyVoltage;
	}

	private void setSupplyVoltage(Integer supplyVoltage) {
		this.supplyVoltage = supplyVoltage;
	}
	
	// TODO this could be simplified with reflection
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(super.toString());
		
		builder.append(",digitalChannelMask1=" + ByteUtils.toBase2(this.getDigitalChannelMask1()));
		builder.append(",digitalChannelMask2=" + ByteUtils.toBase2(this.getDigitalChannelMask2()));
		builder.append(",analogChannelMask=" + ByteUtils.toBase2(this.getAnalogChannelMask()));
		
		if (this.containsDigital()) {
			builder.append(",dioMsb=" + ByteUtils.toBase2(this.getDioMsb()));
			builder.append(",dioLsb=" + ByteUtils.toBase2(this.getDioLsb()));
		
			if (this.isD0Enabled()) {
				builder.append(",D0=" + (this.isD0On() ? "high" : "low"));
			}
			
			if (this.isD1Enabled()) {
				builder.append(",D1=" + (this.isD1On() ? "high" : "low"));
			}

			if (this.isD2Enabled()) {
				builder.append(",D2=" + (this.isD2On() ? "high" : "low"));
			}
			
			if (this.isD3Enabled()) {
				builder.append(",D3=" + (this.isD3On() ? "high" : "low"));
			}
			
			if (this.isD4Enabled()) {
				builder.append(",D4=" + (this.isD4On() ? "high" : "low"));
			}
			
			if (this.isD5Enabled()) {
				builder.append(",D5=" + (this.isD5On() ? "high" : "low"));
			}
			
			if (this.isD6Enabled()) {
				builder.append(",D6=" + (this.isD6On() ? "high" : "low"));
			}
			
			if (this.isD7Enabled()) {
				builder.append(",D7=" + (this.isD7On() ? "high" : "low"));
			}
			
			if (this.isD10Enabled()) {
				builder.append(",D10=" + (this.isD10On() ? "high" : "low"));
			}
			
			if (this.isD11Enabled()) {
				builder.append(",D11=" + (this.isD11On() ? "high" : "low"));
			}	

			if (this.isD12Enabled()) {
				builder.append(",D12=" + (this.isD12On() ? "high" : "low"));
			}
		}
		
		if (this.containsAnalog()) {
			if (this.isA0Enabled()) {
				builder.append(",analog0=" + this.getAnalog0());
			}
			
			if (this.isA1Enabled()) {
				builder.append(",analog1=" + this.getAnalog1());
			}

			if (this.isA2Enabled()) {
				builder.append(",analog2=" + this.getAnalog2());
			}

			if (this.isA3Enabled()) {
				builder.append(",analog3=" + this.getAnalog3());
			}
			
			if (this.isSupplyVoltageEnabled()) {
				builder.append(",supplyVoltage=" + this.getSupplyVoltage());
			}
		}
		
		return builder.toString();
	}
}
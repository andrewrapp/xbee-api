package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.DoubleByte;


/**
 * Represents a double byte XBee Address.
 * 
 * @author andrew
 *
 */
public class XBeePacketLength extends DoubleByte {
	
	/**
	 * Manual says max packet length is 100 bytes so not sure why 2 bytes are needed
	 * 
	 * @param msb
	 * @param lsb
	 */
	public XBeePacketLength(int msb, int lsb) {
		super(msb, lsb);
	}

	public XBeePacketLength(int length) {
		super(length);
	}
	
	public int getLength() {
		return this.get16BitValue();
	}
}

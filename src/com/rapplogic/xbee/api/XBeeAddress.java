package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.ByteUtils;


/**
 * Represents a double byte XBeeApi Address.
 * 
 * @author andrew
 *
 */
public abstract class XBeeAddress {
	

	public XBeeAddress() {
		
	}
	
	public abstract int[] getAddress();
	
	public String toString() {
		return ByteUtils.toBase16(this.getAddress());
	}
}

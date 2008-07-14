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

package com.rapplogic.xbee.api;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.util.ByteUtils;

/**
 * Big Endian container for 64-bit XBee Address
 * 
 * See device addressing in manual p.32
 * 
 * @author andrew
 *
 */
public class XBeeAddress64 extends XBeeAddress  {
	
	private final static Logger log = Logger.getLogger(XBeeAddress64.class);
	
	// broadcast address 0x000000ff
	public static final XBeeAddress64 BROADCAST = new XBeeAddress64(new int[] {0, 0, 0, 0, 0, 0, 0xff, 0xff});
	public static final XBeeAddress64 ZNET_COORDINATOR = new XBeeAddress64(new int[] {0, 0, 0, 0, 0, 0, 0, 0});
	
	private int[] address;

	/**
	 * Parses an 64-bit XBee address from a string representation
	 * Must be in the format "## ## ## ## ## ## ## ##" (i.e. don't use 0x prefix)
	 * 
	 * @param addressStr
	 */
	public XBeeAddress64(String addressStr) {
		StringTokenizer st = new StringTokenizer(addressStr, " ");
		
		address = new int[8];
		
		for (int i = 0; i < address.length; i++) {
			String byteStr = st.nextToken();
			address[i] = Integer.parseInt(byteStr, 16);
			
			log.debug("byte is " + ByteUtils.toBase16(address[i]) + " at pos " + i);
		}
	}
	
	/**
	 * Creates a 64-bit address
	 *  
	 * @param b1 MSB
	 * @param b2
	 * @param b3
	 * @param b4
	 * @param b5
	 * @param b6
	 * @param b7
	 * @param b8 LSB
	 */
	public XBeeAddress64(int b1, int b2, int b3, int b4, int b5, int b6, int b7, int b8) {
		address = new int[8];
		
		address[0] = b1;
		address[1] = b2;
		address[2] = b3;
		address[3] = b4;
		address[4] = b5;
		address[5] = b6;
		address[6] = b7;
		address[7] = b8;
	}

	public XBeeAddress64(int[] address) {
		this.address = address;
	}
	
	public XBeeAddress64() {
		address = new int[8];
	}

	public void setAddress(int[] address) {
		this.address = address;
	}
	
	public boolean equals(Object o) {
		throw new RuntimeException("TODO");
	}

	@Override
	public int[] getAddress() {
		return address;
	}
}

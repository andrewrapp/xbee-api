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

import com.rapplogic.xbee.util.ByteUtils;

/**
 * TODO RX_64_RESPONSE, RX_64_IO_RESPONSE
 * 
 * @author andrew
 *
 */
public abstract class XBeeResponse {
	
	// TODO enum these
	public final static int RX_64_RESPONSE = 0x80;
	public final static int RX_16_RESPONSE = 0x81;
	public final static int RX_64_IO_RESPONSE = 0x82;
	public final static int RX_16_IO_RESPONSE = 0x83;
	public final static int AT_RESPONSE = 0x88;
	public final static int TX_16_STATUS_RESPONSE = 0x89;
	public final static int MODEM_STATUS_RESPONSE = 0x8a;
	public final static int ZNET_RX_RESPONSE = 0x90;
	public final static int ZNET_TX_STATUS_RESPONSE = 0x8b;
	public final static int ZNET_REMOTE_AT_RESPONSE = 0x97;
	public final static int ZNET_IO_SAMPLE_RESPONSE = 0x92;
	public final static int ZNET_IO_NODE_IDENTIFIER_RESPONSE = 0x95;
	
	public final static int ERROR_RESPONSE = -1;

	private int apiId;
	private int checksum;

	private XBeePacketLength length;
	
	private boolean error = false;
		
	public XBeeResponse() {

	}

	public XBeePacketLength getLength() {
		return length;
	}

	public void setLength(XBeePacketLength length) {
		this.length = length;
	}

	public int getApiId() {
		return apiId;
	}

	public void setApiId(int apiId) {
		this.apiId = apiId;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}
	
	/**
	 * If true then this is an instance of ErrorResponse
	 * 
	 * @return
	 */
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
	
	public String toString() {
		return "apiId=" + ByteUtils.toBase16(this.apiId) +
			",length=" + length.get16BitValue() + 
			",checksum=" + ByteUtils.toBase16(checksum) +
			",error=" + this.error;
	}
}
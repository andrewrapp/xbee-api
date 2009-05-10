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
 * 
 * @author andrew
 *
 */
public abstract class XBeeResponse {

	// the raw (escaped) bytes of this packet (minus start byte)
	// this is the most compact representation of the packet;
	// useful for sending the packet over a wire (e.g. xml),
	// for later reconstitution
	private int[] packetBytes;

	private ApiId apiId;
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

	public ApiId getApiId() {
		return apiId;
	}

	public void setApiId(ApiId apiId) {
		this.apiId = apiId;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}
	
	/**
	 * Indicates an error occurred during the parsing of the packet.
	 * This may indicate a bug in this software or in the XBee firmware.
	 * Absence of an error does not indicate the request was successful;
	 * you will need to inspect the status byte of the response object (if available)
	 * to determine success.
	 * 
	 * @return
	 */
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
	
	/**
	 * Returns an array all bytes in packet except the start byte
	 * 
	 * @return
	 */
	public int[] getPacketBytes() {
		return packetBytes;
	}

	public void setPacketBytes(int[] packetBytes) {
		this.packetBytes = packetBytes;
	}
	
	public String toString() {
		return "apiId=" + this.apiId +
			",length=" + length.get16BitValue() + 
			",checksum=" + ByteUtils.toBase16(checksum) +
			",error=" + this.error;
	}
}
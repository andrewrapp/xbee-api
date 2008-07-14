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

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IntArrayOutputStream;

public class ZNetTxRequest extends XBeeRequest {
	
	private final static Logger log = Logger.getLogger(ZNetTxRequest.class);

	public final static int MAX_DATA_SIZE = 72;
	public final static int DEFAULT_BROADCAST_RADIUS = 0;
	
	public final static int UNICAST_OPTION = 0;
	public final static int BROADCAST_OPTION = 0x8;
				
	private XBeeAddress64 destAddr64;
	private XBeeAddress16 destAddr16;
	private int broadcastRadius;
	private int option;
	private int[] payload;
	
	/**
	 * From manual p. 33:
	 * The ZigBee Transmit Request API frame specifies the 64-bit Address and the network address (if
	 * known) that the packet should be sent to. By supplying both addresses, the module will forego
	 * network address Discovery and immediately attempt to route the data packet to the remote. If the
	 * network address of a particular remote changes, network address and route discovery will take
	 * place to establish a new route to the correct node. Upon successful
	 * 
	 * Key points:
	 * 	- always specify the 64-bit address and also specify the 16-bit address, if known.  Set
	 * 	the 16-bit network address to fffe if not known.
	 *  - check the 16-bit address of the tx status response frame as it may change.  keep a
	 *  hash table mapping of 64-bit address to 16-bit network address.
	 *  

	 * @param frameId
	 * @param dest64
	 * @param dest16
	 * @param broadcastRadius
	 * @param option
	 * @param payload
	 */
	public ZNetTxRequest(int frameId, XBeeAddress64 dest64, XBeeAddress16 dest16, int broadcastRadius, int option, int[] payload) {
		this.setFrameId(frameId);
		this.destAddr64 = dest64;
		this.destAddr16 = dest16;
		this.broadcastRadius = broadcastRadius;
		this.option = option;
		this.payload = payload;
	}
	
	public int[] getFrameData() {
		if (payload.length > MAX_DATA_SIZE) {
			throw new IllegalArgumentException("Payload cannot exceed " + MAX_DATA_SIZE + " bytes.  Please package into multiple packets");
		}
		
		IntArrayOutputStream out = new IntArrayOutputStream();
		
		// api id
		out.write(this.getApiId()); 
		
		// frame id (arbitrary byte that will be sent back with ack)
		out.write(this.getFrameId());
		
		// add 64-bit dest address
		out.write(destAddr64.getAddress());
		
		// add 16-bit dest address
		out.write(destAddr16.getAddress());
		
		// write broadcast radius
		out.write(broadcastRadius);
		
		// write options byte
		out.write(option);
		
		out.write(payload);
		
		return out.getIntArray();
	}
	
	public int getApiId() {
		return ZNET_TX_REQUEST;
	}
	
	public XBeeAddress64 getDestAddr64() {
		return destAddr64;
	}

	public void setDestAddr64(XBeeAddress64 destAddr64) {
		this.destAddr64 = destAddr64;
	}

	public XBeeAddress16 getDestAddr16() {
		return destAddr16;
	}

	public void setDestAddr16(XBeeAddress16 destAddr16) {
		this.destAddr16 = destAddr16;
	}

	public int getBroadcastRadius() {
		return broadcastRadius;
	}

	public void setBroadcastRadius(int broadcastRadius) {
		this.broadcastRadius = broadcastRadius;
	}

	public int getOption() {
		return option;
	}

	public void setOption(int option) {
		this.option = option;
	}

	public int[] getPayload() {
		return payload;
	}

	public void setPayload(int[] payload) {
		this.payload = payload;
	}
	
	public String toString() {
		return super.toString() + ",destAddr64=" + this.destAddr64 +
			"destAddr16=" + this.destAddr16 +
			"broadcastRadius=" + this.broadcastRadius + 
			"option=" + this.option +
			"payload=" + ByteUtils.toBase16(this.payload);
	}
}

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

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Constructs frame data portion of a 16-bit transmit request
 * 
 * @author andrew
 *
 */
public class TxRequest16 extends TxRequestBase {
	
	private final static Logger log = Logger.getLogger(TxRequest16.class);
	
	private XBeeAddress16 remoteAddr16;
	
	/**
	 * 16 bit Tx Request with default frame id and awk option
	 * 
	 * @param remoteAddr16
	 * @param payload
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, int[] payload) {
		this(remoteAddr16, DEFAULT_FRAME_ID, Option.DEFAULT_OPTION, payload);
	}
	
	/**
	 * 16 bit Tx Request.
	 *   
	 * Keep in mind that if you programmed the destination address with AT commands, it is in Hex,
	 * so prepend int with 0x (e.g. 0x1234).
	 * 
	 * Payload size is limited to 100 bytes, according to MaxStream documentation.
	 * 
	 * @param remoteAddr16
	 * @param frameId
	 * @param payload
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, int frameId, int[] payload) {
		this(remoteAddr16, frameId, Option.DEFAULT_OPTION, payload);
	}
	
	/**
	 * Note: if option is DISABLE_ACK_OPTION you will not get a ack response and you must use the asynchronous send method
	 * 
	 * @param remoteAddr16
	 * @param frameId
	 * @param payload
	 * @param option
	 */
	public TxRequest16(XBeeAddress16 remoteAddr16, int frameId, Option option, int[] payload) {		
		this.remoteAddr16 = remoteAddr16;
		this.setFrameId(frameId);
		this.setOption(option);
		this.setPayload(payload);
	}

	public int[] getFrameData() {	
		// response does not imply ack
		if (remoteAddr16.equals(XBeeAddress16.BROADCAST) && this.getOption() != Option.DISABLE_ACK_OPTION) {
			throw new RuntimeException("When sending a broadcast packet you cannot get an ACK and so your option must equal: " + Option.DISABLE_ACK_OPTION);
		}
		
		IntArrayOutputStream out = new IntArrayOutputStream();

		// api id
		out.write(this.getApiId().getValue());
		// frame id (arbitrary byte that will be sent back with ack)
		out.write(this.getFrameId());
		// destination address (broadcast is 0xFFFF)
		out.write(remoteAddr16.getAddress());
		// options byte disable ack = 1, send pan id = 4
		out.write(this.getOption().getValue());		
		out.write(this.getPayload());

		return out.getIntArray();	
	}
	
	public ApiId getApiId() {
		return ApiId.TX_REQUEST_16;
	}
	
	public XBeeAddress16 getRemoteAddr16() {
		return remoteAddr16;
	}

	public void setRemoteAddr16(XBeeAddress16 remoteAddr16) {
		this.remoteAddr16 = remoteAddr16;
	}
	
	public String toString() {
		return super.toString() + 
			",remoteAddress16=" + this.remoteAddr16;
	}
}

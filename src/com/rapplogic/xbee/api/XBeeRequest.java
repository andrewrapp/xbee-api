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

/**
 * Constructs frame data portion of an XBee packet
 * 
 * TODO cache frame data and use isDirty check to see if new frame data has been updated
 * 
 * @author andrew
 *
 */
public abstract class XBeeRequest {
	
	// TODO enum these
	public static final int TX_REQUEST_64 = 0;
	public static final int TX_REQUEST_16 = 1;
	public static final int AT_COMMAND = 0x08;
	public static final int AT_COMMAND_QUEUE = 0x09;
	public static final int ZNET_REMOTE_AT_REQUEST = 0x17;
	public static final int ZNET_TX_REQUEST = 0x10;
	
	public static final int DEFAULT_FRAME_ID = 1;
	// XBee will not generate a TX Status Packet if this frame id sent
	public static final int NO_RESPONSE_FRAME_ID = 0;
	
	private int apiId;
	private int frameId;
	
	public XBeeRequest() {
	
	}
	
	public XBeePacket getXBeePacket() {
		int[] frameData = this.getFrameData();
		
		if (frameData == null) {
			throw new RuntimeException("frame data is null");
		}
		
		XBeePacket packet = new XBeePacket(frameData);
		
		return packet;
	}

	public abstract int[] getFrameData();

	public int getApiId() {
		return apiId;
	}

	public int getFrameId() {
		return frameId;
	}
	
	public String toString() {
		return "apiId=" + this.getApiId() + ",frameId=" + this.getFrameId();
	}

	public void setApiId(int apiId) {
		this.apiId = apiId;
	}

	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}
}

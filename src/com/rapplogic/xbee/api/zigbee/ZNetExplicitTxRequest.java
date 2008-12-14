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
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.DoubleByte;
import com.rapplogic.xbee.util.IntArrayOutputStream;

public class ZNetExplicitTxRequest extends ZNetTxRequest {
	
	private final static Logger log = Logger.getLogger(ZNetExplicitTxRequest.class);
	
	private final int reserved = 0;
	
	private int sourceEndpoint;
	private int destinationEndpoint;
	private int clusterId;
	private DoubleByte profileId = new DoubleByte(0xc1, 0x05);
	
	public ZNetExplicitTxRequest(int frameId, XBeeAddress64 dest64, XBeeAddress16 dest16, int broadcastRadius, int option, int[] payload, 	int sourceEndpoint, int destinationEndpoint, int clusterId) {
		super(frameId, dest64, dest16, broadcastRadius, option, payload);
		this.sourceEndpoint = sourceEndpoint;
		this.destinationEndpoint = destinationEndpoint;
		this.clusterId = clusterId;
	}
	
	/**
	 * Gets frame data from tx request (super) and inserts necessary bytes
	 */
	public int[] getFrameData() {
		
		// get frame id from tx request
		IntArrayOutputStream frameData = this.getFrameDataAsIntArrayOutputStream();
		
		// overwrite api id
		frameData.getInternalList().set(0, this.getApiId());
		
		// insert explicit bytes
		
		// source endpoint
		frameData.getInternalList().add(12, this.getSourceEndpoint());
		// dest endpoint
		frameData.getInternalList().add(13, this.getDestinationEndpoint());
		// reserved byte
		frameData.getInternalList().add(14, this.getReserved());
		// cluster id
		frameData.getInternalList().add(15, this.getClusterId());
		// profile id
		frameData.getInternalList().add(16, this.getProfileId().getMsb());
		frameData.getInternalList().add(17, this.getProfileId().getLsb());
		
		log.debug("frameData is " + ByteUtils.toBase16(frameData.getIntArray()));
		
		return frameData.getIntArray();
	}
	
	public int getApiId() {
		return ZNET_EXPLICIT_TX_REQUEST;
	}

	public int getReserved() {
		return reserved;
	}

	public int getSourceEndpoint() {
		return sourceEndpoint;
	}

	public void setSourceEndpoint(int sourceEndpoint) {
		this.sourceEndpoint = sourceEndpoint;
	}

	public int getDestinationEndpoint() {
		return destinationEndpoint;
	}

	public void setDestinationEndpoint(int destinationEndpoint) {
		this.destinationEndpoint = destinationEndpoint;
	}

	public int getClusterId() {
		return clusterId;
	}

	public void setClusterId(int clusterId) {
		this.clusterId = clusterId;
	}

	public DoubleByte getProfileId() {
		return profileId;
	}

	public void setProfileId(DoubleByte profileId) {
		this.profileId = profileId;
	}
	
	public String toString() {
		return super.toString() + 
			",sourceEndpoint=" + ByteUtils.toBase16(this.getSourceEndpoint()) +
			",destinationEndpoint=" + ByteUtils.toBase16(this.getDestinationEndpoint()) +
			",reserved byte=" + ByteUtils.toBase16(this.getReserved()) +
			",clusterId=" + ByteUtils.toBase16(this.getClusterId()) + 
			",profileId (MSB)=" + ByteUtils.toBase16(this.getProfileId().getMsb()) +
			",profileId (LSB)=" + ByteUtils.toBase16(this.getProfileId().getLsb());
	}
}

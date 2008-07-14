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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeFrameIdResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IntArrayInputStream;

public class ZNetRemoteAtResponse extends XBeeFrameIdResponse {
	
	public enum Status {
		OK (0),
		ERROR (1),
		INVALID_COMMAND (2),
		INVALID_PARAMETER (3);

		private static final Map<Integer,Status> lookup = new HashMap<Integer,Status>();
		
		static {
			for(Status s : EnumSet.allOf(Status.class)) {
				lookup.put(s.getValue(), s);
			}
		}
		
		public static Status get(int value) { 
			return lookup.get(value); 
		}
		
	    private final int value;
	    
	    Status(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}
	
	private XBeeAddress64 remoteAddress64;
	private XBeeAddress16 remoteAddress16;
	// TODO need ATCommand class with enum of commands instead of boring old string
	private String commandName;
	private Status status;
	private int[] commandData;
	
	public ZNetRemoteAtResponse() {

	}

	public boolean isSixteenBitAddressUnknown() {
		return remoteAddress16.getMsb() == 0xff && remoteAddress16.getLsb() == 0xfe;
	}

	public XBeeAddress64 getRemoteAddress64() {
		return remoteAddress64;
	}

	public void setRemoteAddress64(
			XBeeAddress64 sixtyFourBitResponderAddress) {
		this.remoteAddress64 = sixtyFourBitResponderAddress;
	}

	public XBeeAddress16 getRemoteAddress16() {
		return remoteAddress16;
	}

	public void setRemoteAddress16(
			XBeeAddress16 sixteenBitResponderAddress) {
		this.remoteAddress16 = sixteenBitResponderAddress;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int[] getCommandData() {
		return commandData;
	}

	public void setCommandData(int[] commandData) {
		this.commandData = commandData;
	}
	
	public boolean isOk() {
		return status == Status.OK;
	}
	
	/**
	 * Returns the sample portion of a ZNetRxIoSampleResponse, based on a IS (force sample) response.
	 * Only the sample data is populated; other fields are null or default.
	 * 
	 * @return
	 * @throws IOException
	 */
	public ZNetRxIoSampleResponse parseIsSample() throws IOException {
		
		if (!this.commandName.equals("IS")) {
			throw new RuntimeException("This method is only applicable to the IS command");
		}
		
		IntArrayInputStream in = new IntArrayInputStream(this.getCommandData());
		ZNetRxIoSampleResponse sample = new ZNetRxIoSampleResponse();
		sample.parse(in);
		
		return sample;
	}
	
	public String toString() {
		
		return super.toString() +
			",remoteAddress64=" + this.remoteAddress64 +
			",remoteAddress16=" + this.remoteAddress16 +
			",command=" + this.commandName +
			",status=" + this.status + 
			",commandReponse=" + ByteUtils.toBase16(this.commandData);
	}
}
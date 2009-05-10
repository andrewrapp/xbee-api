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

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Allows AT commands to be sent to a remote radio.
 * Warning: this command does not return a response if the remote radio is unreachable.
 * You will need to set your own timeout when waiting for a response from this command,
 * or you may wait forever.
 * 
 * TODO now supported by series 1.  Need to move up to api package and refactor to support both radio types
 * 
 * @author andrew
 *
 */
public class ZNetRemoteAtRequest extends AtCommand {
	
	private final static Logger log = Logger.getLogger(ZNetRemoteAtRequest.class);
	
	private XBeeAddress64 remoteAddr64;
	private XBeeAddress16 remoteAddr16;
	private boolean applyChanges;
	
	/**
	 * Creates a Remote AT request for setting an AT command on a remote XBee
	 * 
	 * Note: When setting a value, you must set applyChanges for the setting to
	 * take effect.  When sending several requests, you can wait until the last
	 * request before setting applyChanges=true.
	 * 
	 * @param frameId
	 * @param dest64
	 * @param dest16
	 * @param applyChanges set to true if setting a value or issuing a command that changes the state of the radio (e.g. FR); not applicable to query requests 
	 * @param command two character AT command to set or query
	 * @param value if null then the current setting will be queried
	 */
	public ZNetRemoteAtRequest(int frameId, XBeeAddress64 dest64, XBeeAddress16 dest16, boolean applyChanges, String command, int[] value) {
		super(command, value);
		this.setFrameId(frameId);
		this.remoteAddr64 = dest64;
		this.remoteAddr16 = dest16;
		this.applyChanges = applyChanges;
	}
	
	/**
	 * Creates a Remote AT request for querying the current value of an AT command on a remote XBee
	 * 
	 * @param frameId
	 * @param macAddress
	 * @param znetAddress
	 * @param applyChanges
	 * @param command
	 */
	public ZNetRemoteAtRequest(int frameId, XBeeAddress64 macAddress, XBeeAddress16 znetAddress, boolean applyChanges, String command) {
		this(frameId, macAddress, znetAddress, applyChanges, command, null);
	}

	/**
	 * Abbreviated Constructor for setting an AT command on a remote XBee.
	 * This defaults to the DEFAULT_FRAME_ID, and true for apply changes
	 * 
	 * @param dest64
	 * @param command
	 * @param value
	 */
	public ZNetRemoteAtRequest(XBeeAddress64 dest64, String command, int[] value) {
		this(XBeeRequest.DEFAULT_FRAME_ID, dest64, XBeeAddress16.ZNET_BROADCAST, true, command, value);
	}

	/**
	 * Abbreviated Constructor for querying the value of an AT command on a remote XBee.
	 * This defaults to the DEFAULT_FRAME_ID, and true for apply changes
	 * 
	 * @param dest64
	 * @param command
	 */
	public ZNetRemoteAtRequest(XBeeAddress64 dest64, String command) {
		this(XBeeRequest.DEFAULT_FRAME_ID, dest64, XBeeAddress16.ZNET_BROADCAST, true, command, null);
	}
	
	public int[] getFrameData() {		
		IntArrayOutputStream out = new IntArrayOutputStream();
		
		// api id
		out.write(this.getApiId().getValue());
		// frame id (arbitrary byte that will be sent back with ack)
		out.write(this.getFrameId());
		
		out.write(remoteAddr64.getAddress());
		
		// 16-bit address
		out.write(remoteAddr16.getAddress());
		
		if (applyChanges) {
			out.write(2);	
		} else {
			// queue changes -- don't forget to send AC command
			out.write(0);
		}
		 
		// command name ascii [1]
		out.write((int) this.getCommand().substring(0, 1).toCharArray()[0]);
		// command name ascii [2]
		out.write((int) this.getCommand().substring(1, 2).toCharArray()[0]);
	
		if (this.getValue() != null) {
			out.write(this.getValue());
		}

		return out.getIntArray();
	}
	
	public ApiId getApiId() {
		return ApiId.ZNET_REMOTE_AT_REQUEST;
	}
	
	public XBeeAddress64 getRemoteAddr64() {
		return remoteAddr64;
	}

	public void setRemoteAddr64(XBeeAddress64 remoteAddr64) {
		this.remoteAddr64 = remoteAddr64;
	}

	public XBeeAddress16 getRemoteAddr16() {
		return remoteAddr16;
	}

	public void setRemoteAddr16(XBeeAddress16 remoteAddr16) {
		this.remoteAddr16 = remoteAddr16;
	}

	public boolean isApplyChanges() {
		return applyChanges;
	}

	public void setApplyChanges(boolean applyChanges) {
		this.applyChanges = applyChanges;
	}
	
	public String toString() {
		return super.toString() + 
			",remoteAddr64=" + this.remoteAddr64 +
			",remoteAddr16=" + this.remoteAddr16 +
			",applyChanges=" + this.applyChanges;
	}
}

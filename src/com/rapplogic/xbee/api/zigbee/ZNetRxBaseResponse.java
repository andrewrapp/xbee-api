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

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;

/**
 * Series 2 XBee.  Super class of all Receive packets.
 * <p/>
 * Note: ZNet RX packets do not include RSSI since it is a mesh network and potentially requires several
 * hops to get to the destination.  The RSSI of the last hop is available using the DB AT command.
 * If your network is not mesh (i.e. composed of a single coordinator and end devices -- no routers) 
 * then the DB command should provide accurate RSSI.
 * <p/> 
 * @author Andrew Rapp
 *
 */
public abstract class ZNetRxBaseResponse extends XBeeResponse {

	private final static Logger log = Logger.getLogger(ZNetRxBaseResponse.class);
	
	public enum Option {
//		0x01 - Packet Acknowledged
//		0x02 - Packet was a broadcast packet
//		0x20 - Packet encrypted with APS encryption
//		0x40 - Packet was sent from an end device (if known)
//		Note: Option values can be combined. For example, a 
//		0x40 and a 0x01 will show as a 0x41. Other possible 
//		values 0x21, 0x22, 0x41, 0x42, 0x60, 0x61, 0x62.
		
		// TODO ugh this is mess now with bitfield indicators
		// TODO ditch the enum, and replace with a class that has isBroadcast(), isPacketAcknowledged() etc
		
		PACKET_ACKNOWLEDGED (0x01),
		BROADCAST_PACKET (0x02),
		PACKET_ENCRYPTED_WITH_APS (0x20),
		PACKET_SENT_FROM_END_DEVICE(0x40),
		PACKET_ACKNOWLEDGED_0x21 (0x21),
		PACKET_ACKNOWLEDGED_0x41 (0x41),
		PACKET_ACKNOWLEDGED_0x61 (0x61),
		PACKET_ENCRYPTED_WITH_APS_PACKET_SENT_FROM_END_DEVICE (0x60),
		BROADCAST_PACKET_0x22 (0x22),
		BROADCAST_PACKET_0x42 (0x42),
		BROADCAST_PACKET_0x62 (0x62),
		UNKNOWN(-1);
		
		private static final Map<Integer,Option> lookup = new HashMap<Integer,Option>();
		
		static {
			for(Option s : EnumSet.allOf(Option.class)) {
				lookup.put(s.getValue(), s);
			}
		}
		
		public static Option get(int value) { 
			return lookup.get(value); 
		}
		
	    private final int value;
	    
	    Option(int value) {
	        this.value = value;
	    }

		public int getValue() {
			return value;
		}
	}
	
	// TODO where is frame id??
	
	private XBeeAddress64 remoteAddress64;
	private XBeeAddress16 remoteAddress16;
	private Option option;
	
	public ZNetRxBaseResponse() {

	}

	public XBeeAddress64 getRemoteAddress64() {
		return remoteAddress64;
	}

	public void setRemoteAddress64(XBeeAddress64 remoteAddress64) {
		this.remoteAddress64 = remoteAddress64;
	}

	public XBeeAddress16 getRemoteAddress16() {
		return remoteAddress16;
	}

	public void setRemoteAddress16(XBeeAddress16 remoteAddress16) {
		this.remoteAddress16 = remoteAddress16;
	}
	
	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
		this.option = option;
	}
	
	protected void parseAddress(IPacketParser parser) throws IOException {
		this.setRemoteAddress64(parser.parseAddress64());
		this.setRemoteAddress16(parser.parseAddress16());		
	}

	protected static Option getOption(int option) {
		if (Option.get(option) != null) {
			return Option.get(option);	
		} else {
			log.warn("Unknown response option " + option);
			return Option.UNKNOWN;
		}		
	}
	
	protected void parseOption(IPacketParser parser) throws IOException {
		int option = parser.read("ZNet RX Response Option");
		this.setOption(this.getOption(option));		
	}
	
	public String toString() {
		return super.toString() +
			",remoteAddress64=" + this.remoteAddress64 +
			",remoteAddress16=" + this.remoteAddress16 +
			",option=" + this.option;
	}
}
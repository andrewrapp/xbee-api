package com.rapplogic.xbee.api.zigbee;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;

/**
 *  Note: ZNet RX packets do not include RSSI since it is a mesh network, requiring several
 *  hops to get to a destination.  The RSSI of the last hop is available using the DB AT command.
 *  If your network is not mesh (i.e. composed of a single coordinator and end devices -- no routers) 
 *  then the DB command should provide accurate RSSI.
 *  
 * @author Andrew Rapp
 *
 */
public class ZNetRxBaseResponse extends XBeeResponse {

	public enum Option {
		PACKET_ACKNOWLEDGED (0x01),
		BROADCAST_PACKET (0x02);
		
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
	
	public String toString() {
		return super.toString() +
			",remoteAddress64=" + this.remoteAddress64 +
			",remoteAddress16=" + this.remoteAddress16 +
			",option=" + this.option;
	}
}
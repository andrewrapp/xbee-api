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
import com.rapplogic.xbee.api.XBeeFrameIdResponse;

/**
 * Series 2 XBee.  This is sent out the UART of the transmitting XBee immediately following
 * a Transmit packet.  Indicates if the Transmit packet (ZNetTxRequest)
 * was successful.
 * <p/>
 * API ID: 0x8b
 * <p/>
 * @author andrew
 */
public class ZNetTxStatusResponse extends XBeeFrameIdResponse {
	
	private final static Logger log = Logger.getLogger(ZNetTxStatusResponse.class);

	public enum DeliveryStatus {
//		0x00 = Success
//		0x01 = MAC ACK Failure
//		0x02 = CCA Failure
//		0x15 = Invalid destination
//		endpoint
//		0x21 = Network ACK Failure
//		0x22 = Not Joined to Network
//		0x23 = Self-addressed
//		0x24 = Address Not Found
//		0x25 = Route Not Found
//		0x26 = Broadcast source failed to hear a neighbor relay 
//		the message 
//		0x2B = Invalid binding table index
//		0x2C = Resource error lack of free buffers, timers, etc.
//		0x2D = Attempted broadcast with APS transmission
//		0x2E = Attempted unicast with APS transmission, but 
//		EE=0
//		0x32 = Resource error lack of free
//		0x74 = Data payload too large
		
		SUCCESS (0),
		MAC_FAILURE (1),
		CCA_FAILURE (0x02),
		INVALID_DESTINATION_ENDPOINT (0x15),
		NETWORK_ACK_FAILURE (0x21),
		NOT_JOINED_TO_NETWORK (0x22),
		SELF_ADDRESSED (0x23),
		ADDRESS_NOT_FOUND (0x24),
		ROUTE_NOT_FOUND (0x25),
		BROADCAST_SOURCE_NEIGHBOR_FAILURE (0x26),
		INVALID_BINDING_TABLE_INDEX (0x2B),
		RESOURCE_ERROR_LACK_FREE_BUFFERS (0x2C),
		ATTEMPTED_BROADCAST_WITH_APS_TX (0x2D),
		ATTEMPTED_UNICAST_WITH_APS_TX_EE_ZERO (0x2E),
		RESOURCE_ERROR_LACK_FREE_BUFFERS_0x32 (0x32), // WUT, SAME AS 2C
		PAYLOAD_TOO_LARGE(0x74), // ZB Pro firmware only
		UNKNOWN(-1);
		
		private static final Map<Integer,DeliveryStatus> lookup = new HashMap<Integer,DeliveryStatus>();
		
		static {
			for(DeliveryStatus s : EnumSet.allOf(DeliveryStatus.class)) {
				lookup.put(s.getValue(), s);
			}
		}
		
	    private final int value;
	    
	    DeliveryStatus(int value) {
	        this.value = value;
	    }

		public static DeliveryStatus get(int value) { 
			return lookup.get(value); 
		}
		
		public int getValue() {
			return value;
		}
	}

	public enum DiscoveryStatus {
//		0x00 = No Discovery	Overhead
//		0x01 = Address Discovery
//		0x02 = Route Discovery
//		0x03 = Address and Route
//		0x40 = Extended Timeout Discovery
				
		// NOTE 0x40 IS A bit field so going to be painful with enums
		
		NO_DISCOVERY (0),
		ADDRESS_DISCOVERY (1),
		ROUTE_DISCOVERY (2),
		ADDRESS_AND_ROUTE_DISCOVERY (3),
		EXTENDED_TIMEOUT_DISCOVERY (0x40),
		EXTENDED_TIMEOUT_DISCOVERY_0x41 (0x41),
		EXTENDED_TIMEOUT_DISCOVERY_0x42 (0x42),
		EXTENDED_TIMEOUT_DISCOVERY_0x43 (0x43),
		UNKNOWN(-1);

		private static final Map<Integer,DiscoveryStatus> lookup = new HashMap<Integer,DiscoveryStatus>();
		
		static {
			for(DiscoveryStatus s : EnumSet.allOf(DiscoveryStatus.class)) {
				lookup.put(s.getValue(), s);
			}
		}
		
	    private final int value;
	    
	    DiscoveryStatus(int value) {
	        this.value = value;
	    }

		public static DiscoveryStatus get(int value) { 
			return lookup.get(value); 
		}
		
		public int getValue() {
			return value;
		}
	}

	private XBeeAddress16 remoteAddress16;
	private int retryCount;
	private DeliveryStatus deliveryStatus;
	private DiscoveryStatus discoveryStatus;
	
	
	public ZNetTxStatusResponse() {

	}

	public XBeeAddress16 getRemoteAddress16() {
		return remoteAddress16;
	}


	public void setRemoteAddress16(XBeeAddress16 remoteAddress) {
		this.remoteAddress16 = remoteAddress;
	}


	public int getRetryCount() {
		return retryCount;
	}


	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public DeliveryStatus getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public DiscoveryStatus getDiscoveryStatus() {
		return discoveryStatus;
	}

	public void setDiscoveryStatus(DiscoveryStatus discoveryStatus) {
		this.discoveryStatus = discoveryStatus;
	}
	
	/**
	 * Returns true if the delivery status is SUCCESS
	 * 
	 * @return
	 */
	public boolean isSuccess() {
		return this.getDeliveryStatus() == DeliveryStatus.SUCCESS;
	}
	
	public void parse(IPacketParser parser) throws IOException {		
		this.setFrameId(parser.read("ZNet Tx Status Frame Id"));

		this.setRemoteAddress16(parser.parseAddress16());
		this.setRetryCount(parser.read("ZNet Tx Status Tx Count"));
		
		int deliveryStatus = parser.read("ZNet Tx Status Delivery Status");
		
		if (DeliveryStatus.get(deliveryStatus) != null) {
			this.setDeliveryStatus(DeliveryStatus.get(deliveryStatus));	
		} else {
			log.warn("Unknown delivery status " + deliveryStatus);
			this.setDeliveryStatus(DeliveryStatus.UNKNOWN);
		}

		int discoveryStatus = parser.read("ZNet Tx Status Discovery Status");
		
		if (DiscoveryStatus.get(discoveryStatus) != null) {
			this.setDiscoveryStatus(DiscoveryStatus.get(discoveryStatus));	
		} else {
			log.warn("Unknown discovery status " + discoveryStatus);
			this.setDiscoveryStatus(DiscoveryStatus.UNKNOWN);
		}
	}
	
	public String toString() {
		return super.toString() + 
		",remoteAddress16=" + this.remoteAddress16 +
		",retryCount=" + this.retryCount +
		",deliveryStatus=" + this.deliveryStatus + 
		",discoveryStatus=" + this.discoveryStatus;
	}
}
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.rapplogic.xbee.util.ByteUtils;

public enum ApiId {
	TX_REQUEST_64 (0x0),
	TX_REQUEST_16 (0x1),
	AT_COMMAND (0x08),
	AT_COMMAND_QUEUE (0x09),
	ZNET_REMOTE_AT_REQUEST (0x17),
	ZNET_TX_REQUEST (0x10),
	ZNET_EXPLICIT_TX_REQUEST (0x11),
	RX_64_RESPONSE (0x80),
	RX_16_RESPONSE (0x81),
	RX_64_IO_RESPONSE (0x82),
	RX_16_IO_RESPONSE (0x83),
	AT_RESPONSE (0x88),
	TX_16_STATUS_RESPONSE (0x89),
	MODEM_STATUS_RESPONSE (0x8a),
	ZNET_RX_RESPONSE (0x90),
	ZNET_EXPLICIT_RX_RESPONSE (0x91),
	ZNET_TX_STATUS_RESPONSE (0x8b),
	ZNET_REMOTE_AT_RESPONSE (0x97),
	ZNET_IO_SAMPLE_RESPONSE (0x92),
	ZNET_IO_NODE_IDENTIFIER_RESPONSE (0x95),
	ERROR_RESPONSE (-1);
	
	private static final Map<Integer,ApiId> lookup = new HashMap<Integer,ApiId>();
	
	static {
		for(ApiId s : EnumSet.allOf(ApiId.class)) {
			lookup.put(s.getValue(), s);
		}
	}
	
	public static ApiId get(int value) { 
		return lookup.get(value); 
	}
	
    private final int value;
    
    ApiId(int value) {
        this.value = value;
    }

	public int getValue() {
		return value;
	}
	
	public String toString() {
		return this.name() + " (" + ByteUtils.toBase16(this.getValue()) + ")";
	}
}

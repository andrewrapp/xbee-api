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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.util.ByteUtils;

public abstract class TxRequestBase extends XBeeRequest {
	
	private final static Logger log = Logger.getLogger(TxRequestBase.class);
	
	// based on manual.  test this!
	public final static int MAX_PAYLOAD_SIZE = 100;
	
	public enum Option {
		
		DEFAULT_OPTION (0),
		DISABLE_ACK_OPTION (1),
		BROADCAST_OPTIONI(4);
		
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
	
	private int[] payload;
	private Option option;

	public int[] getPayload() {
		return payload;
	}

	public void setPayload(int[] payload) {
		// TODO verify max payload size?
	
		if (payload.length > MAX_PAYLOAD_SIZE) {
			throw new IllegalArgumentException("Payload cannot exceed " + MAX_PAYLOAD_SIZE + " bytes.  Please re-package into multiple packets");
		}
		
		this.payload = payload;
	}

	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
		this.option = option;
	}
	
	public String toString() {
		return super.toString() + ",option=" + this.option + 
			",payload=" + ByteUtils.toBase16(this.payload);
	}
}

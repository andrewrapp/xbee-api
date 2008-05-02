package com.rapplogic.xbee.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Andrew Rapp
 *
 */
public class ModemStatusResponse extends XBeeResponse {
		
	public enum Status {
		HARDWARE_RESET (0),
		WATCHDOG_TIMER_RESET (1),
		ASSOCIATED (2),
		DISASSOCIATED (3),
		SYNCHRONIZATION_LOST (4),
		COORDINATOR_REALIGNMENT (5),
		COORDINATOR_STARTED (6);
		
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
	
	private Status status;
	
	public ModemStatusResponse() {

	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String toString() {
		return super.toString() + ",status=" + this.status;
	}
}
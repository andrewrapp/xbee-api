package com.rapplogic.xbee.api.wpan;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.rapplogic.xbee.api.XBeeFrameIdResponse;


public class TxStatusResponse extends XBeeFrameIdResponse {
	
	public enum Status {
	
		SUCCESS (0),
		NO_ACK (1),
		CCA_FAILURE(2),
		PURGED(3);
		
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
	
	public TxStatusResponse() {

	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public boolean isSuccess() {
		return this.status == Status.SUCCESS;
	}

	public boolean isError() {
		return this.status != Status.SUCCESS;
	}
		
	public boolean isAckError() {
		return this.status == Status.NO_ACK;
	}
	
	public boolean isCcaError() {
		return this.status == Status.CCA_FAILURE;
	}
	
	public boolean isPurged() {
		return this.status == Status.PURGED;
	}
	
	public String toString() {
		return super.toString() + ",status=" + this.getStatus();
	}
}
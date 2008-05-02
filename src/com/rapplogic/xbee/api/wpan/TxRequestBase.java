package com.rapplogic.xbee.api.wpan;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.util.ByteUtils;


/**
 * Constructs frame data portion of a 16-bit transmit request
 * 
 * @author andrew
 *
 */
public abstract class TxRequestBase extends XBeeRequest {
	
	private final static Logger log = Logger.getLogger(TxRequestBase.class);
	
	public enum Option {
		
		DEFAULT_OPTION (1),
		DISABLE_ACK_OPTION (2),
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
		this.payload = payload;
	}

	public Option getOption() {
		return option;
	}

	public void setOption(Option option) {
		this.option = option;
	}
	
	/**
	 * This is just for validation
	 */
	public int[] getFrameData() {		
		if (payload.length > 100) {
			throw new IllegalArgumentException("Payload cannot exceed 100 bytes.  Please package into multiple packets");
		}
		
		return null;
	}
	
	public String toString() {
		return super.toString() + ",option=" + this.option + 
			",payload=" + ByteUtils.toBase16(this.payload);
	}
}

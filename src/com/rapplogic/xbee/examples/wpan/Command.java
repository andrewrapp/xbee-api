package com.rapplogic.xbee.examples.wpan;

/**
 * This goes along with XBeePing/Pong
 * 
 * @author andrew
 *
 */
public class Command {

	public final static String COMMAND = "C";
	public final static String ACK = "A";
	
	private String type;
	private Integer sequence;
	
	public Command(String type, Integer sequence) {
		this.type = type;
		this.sequence = sequence;
	}

	public String getType() {
		return type;
	}

	public Integer getSequence() {
		return sequence;
	}
	
	public static Command parse(String str) {
		String type = str.substring(0, 1);
		Integer sequence = new Integer(str.substring(1));
		
		return new Command(type, sequence);
	}
	
	public String toString() {
		return "Type=" + type + ",Sequence=" + sequence;
	}
}

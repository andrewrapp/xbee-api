package com.rapplogic.xbee.api;



/**
 * TODO test
 * 
 * @author andrew
 *
 */
public class AtCommandQueue extends AtCommand {

	public AtCommandQueue(String command) {
		this(command, null, DEFAULT_FRAME_ID);
	}
	
	public AtCommandQueue(String command, int[] value, int frameId) {
		super(command, value, frameId);
	}

	public int getApiId() {
		return AT_COMMAND_QUEUE;
	}
}

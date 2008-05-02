package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * API technique to set/query commands
 * 
 * WARNING: Any changes made will not survive a power cycle unless written to memory with WR command
 * According to the manual, the WR command can only be written so many times.. however many that is.
 * 
 * @author andrew
 *
 */
public class AtCommand extends XBeeRequest {
	
	private String command;
	private int[] value;
	
	public AtCommand(String command) {
		this(command, null, DEFAULT_FRAME_ID);
	}
	
	public AtCommand(String command, int value) {
		this(command, new int[] {value}, DEFAULT_FRAME_ID);		
	}

	public AtCommand(String command, int value[]) {
		this(command, value, DEFAULT_FRAME_ID);
	}
	
	/**
	 * Warning: frameId must be > 0 for a response
	 * 
	 * @param command
	 * @param value
	 * @param frameId
	 */
	public AtCommand(String command, int[] value, int frameId) {
		this.command = command;
		this.value = value;
		this.setFrameId(frameId);
	}

	public int[] getFrameData() {
		if (command.length() > 2) {
			throw new IllegalArgumentException("Command should be two characters.  Do not include AT prefix");
		}
		
		IntArrayOutputStream out = new IntArrayOutputStream();
		
		// api id
		out.write(this.getApiId());
		// frame id
		out.write(this.getFrameId());
		// at command byte 1
		out.write((int) command.substring(0, 1).toCharArray()[0]);
		// at command byte 2
		out.write((int) command.substring(1, 2).toCharArray()[0]);

		// int value is up to four bytes to represent command value
		if (value != null) {		
			out.write(value);
		}
		
		return out.getIntArray();
	}

	public int getApiId() {
		return AT_COMMAND;
	}
	
	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int[] getValue() {
		return value;
	}

	public void setValue(int[] value) {
		this.value = value;
	}
	
	public String toString() {
		return super.toString() +
			",command=" + this.command +
			",value=" + (value == null ? "null" : ByteUtils.toBase16(value));
	}
}

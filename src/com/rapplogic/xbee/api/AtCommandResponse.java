package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.ByteUtils;


public class AtCommandResponse extends XBeeFrameIdResponse {
	
	// TODO enum this
	public final static int OK = 0;
	public final static int ERROR = 1;
	
	private int char1;
	private int char2;
	private int status;
	// response value msb to lsb
	private int[] value;
	
	public AtCommandResponse() {

	}

	public int getChar1() {
		return char1;
	}


	public void setChar1(int char1) {
		this.char1 = char1;
	}


	public int getChar2() {
		return char2;
	}


	public void setChar2(int char2) {
		this.char2 = char2;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isOk() {
		return status == OK;
	}

	public int[] getValue() {
		return value;
	}
	
	public void setValue(int[] data) {
		this.value = data;
	}

	public String getCommand() {
		return String.valueOf((char)this.char1) + String.valueOf((char)this.char2);
	}
	
	public String toString() {
		return super.toString() + 
			",status=" + this.getStatus() + ",value=" + 
			(this.value == null ? "null" : ByteUtils.toBase16(this.getValue()));
	}
}
package com.rapplogic.xbee.util;


public class IntArrayInputStream implements IIntArrayInputStream {

	private int[] source;
	private int pos;
	
	public IntArrayInputStream(int[] source) {
		this.source = source;
	}
	
	public int read() {
		return source[pos++];
	}
	
	public int[] read(int size) {
		int[] block = new int[size];
		System.arraycopy(source, pos, block, 0, size);
		// index pos
		pos+=size;
		return block;
	}
	
	public int read(String s) {
		return read();
	}
}

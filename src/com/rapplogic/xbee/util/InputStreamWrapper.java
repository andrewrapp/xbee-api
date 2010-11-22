package com.rapplogic.xbee.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper implements IIntInputStream {

	private InputStream in;
	
	public InputStreamWrapper(InputStream in) {
		this.in = in;
	}
	
	public int read() throws IOException {
		return in.read();
	}
	
	public int read(String s) throws IOException {
		return in.read();
	}
}

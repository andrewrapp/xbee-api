package com.rapplogic.xbee.util;

import java.io.IOException;

public interface IIntArrayInputStream {
	public int read() throws IOException;
	public int read(String s) throws IOException;
}

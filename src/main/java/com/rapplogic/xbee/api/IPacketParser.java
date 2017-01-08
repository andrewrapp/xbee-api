package com.rapplogic.xbee.api;

import java.io.IOException;

public interface IPacketParser {
	int read(String context) throws IOException;
	int[] readRemainingBytes() throws IOException;
	int getFrameDataBytesRead();
	int getRemainingBytes();
	int getBytesRead();
	XBeePacketLength getLength();
	ApiId getApiId();
	int getIntApiId();
	// TODO move to util
	XBeeAddress16 parseAddress16() throws IOException;
	XBeeAddress64 parseAddress64() throws IOException;
}

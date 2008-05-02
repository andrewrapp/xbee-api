package com.rapplogic.xbee.api;

public interface XBeePacketHandler {
	public void handlePacket(XBeeResponse response);
	public void error(Throwable th);
}

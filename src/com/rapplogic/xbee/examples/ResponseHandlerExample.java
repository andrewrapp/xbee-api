package com.rapplogic.xbee.examples;

import java.io.IOException;

import com.rapplogic.xbee.api.IPacketParser;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;

public class ResponseHandlerExample {

	public ResponseHandlerExample() {
		XBee xbee = new XBee();
		xbee.registerResponseHandler(0x88, MyResponse.class);
//		xbee.open(..);
	}
	
	public static class MyResponse extends XBeeResponse {

		@Override
		protected void parse(IPacketParser parser) throws IOException {
//			this.setxxx(parser.read("AT Response Frame Id"));
//			this.setxxy(parser.read("AT Response Char 1"));
//			this.setxxz(parser.read("AT Response Char 2"));			
		}
	}
}

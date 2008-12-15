/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee.examples.zigbee;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.ErrorResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.DoubleByte;


public class ZNetExplicitSenderTest {

	private final static Logger log = Logger.getLogger(ZNetExplicitSenderTest.class);
	
	// ZB firmware config:
	// sleep disabled is not supported in ZB firmware!!!! set to Pin sleep
	// ATSM 1
	// now connect pin 9 to ground to prevent sleep.
	// set AO 1.  once set, you must use explicit packets instead of plain vanilla tx requests.  you can still send a tx request but it will be receive as a explicit response
	// ATAO 1
	private ZNetExplicitSenderTest() throws XBeeException {
		
		XBee xbee = new XBee();
		
		try {
			// replace with your com port and baud rate. this is the com port of my coordinator
			//xbee.open("COM5", 9600);
			xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			
			// replace with end device's 64-bit address (SH + SL)
			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
			
			// create an array of arbitrary data to send
			int[] payload = new int[] { 0, 0x66, 0xee };
			
			// loopback test
			int sourceEndpoint = 0;
			int destinationEndpoint = ZNetExplicitTxRequest.Endpoint.DATA.getValue();
			
			DoubleByte clusterId = new DoubleByte(0x0, ZNetExplicitTxRequest.ClusterId.SERIAL_LOOPBACK.getValue());
			//DoubleByte clusterId = new DoubleByte(0x0, ZNetExplicitTxRequest.ClusterId.TRANSPARENT_SERIAL.getValue());
			
			// first request we just send 64-bit address.  we get 16-bit network address with status response
			ZNetExplicitTxRequest request = new ZNetExplicitTxRequest(xbee.getNextFrameId(), addr64, XBeeAddress16.ZNET_BROADCAST, 
						ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.UNICAST_OPTION, payload, sourceEndpoint, destinationEndpoint, clusterId, ZNetExplicitTxRequest.znetProfileId);
			
			log.info("sending explicit " + request.toString());
			
			while (true) {
				xbee.sendAsynchronous(request);
				
				XBeeResponse response = xbee.getResponse();
				
				log.info("received response " + response.toString());
					
				try {
					// wait a bit then send another packet
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		} finally {
			xbee.close();
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException  {
		PropertyConfigurator.configure("log4j.properties");
		new ZNetExplicitSenderTest();
	}
}

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

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;


public class ZNetSynchronousSendTest {

	private final static Logger log = Logger.getLogger(ZNetSynchronousSendTest.class);

	private ZNetSynchronousSendTest() throws XBeeException {
		
		XBee xbee = new XBee();
		
		try {
			// replace with your com port and baud rate. this is the com port of my coordinator
			//xbee.open("COM5", 9600);
			// my coordinator com/baud
			xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			// my end device
			//xbee.open("/dev/tty.usbserial-A6005uRz", 9600);
			
			// replace with end device's 64-bit address (SH + SL)
			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
			
			// create an array of arbitrary data to send
			int[] payload = new int[] { 0x01, 0x02 };
			
			// first request we just send 64-bit address.  we get 16-bit network address with status response
			ZNetTxRequest request = new ZNetTxRequest(xbee.getNextFrameId(), addr64, XBeeAddress16.ZNET_BROADCAST, ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.UNICAST_OPTION, payload);
			
			while (true) {
				long start = System.currentTimeMillis();
				
				log.info("sending tx packet: " + request.toString());
				
				XBeeResponse response = null;
				
				try {
					// update frame id
					request.setFrameId(xbee.getNextFrameId());
					log.info("updating request with frameId: " + request.getFrameId());
					
					response = xbee.sendSynchronous(request, 5000);

					log.info("received response in " + (System.currentTimeMillis() - start) + ", " + response.toString());

					ZNetTxStatusResponse txStatus = (ZNetTxStatusResponse) response;

					if (txStatus.getDeliveryStatus() != ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
						log.error("packet failed. status: " + txStatus.getDeliveryStatus());
					}	

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {	}
					
				} catch (XBeeTimeoutException to) {
					log.error("request timed out");
				}
			}

		} finally {
			xbee.close();
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException  {
		PropertyConfigurator.configure("log4j.properties");
		new ZNetSynchronousSendTest();
	}
}

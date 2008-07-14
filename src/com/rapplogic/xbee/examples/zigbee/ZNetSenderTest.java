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
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * TODO Send DN command to get 64-bit address from NI
 * 
 * @author andrew
 *
 */
public class ZNetSenderTest {

	private final static Logger log = Logger.getLogger(ZNetSenderTest.class);
	
	private ZNetSenderTest() throws XBeeException {
		
		XBee xbee = new XBee();
		
		try {
			xbee.open("COM5", 9600);			
			
			// end device 64-bit address
			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
			
			// first request we just send 64-bit address.  we get 16-bit network address with status response
			
			ZNetTxRequest request = new ZNetTxRequest(xbee.getNextFrameId(), addr64, XBeeAddress16.ZNET_BROADCAST, ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.UNICAST_OPTION, new int[] { 0x01, 0x02 });
			
			while (true) {
				long start = System.currentTimeMillis();
				//log.info("sending tx packet: " + request.toString());
				xbee.sendAsynchronous(request);
				// update frame id
				request.setFrameId(xbee.getNextFrameId());
				
				XBeeResponse response = xbee.getResponse();
				
				log.info("received response " + response.toString());
				
				if (response.getApiId() == XBeeResponse.ZNET_TX_STATUS_RESPONSE) {
					ZNetTxStatusResponse txStatus = (ZNetTxStatusResponse) response;

					if (txStatus.getRemoteAddress16().equals(XBeeAddress16.ZNET_BROADCAST)) {
						// specify 16-bit address for faster routing?.. really only need to do this when it changes
						request.setDestAddr16(txStatus.getRemoteAddress16());
					}				
					
					log.info("Response in " + (System.currentTimeMillis() - start) + ", Delivery status is " + txStatus.getDeliveryStatus() + ", 16-bit address is " + ByteUtils.toBase16(txStatus.getRemoteAddress16().getAddress()) + ", retry count is " +  txStatus.getRetryCount() + ", discovery status is " + txStatus.getDeliveryStatus());
				} else {
					log.info("ignoring unexpected packet " + response.toString());
				}
	
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}

		} finally {
			xbee.close();
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException  {
		PropertyConfigurator.configure("log4j.properties");
		new ZNetSenderTest();
	}
}

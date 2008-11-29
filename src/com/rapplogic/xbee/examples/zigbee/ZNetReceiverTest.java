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

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtRequest;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * This class is the companion to ZNetSenderTest.java, and as such, it receives packets sent by ZNetSenderTest.java
 * See the ZNetSenderTest.java for information on how to configure your XBee for this demo
 * 
 * If you want to test receiving RX packets from the coordinator, connect to the end device and run
 * 
 * If you want to test receiving I/O samples from an end device, connect to your coordinator and run
 * 
 * You can start ZNetSenderTest.java and this class in any order but it's generally best to start this class first.
 * 
 * @author andrew
 *
 */
public class ZNetReceiverTest {

	private final static Logger log = Logger.getLogger(ZNetReceiverTest.class);
	
	private ZNetReceiverTest() throws Exception {
		XBee xbee = new XBee();		

		try {			
			// replace with the com port or your receiving XBee
			// my coordinator com/baud
			xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			
			// this is the com port of my end device on my mac
			//xbee.open("/dev/tty.usbserial-A6005uRz", 9600);
			
			while (true) {

				try {
					// we wait here until a packet is received.
					XBeeResponse response = xbee.getResponse();
					
					log.info("received response " + response.toString());
					
					if (response.getApiId() == XBeeResponse.ZNET_RX_RESPONSE) {
						// we received a packet from ZNetSenderTest.java
						ZNetRxResponse rx = (ZNetRxResponse) response;
						
						log.info("Received RX packet, option is " + rx.getOption() + ", sender 64 address is " + ByteUtils.toBase16(rx.getRemoteAddress64().getAddress()) + ", remote 16-bit address is " + ByteUtils.toBase16(rx.getRemoteAddress16().getAddress()) + ", data is " + ByteUtils.toBase16(rx.getData()));

						// optionally we may want to get the signal strength (RSSI) my network is only single hop, but if you have routers you could see multple hops
						AtCommand at = new AtCommand("DB");
						xbee.sendAsynchronous(at);
						XBeeResponse atResponse = xbee.getResponse();
						
						if (atResponse.getApiId() == XBeeResponse.AT_RESPONSE) {
							log.info("RSSI of last response is " + ((AtCommandResponse)atResponse).getValue()[0]);
						} else {
							// we didn't get an AT response
							log.info("expected RSSI, but received " + atResponse.toString());
						}
					} else if (response.getApiId() == XBeeResponse.ZNET_IO_SAMPLE_RESPONSE) {
						// This is a I/O sample response.  You will only get this is you are connected to a Coordinator that is configured to
						// receive I/O samples from a remote XBee.
						
						ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;
						
						log.debug("received i/o sample packet.  contains analog is " + ioSample.containsAnalog() + ", contains digital is " + ioSample.containsDigital());
						
						// check the value of the input pins
//						log.debug("analog1 is " + ioSample.getAnalog1());
//						log.debug("digital 2 is " + ioSample.isD1On());
						
						
						// ZNet radios allow us to force a sample, instead of waiting for the remote to send us one
						// the following code shows how you can request a sample (force sample) on a remote xbee
					
						// end device 64-bit address
//						XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
						
//						ZNetRemoteAtRequest sample = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, XBeeAddress16.ZNET_BROADCAST, false, "IS");
//						xbee.sendAsynchronous(sample);
//						XBeeResponse sampleResponse = xbee.getResponse();
//						
//						if (sampleResponse.getApiId() == XBeeResponse.ZNET_REMOTE_AT_RESPONSE && ((ZNetRemoteAtResponse)sampleResponse).isOk())  {
//							// TODO need to parse IS response into IO sample
//							ZNetRemoteAtResponse remoteAt = (ZNetRemoteAtResponse)sampleResponse;
//							log.info("sample rate response succeeded. command data is " + ByteUtils.toBase16(((ZNetRemoteAtResponse)remoteAt).getCommandData()));
//							log.info("sample analog1 is " + remoteAt.parseIsSample().getAnalog1());
//						} else {
//							log.info("either it failed or received something else");
//						}
					} else {
						log.debug("received unexpected packet " + response.toString());
					}
				} catch (Exception e) {
					log.error(e);
				}
			}
		} finally {
			xbee.close();
		}
	}

	public static void main(String[] args) throws Exception {
		// init log4j
		PropertyConfigurator.configure("log4j.properties");
		new ZNetReceiverTest();
	}
}

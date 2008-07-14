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

public class ZNetReceiverTest {

	private final static Logger log = Logger.getLogger(ZNetReceiverTest.class);
	
	private ZNetReceiverTest() throws Exception {
		XBee xbee = new XBee();		

		try {			
			//xbee.open("/dev/tty.usbserial-A4004Rim", 9600);
			xbee.open("COM6", 9600);
			
			while (true) {

				try {
					XBeeResponse response = xbee.getResponse();
					
					log.info("received response " + response.toString());
					
					if (response.getApiId() == XBeeResponse.ZNET_RX_RESPONSE) {
						ZNetRxResponse rx = (ZNetRxResponse) response;
						
						log.info("Received RX packet, option is " + rx.getOption() + ", sender 64 address is " + ByteUtils.toBase16(rx.getRemoteAddress64().getAddress()) + ", remote 16-bit address is " + ByteUtils.toBase16(rx.getRemoteAddress16().getAddress()) + ", data is " + ByteUtils.toBase16(rx.getData()));

						// get RSSI (my network is only single hop)
						AtCommand at = new AtCommand("DB");
						xbee.sendAsynchronous(at);
						XBeeResponse atResponse = xbee.getResponse();
						
						if (atResponse.getApiId() == XBeeResponse.AT_RESPONSE) {
							log.info("RSSI of last response is " + ((AtCommandResponse)atResponse).getValue()[0]);
						} else {
							log.info("expected RSSI, but received " + atResponse.toString());
						}
					} else if (response.getApiId() == XBeeResponse.ZNET_IO_SAMPLE_RESPONSE) {
						ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;
						
						log.debug("received i/o sample packet.  contains analog is " + ioSample.containsAnalog() + ", contains digital is " + ioSample.containsDigital() + ", analog1 is " + ioSample.getAnalog1() + ", digital2 is " + ioSample.isD1On());
					
						// end device 64-bit address
						XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
					
						// send force sample
						ZNetRemoteAtRequest sample = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, XBeeAddress16.ZNET_BROADCAST, false, "IS");
						xbee.sendAsynchronous(sample);
						XBeeResponse sampleResponse = xbee.getResponse();
						
						if (sampleResponse.getApiId() == XBeeResponse.ZNET_REMOTE_AT_RESPONSE && ((ZNetRemoteAtResponse)sampleResponse).isOk())  {
							// TODO need to parser IS response into IO sample
							ZNetRemoteAtResponse remoteAt = (ZNetRemoteAtResponse)sampleResponse;
							log.info("sample rate response succeeded. command data is " + ByteUtils.toBase16(((ZNetRemoteAtResponse)remoteAt).getCommandData()));
							log.info("sample analog1 is " + remoteAt.parseIsSample().getAnalog1());
						} else {
							log.info("either it failed or received something else");
						}
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

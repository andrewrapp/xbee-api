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

import com.rapplogic.xbee.api.ErrorResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * To run this example you need to have at least two ZNet XBees powered up and configured to the same PAN ID (ATID) in API mode (2).
 * This software requires the XBee to be configured in API mode; if your ZNet radios are flashed with the transparent (AT) firmware, 
 * you will need to re-flash with API firmware to run this software.
 * 
 * I use the Digi X-CTU software to configure my XBee's, but if you don't have a PC, you can still use the configureCoordinator and
 * configureEndDevice methods in ZNetAtCommandTest.java.
 * 
 * There are a few chicken and egg situations where you need to know some basic configuration before you can connect to the XBee.  This
 * includes the baud rate and the API mode.  The default baud rate is 9600 and if you ever change it, you will want to remember the setting.  
 * If you can't connect at 9600 and you don't know the baud rate, try all possibilities until it works.  Same with the API Mode: if you click
 * Test/Query in X-CTU, try changing the API mode until it succeeds, then write it down somewhere for next time.
 *  
 * Here's my setup configuration (assumes factory configuration):
 * 
 * COORDINATOR config:
 * 
 * - Reset to factory settings: 
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI COORDINATOR
 * - Save config
 * ATWR
 * - reboot
 * ATFR
 *
 * The XBee network will assign the network 16-bit MY address.  The coordinator MY address is always 0
 * 
 * X-CTU tells me my SH Address is 00 13 a2 00 and SL is 40 0a 3e 02
 * 
 * END DEVICE config:
 * 
 * - Reset to factory settings: 
 * ATRE
 * - Put in API mode 2
 * ATAP 2
 * - Set PAN id to arbitrary value
 * ATID 1AAA
 * - Set the Node Identifier (give it a meaningful name)
 * ATNI END_DEVICE_1
 * - Save config
 * ATWR
 * - reboot
 * ATFR
 *   
 * Only one XBee needs to be connected to the computer (serial-usb); the other may be remote, but can also be connected to the computer.
 * I use the XBee Explorer from SparkFun to connect my XBees to my computer as it makes it incredibly easy -- just drop in the XBee.
 * 
 * For this example, I use my XBee COORDINATOR as my "sender" (runs this class) and the END DEVICE as my "receiver" XBee.
 * You could alternatively use your END DEVICE as the sender -- it doesn't matter because any XBee, either configured as a COORDINATOR
 * or END DEVICE, can both send and receive.
 * 
 * How to find the COM port:
 * 
 * Java is nice in that it runs on many platforms.  I use mac/windows and linux (server) and the com port is different on all there.
 * On the mac it appears as /dev/tty.usbserial-A6005v5M on my machine.  I just plug in each XBee one at a time and check the /dev dir
 * to match the XBee to the device name: ls -l /dev/tty.u (hit tab twice to so all entries)
 *
 * On Windows you can simply select Start->My Computer->Manage, select Device Manager and expand "Ports" 
 * 
 * For Linux I'm not exactly sure just yet although I found mine by trial and error to be /dev/ttyUSB0  I think it could easily be different
 * for other distros.
 * 
 * To run, simply right-click on the class, in the left pane, and select Run As->Java Application.  Eclipse will let you run multiple
 * processes in one IDE, but only the last process will display output in the console.  If you want to run both the ZNet sender and receiver
 * examples and see log messages for both, run one on a separate computer or start another instance of eclipse.
 * 
 * Note: I haven't tried the dual eclipse approach, but seems like it should work as long as eclipse doesn't complain that another 
 * eclipse is already running.  If you are running the sender and receiver in the same eclipse, hit the terminate button twice to kill both
 * or you won't be able to start it again.  If this situation occurs, simply restart eclipse.
 * 
 * Be sure to hit the "Terminate" red square button before running again.
 * 
 * @author andrew
 */
public class ZNetSenderTest {

	private final static Logger log = Logger.getLogger(ZNetSenderTest.class);
	
	private ZNetSenderTest() throws XBeeException {
		
		XBee xbee = new XBee();
		
		try {
			// replace with your com port and baud rate. this is the com port of my coordinator
			//xbee.open("COM5", 9600);
			xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			
			// replace with end device's 64-bit address (SH + SL)
			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
			
			// create an array of arbitrary data to send
			int[] payload = new int[] { 0x01, 0x02 };
			
//			// testing max payload size
//			int bigPacket = ZNetTxRequest.MAX_DATA_SIZE;
//			int[] payload = new int[bigPacket];
//			for (int i = 0; i < bigPacket; i++) {
//				payload[i] = 0x0;
//			}
			
			// first request we just send 64-bit address.  we get 16-bit network address with status response
			ZNetTxRequest request = new ZNetTxRequest(xbee.getNextFrameId(), addr64, XBeeAddress16.ZNET_BROADCAST, ZNetTxRequest.DEFAULT_BROADCAST_RADIUS, ZNetTxRequest.UNICAST_OPTION, payload);
			
			while (true) {
				long start = System.currentTimeMillis();
				//log.info("sending tx packet: " + request.toString());
				xbee.sendAsynchronous(request);
				// update frame id
				request.setFrameId(xbee.getNextFrameId());
				
				XBeeResponse response = xbee.getResponse();
				
				log.info("received response " + response.toString());
				
				if (response.isError()) {
					log.error("we got a bad packet", ((ErrorResponse)response).getException());
				} else if (response.getApiId() == XBeeResponse.ZNET_TX_STATUS_RESPONSE) {
					// a tx status response is returned by the local xbee
					ZNetTxStatusResponse txStatus = (ZNetTxStatusResponse) response;

					if (txStatus.getDeliveryStatus() == ZNetTxStatusResponse.DeliveryStatus.SUCCESS) {
						// the packet was successfully delivered
						if (txStatus.getRemoteAddress16().equals(XBeeAddress16.ZNET_BROADCAST)) {
							// specify 16-bit address for faster routing?.. really only need to do this when it changes
							request.setDestAddr16(txStatus.getRemoteAddress16());
						}							
					} else {
						// packet failed.  log error
						// it's easy to create this error by unplugging/powering off your remote xbee.  when doing so I get: packet failed due to error: ADDRESS_NOT_FOUND  
						log.error("packet failed due to error: " + txStatus.getDeliveryStatus());
					}
					
					// I get the following message: Response in 75, Delivery status is SUCCESS, 16-bit address is 0x08 0xe5, retry count is 0, discovery status is SUCCESS 
					log.info("Response in " + (System.currentTimeMillis() - start) + ", Delivery status is " + txStatus.getDeliveryStatus() + ", 16-bit address is " + ByteUtils.toBase16(txStatus.getRemoteAddress16().getAddress()) + ", retry count is " +  txStatus.getRetryCount() + ", discovery status is " + txStatus.getDeliveryStatus());
				} else {
					log.info("ignoring unexpected packet " + response.toString());
				}
	
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
		new ZNetSenderTest();
	}
}

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

package com.rapplogic.xbee.examples.wpan;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeePacket;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

/**
 * Sends a TX Request every 500 ms and waits for TX status packet.
 * If the radio is sending samples it will continue to wait for tx status.
 * 
 * Sender is COM5 on my machine
 * 
 * @author andrew
 * 
 */
public class ApiSenderTest {

	private final static Logger log = Logger.getLogger(ApiSenderTest.class);

	private ApiSenderTest() throws Exception {

		XBee xbee = new XBee();
		xbee.setSendSynchronousTimeout(20000);

		final int sleep = 500;

		int count = 0;
		int errors = 0;
		int ackErrors = 0;
		int ccaErrors = 0;
		int purgeErrors = 0;

		long now;
		
		try {
			xbee.open("COM15", 9600);

			while (true) {

				// log.debug("Sending count " + count);
				// XBeeResponse response = xbee.sendTxRequest16(destination, 0x0a, payload);

				int frameId = 0x13;
				// int[] payload = new int[] {1,2,3,4,5,6,7,8};
				// to verify correct byte escaping, we'll send a start byte
				int[] payload = new int[] { XBeePacket.START_BYTE };

				XBeeAddress16 destination = new XBeeAddress16(0x56, 0x78);
				TxRequest16 tx = new TxRequest16(destination, frameId, payload);
				
				now = System.currentTimeMillis();
				xbee.sendAsynchronous(tx);

				XBeeResponse response = null;

				while (true) {
					// blocks until we get response
					response = xbee.getResponse();

					if (response.getApiId() != XBeeResponse.TX_16_STATUS_RESPONSE) {
						log.debug("expected tx status but received " + response.toString());
					} else {
//						log.debug("got tx status");

						if (((TxStatusResponse) response).getFrameId() != frameId) {
							throw new RuntimeException("frame id does not match");
						}

						if (((TxStatusResponse) response).getStatus() != TxStatusResponse.Status.SUCCESS) {
							errors++;

							if (((TxStatusResponse) response).isAckError()) {
								ackErrors++;
							} else if (((TxStatusResponse) response).isCcaError()) {
								ccaErrors++;
							} else if (((TxStatusResponse) response).isPurged()) {
								purgeErrors++;
							}

							log.debug("Tx status failure with status: " + ((TxStatusResponse) response).getStatus());
						} else {
							// success
							log.debug("Success.  count is " + count + ", errors is " + errors + ", in " + (System.currentTimeMillis() - now) + ", ack errors "
									+ ackErrors + ", ccaErrors " + ccaErrors + ", purge errors " + purgeErrors);
						}

						count++;

						break;
					}
				}

				Thread.sleep(sleep);
			}
		} finally {
			xbee.close();
		}
	}

	public static void main(String[] args) throws Exception {
		// init log4j
		PropertyConfigurator.configure("log4j.properties");
		new ApiSenderTest();
	}
}

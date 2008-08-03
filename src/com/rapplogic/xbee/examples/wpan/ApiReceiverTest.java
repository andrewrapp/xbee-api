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

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.ErrorResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

/**
 * Receives IO samples from remote radio
 * I have a photoresistor connected to analog0 and a thermistor is connected to analog1
 * Also there is a breadboard switch connected to digital2 with change detect configured
 * 
 * @author andrew
 * 
 */
public class ApiReceiverTest {

	private final static Logger log = Logger.getLogger(ApiReceiverTest.class);

	private long last = System.currentTimeMillis();
	
	private ApiReceiverTest() throws Exception {
		XBee xbee = new XBee();		
		
		int count = 0;
		int errors = 0;

		try {			
			xbee.open("COM15", 9600);
			
			while (true) {

				try {
					XBeeResponse response = xbee.getResponse();
					count++;
					
					if (response.isError()) {
						log.info("response contains errors", ((ErrorResponse)response).getException());
						errors++;
					}
					
					if (response.getApiId() == XBeeResponse.RX_16_IO_RESPONSE) {
						RxResponseIoSample ioSample = (RxResponseIoSample)response;
						
						if (ioSample.containsAnalog()) {
							int rssi = ioSample.getRssi();
							int photo = ioSample.getSamples()[0].getAnalog0();
							int temp = ioSample.getSamples()[0].getAnalog1();
							 
							log.info("10-bit temp reading is " + temp + ", temp is " + this.getTemperatureFahrenheit(temp, 3.28, 9855.0) + ", rssi is " + rssi);
							log.info("10-bit photo sensor reading is " + photo);								
						} else {
							// afaik there isn't a way to detect change detect (CD) packets except that they don't send analog data, so turn on a analog pin, even if not using to detect CD
							log.info("change detect: breadboard switch is " + (ioSample.getSamples()[0].isD2On() ? "on" : "off"));
						}
		
						// TODO take moving average to determine trend
						// TODO upload to google spreadsheet to use visualization api
					} else if (response.getApiId() == XBeeResponse.TX_16_STATUS_RESPONSE) {
						
						TxStatusResponse txResponse = (TxStatusResponse) response;
						
						if (txResponse.getStatus() != TxStatusResponse.Status.SUCCESS) {
							errors++;
							
							if (txResponse.isAckError()) {
								log.error("Ack error");
							} else if (txResponse.isCcaError()) {
								log.error("cca error");
							} else if (txResponse.isPurged()) {
								log.error("purge error");
							}
						} else {
							//log.info("Received Tx status packet");
						}
					} else if (response.getApiId() == XBeeResponse.RX_16_RESPONSE) {
						RxResponse rxResponse = (RxResponse) response;
						
						if (rxResponse.getData().length == 2 && rxResponse.getData()[0] == 0xff && rxResponse.getData()[1] == 0xaa) {
							log.info("Received Arduino Thump RX packet " + rxResponse.toString());						
						} else {
							log.info("Received unknown RX packet " + rxResponse.toString());
						}
					} else {
						log.info("Ignoring mystery packet " + response.toString());
					}
					
					// Send a tx packet every 15 seconds just for fun
					
					// this is an example of sending a TX 16 request while we are receiving data
					// after sending this packet we while get a TX_16_STATUS_RESPONSE
					if (System.currentTimeMillis() - last > 15000) {
						// send a control byte
						int[] payload = new int[] {0x11};
						XBeeAddress16 destination = new XBeeAddress16(0x56, 0x78);
						TxRequest16 tx = new TxRequest16(destination, 0x13, payload);
//						log.info("Sending tx request");
						xbee.sendAsynchronous(tx);
						
						last = System.currentTimeMillis(); 
					}

					log.debug("Received response: " + response.toString() + ", count is " + count + ", errors is " + errors);
				} catch (Exception e) {
					log.error(e);
				}
			}
		} finally {
			xbee.close();
		}
	}

	/**
	 * Stole from arduino client
	 * 
	 * @param v10bit
	 * @param vSupply
	 * @param pdRes
	 * @return
	 */
	public double getTemperatureFahrenheit(double v10bit, double vSupply,
			double pdRes) {

		// based on 700+ indoor readings over 10 degree range
		// closer to 2 near 72F and closer to 1.2 near 65, with std dev of .27
		final double calibrationOffset = -1.34;

		double vActual = vSupply * v10bit / 1024;

		// System.out.println("vread is " + vActual);
		// System.out.println("vsupply is " + vSupply);

		double thermResistance = pdRes * (vSupply - vActual) / vActual;

		// System.out.println("therm res is " + thermResistance);

		double thermRefResistance = 10000.0;

		// Steinhart and Hart constants for Vishay thermistor NTCLE100E3103JB0
		double a = 3.354016 * Math.pow(10, -3);
		double b = 2.56985 * Math.pow(10, -4);
		double c = 2.620131 * Math.pow(10, -6);
		double d = 6.383091 * Math.pow(10, -8);

		double celcius = 1.0 / (a + b
				* Math.log(thermResistance / thermRefResistance) + c
				* Math.pow(Math.log(thermResistance / thermRefResistance), 2) + d
				* Math.pow(Math.log(thermResistance / thermRefResistance), 3)) - 273;

		double fahrenheit = 9.0 / 5.0 * celcius + 32 + calibrationOffset;

		return fahrenheit;
	}

	public static void main(String[] args) throws Exception {

		// init log4j
		Properties props = new Properties();
		props.load(new FileInputStream("log4j.properties"));
		PropertyConfigurator.configure(props);

		new ApiReceiverTest();
	}
}

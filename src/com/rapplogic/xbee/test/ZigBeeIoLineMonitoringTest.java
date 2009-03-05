/**
 * Copyright (c) 2009 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API
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

package com.rapplogic.xbee.test;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.XBeePin;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

/**
 * Series 2 test harness for verifying i/o pin functionality
 * 
 * TODO split test/examples code into src/test, src/examples
 * 
 * @author andrew
 *
 */
public class ZigBeeIoLineMonitoringTest implements PacketListener {
	
	private final static Logger log = Logger.getLogger(ZigBeeIoLineMonitoringTest.class);
	
	private XBeeResponse response;
	
	private XBee xbee = new XBee();
	
	public static void main(String[] args) throws XBeeException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		new ZigBeeIoLineMonitoringTest();
	}

	public ZigBeeIoLineMonitoringTest() throws XBeeException, InterruptedException {
		
	
		try {
			xbee.addPacketListener(this);
			
			// first connect directly to end device and configure.  then comment out configureXXX methods and connect to coordinator
			// my end device
			xbee.open("/dev/tty.usbserial-A6005uRz", 9600);
			
			// my coordinator com/baud
			//xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			
//			this.configureForDioTest();
//			this.configureForAnalogTest();
			
			// wait and receive
			while (true) {
				synchronized(this) {
					this.wait();
				}
				
				try {
					ZNetRxIoSampleResponse io = (ZNetRxIoSampleResponse) response;
					log.debug("received sample " + io.toString());
				} catch (Exception e) {
					log.error("failed to convert response to ZNetRxIoSampleResponse", e);
				}
			}
			
		} finally {
			try {
				xbee.close();
			} catch (Exception e) {}
		}
	}

	public void configureForAnalogTest() throws InterruptedException, XBeeException {
		// configure all digital input capable pins and change detect
		// must connect gateway directly to end device since I didn't use remote at
		for (XBeePin pin : XBeePin.getZigBeePins()) {
			if (pin.getCapabilities().contains(XBeePin.Capability.ANALOG_INPUT)) {
				log.info("configuring pin " + pin.getAtCommand() + " to analog input. physical pin is " + pin.getPin());
				AtCommand diRequest = new AtCommand(pin.getAtCommand(), new int[] {XBeePin.Capability.ANALOG_INPUT.getValue()});
				this.sendRequestAndWait(diRequest);
			}
		}
		
		// send sample every 10 seconds
		AtCommand irRequest = new AtCommand("IR", new int[] {0x27, 0x10});
		this.sendRequestAndWait(irRequest);

		// testing: now connect GND to each DIO pin and it will go low and then high when GND is removed
	}
	
	public void configureForDioTest() throws InterruptedException, XBeeException {
		// configure all digital input capable pins and change detect
		// must connect gateway directly to end device since I didn't use remote at
		for (XBeePin pin : XBeePin.getZigBeePins()) {
			if (pin.getCapabilities().contains(XBeePin.Capability.DIGITAL_INPUT)) {
				log.info("configuring pin " + pin.getAtCommand() + " to digital input. physical pin is " + pin.getPin());
				AtCommand diRequest = new AtCommand(pin.getAtCommand(), new int[] {XBeePin.Capability.DIGITAL_INPUT.getValue()});
				this.sendRequestAndWait(diRequest);
			}
		}
		
		// Just noticed the usb explorer provides 2 pins (an extra GND and 5V) on opposite sides and ends of board
		// the extra pin is adjacent pin 10 (pin 11 = 5V) and pin 20 (pin 21 = GND) 
		
		// turn change detection on all DIO pins
		AtCommand cdRequest = new AtCommand("IC", new int[] {0xff, 0xff});
		this.sendRequestAndWait(cdRequest);

		// testing: now connect GND to each DIO pin and it will go low and then high when GND is removed
	}
	
	public void sendRequestAndWait(XBeeRequest request) throws InterruptedException, XBeeException {
		synchronized (this) {			
			xbee.sendAsynchronous(request);
			// wait a max of 10 seconds for response
			this.wait(10000);
			
			if (response != null && !response.isError()) {
				if (((AtCommandResponse)response).getStatus() != AtCommandResponse.Status.OK) {
					throw new RuntimeException("At command failed");
				}
				
				log.debug("ok");
			} else {
				throw new RuntimeException("didn't get a response back from remote at or got failure");
			}
		}
	}
	
	/**
	 * Called by XBee when a packet is received
	 */
	public void processResponse(XBeeResponse response) {
		synchronized(this) {
			log.debug("received response " + response);
			this.response = response;
			this.notify();		
		}
	}
}

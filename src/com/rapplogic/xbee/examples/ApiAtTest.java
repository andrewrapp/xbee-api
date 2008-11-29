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

package com.rapplogic.xbee.examples;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.AssociationStatus;
import com.rapplogic.xbee.api.zigbee.NodeDiscover;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * The AtCommand/AtCommandResponse classes are supported by both ZNet and WPAN XBees but certain
 * commands are specific to ZNet or WPAN.  
 * 
 * Commands that are ZNet specific are located in the ZNetApiAtTest.
 * 
 * Refer to the manual for more information on available commands
 * 
 * TODO split class in to WPAN class
 * 
 * @author andrew
 *
 */
public class ApiAtTest {

	private final static Logger log = Logger.getLogger(ApiAtTest.class);
	
	private XBee xbee = new XBee();
	
	public ApiAtTest() throws XBeeException {
			
		try {
			// replace with port and baud rate of your XBee
			//xbee.open("COM6", 9600);	
			// my coordinator com/baud
			//xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			// my end device
			xbee.open("/dev/tty.usbserial-A6005uRz", 9600);

			
//			// set D1 analog input
//			this.sendCommand(new AtCommand("D1", 2));
//			// set D2 digital input
//			this.sendCommand(new AtCommand("D2", 3));
//			// send sample every 5 seconds
//			this.sendCommand(new AtCommand("IR", new int[] {0x13, 0x88}));
			
			log.info("MY is " + xbee.sendAtCommand(new AtCommand("MY")));
//			log.info("SH is " + xbee.sendAtCommand(new AtCommand("SH")));
//			log.info("SL is " + xbee.sendAtCommand(new AtCommand("SL")));
			
		} finally {
			xbee.close();
		}
	}
	
	// use sparingly!!!!
	public void save() throws XBeeException {
		xbee.sendAsynchronous(new AtCommand("WR"));
		this.logResponse(xbee.getResponse());
	}
	
	public void sendCommand(AtCommand at) throws XBeeException {
		xbee.sendAsynchronous(at);
		this.logResponse(xbee.getResponse());
	}
	
	public AtCommandResponse getAtResponse(AtCommand at) throws XBeeException {
		xbee.sendAsynchronous(at);
		return (AtCommandResponse) xbee.getResponse();
	}

	public void logResponse(XBeeResponse response) {
		try {
			AtCommandResponse atResponse = (AtCommandResponse) response;
			log.info("response success is " + atResponse.isOk() + ", command issued is " + atResponse.getCommand() + ", command value is [" + ByteUtils.toBase16(atResponse.getValue()) + "]");
		} catch (ClassCastException e) {
			log.error("Expected AT command response but instead got " + response.toString());
		}
	}
	
	public static void main(String[] args) throws XBeeException {
		PropertyConfigurator.configure("log4j.properties");
		new ApiAtTest();
	}
}

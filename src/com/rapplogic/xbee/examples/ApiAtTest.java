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
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * The AtCommand/AtCommandResponse classes are supported by both znet and wpan XBees but certain
 * commands are specific to znet or wpan.
 * 
 * @author andrew
 *
 */
public class ApiAtTest {

	private final static Logger log = Logger.getLogger(ApiAtTest.class);
	
	private XBee xbee = new XBee();
	
	private ApiAtTest() throws XBeeException {
			
		try {
			xbee.open("COM6", 9600);	
			
//			this.sendCommand(new AtCommand("AP"));
//			this.sendCommand(new AtCommand("NI"));
			
//			// set D1 analog input
//			this.sendCommand(new AtCommand("D1", 2));
//			// set D2 digital input
//			this.sendCommand(new AtCommand("D2", 3));
//			// send sample every 5 seconds
//			this.sendCommand(new AtCommand("IR", new int[] {0x13, 0x88}));
			
			log.info("MY is " + xbee.sendAtCommand(new AtCommand("MY")));
//			log.info("SH is " + xbee.sendAtCommand(new AtCommand("SH")));
//			log.info("SL is " + xbee.sendAtCommand(new AtCommand("SL")));
			
			// Send node discover
//			xbee.sendAsynchronous(new AtCommand("ND"));
			
			// I just have one end device
//			XBeeResponse response = xbee.getResponse(true);
//			log.info("received response: " + response.toString());
//
//			NodeDiscover nd = NodeDiscover.parse((AtCommandResponse)response);
//			log.info("nd is " + nd.toString());
		} finally {
			xbee.close();
		}
	}
	
	// use sparingly!!!!
	private void save() throws XBeeException {
		xbee.sendAsynchronous(new AtCommand("WR"));
		this.logResponse(xbee.getResponse());
	}
	
	private void sendCommand(AtCommand at) throws XBeeException {
		xbee.sendAsynchronous(at);
		this.logResponse(xbee.getResponse());
	}
	
	private void logResponse(XBeeResponse response) {
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

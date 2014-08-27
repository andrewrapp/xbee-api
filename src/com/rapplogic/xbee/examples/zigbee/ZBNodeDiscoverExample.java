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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZBNodeDiscover;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * Example of performing a node discover for Series 2 XBees.
 * You must connect to the coordinator to run this example and
 * have one or more end device/routers that are associated.
 * 
 * @author andrew
 *
 */
public class ZBNodeDiscoverExample {

	private final static Logger log = Logger.getLogger(ZBNodeDiscoverExample.class);
	
	private XBee xbee = new XBee();
	
	public ZBNodeDiscoverExample() throws XBeeException, InterruptedException {
		
		try {
			// replace with your serial port
			xbee.open("/dev/tty.usbserial-A6005v5M", 9600);
			
			
			// get the Node discovery timeout
			xbee.sendAsynchronous(new AtCommand("NT"));
			AtCommandResponse nodeTimeout = (AtCommandResponse) xbee.getResponse();
			
			// default is 6 seconds
			int nodeDiscoveryTimeout = ByteUtils.convertMultiByteToInt(nodeTimeout.getValue()) * 100;			
			log.info("Node discovery timeout is " + nodeDiscoveryTimeout + " milliseconds");
						
			log.info("Sending Node Discover command");
			xbee.sendAsynchronous(new AtCommand("ND"));

			// NOTE: increase NT if you are not seeing all your nodes reported
			
			List<? extends XBeeResponse> responses = xbee.collectResponses(nodeDiscoveryTimeout);
			
			log.info("Time is up!  You should have heard back from all nodes by now.  If not make sure all nodes are associated and/or try increasing the node timeout (NT)");
			
			for (XBeeResponse response : responses) {
				if (response instanceof AtCommandResponse) {
					AtCommandResponse atResponse = (AtCommandResponse) response;
					
					if (atResponse.getCommand().equals("ND") && atResponse.getValue() != null && atResponse.getValue().length > 0) {
						ZBNodeDiscover nd = ZBNodeDiscover.parse((AtCommandResponse)response);
						log.info("Node Discover is " + nd);							
					}
				}
			}
		} finally {
			if (xbee != null && xbee.isConnected()) {
				xbee.close();		
			}
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		new ZBNodeDiscoverExample();
	}
}

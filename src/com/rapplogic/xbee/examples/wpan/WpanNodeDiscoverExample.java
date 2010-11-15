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

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.CollectTerminator;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.WpanNodeDiscover;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * Example of performing a node discover for Series 1 XBees.
 * You must connect to the coordinator to run this example and
 * have one or more end devices that are associated.
 * 
 * @author andrew
 *
 */
public class WpanNodeDiscoverExample {

	private final static Logger log = Logger.getLogger(WpanNodeDiscoverExample.class);
	
	private XBee xbee = new XBee();
	
	public WpanNodeDiscoverExample() throws XBeeException, InterruptedException {
		
		try {
			// my coordinator com/baud
			xbee.open("/dev/tty.usbserial-A4004Rim", 9600);
			
			// get the Node discovery timeout
			xbee.sendAsynchronous(new AtCommand("NT"));
			AtCommandResponse nodeTimeout = (AtCommandResponse) xbee.getResponse();
			
			// default is 2.5 seconds for series 1
			int nodeDiscoveryTimeout = ByteUtils.convertMultiByteToInt(nodeTimeout.getValue()) * 100;			
			log.info("Node discovery timeout is " + nodeDiscoveryTimeout + " milliseconds");
			
			xbee.sendAsynchronous(new AtCommand("ND"));

			// collect responses up to the timeout or until the terminating response is received, whichever occurs first
			List<? extends XBeeResponse> responses = xbee.collectResponses(10000, new CollectTerminator() {
				public boolean stop(XBeeResponse response) {
					if (response instanceof AtCommandResponse) {
						AtCommandResponse at = (AtCommandResponse) response;
						if (at.getCommand().equals("ND") && at.getValue() != null && at.getValue().length == 0) {
							log.debug("Found terminating response");
							return true;
						}							
					}
					return false;
				}
			});
			
			//TODO check for terminating node
			
			log.info("Time is up!  You should have heard back from all nodes by now.  If not make sure all nodes are associated and/or try increasing the node timeout (NT)");
			
			for (XBeeResponse response : responses) {
				if (response instanceof AtCommandResponse) {
					AtCommandResponse atResponse = (AtCommandResponse) response;
					
					if (atResponse.getCommand().equals("ND") && atResponse.getValue() != null && atResponse.getValue().length > 0) {
						WpanNodeDiscover nd = WpanNodeDiscover.parse((AtCommandResponse)response);
						log.info("Node Discover is " + nd);							
					}
				}
			}
		} finally {
			xbee.close();
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		new WpanNodeDiscoverExample();
	}
}

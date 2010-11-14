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

import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.NodeDiscover;
import com.rapplogic.xbee.util.ByteUtils;

/** 
 * Example of performing a node discover for Series 1 XBees.
 * You must connect to the coordinator to run this example and
 * have one or more end devices that are associated.
 * 
 * @author andrew
 *
 */
public class NodeDiscoverExample {

	private final static Logger log = Logger.getLogger(NodeDiscoverExample.class);
	
	private XBee xbee = new XBee();
	
	public NodeDiscoverExample() throws XBeeException, InterruptedException {
		
		try {
			// my coordinator com/baud
			xbee.open("/dev/tty.usbserial-A4004Rim", 9600);
			
			// get the Node discovery timeout
			xbee.sendAsynchronous(new AtCommand("NT"));
			AtCommandResponse nodeTimeout = (AtCommandResponse) xbee.getResponse();
			
			// default is 6 seconds
			int nodeDiscoveryTimeout = ByteUtils.convertMultiByteToInt(nodeTimeout.getValue()) * 100;			
			log.info("Node discovery timeout is " + nodeDiscoveryTimeout + " milliseconds");
						
			log.info("Sending Node Discover command");
			xbee.sendAsynchronous(new AtCommand("ND"));
			
			long start = System.currentTimeMillis();
			
			// I've found that the default NT is often not enough time, so I'm using 20 secs
			final int timeout = 20000;
			
			while (true) {
				XBeeResponse response = xbee.getResponse(5000);
				
				if (response instanceof AtCommandResponse) {
					AtCommandResponse atResponse = (AtCommandResponse) response;
					
					if (atResponse.getCommand().equals("ND")) {
						if (atResponse.getValue() != null && atResponse.getValue().length > 0) {
							NodeDiscover nd = NodeDiscover.parse((AtCommandResponse)response);
							log.info("Node Discover is " + nd);							
						} else {
							log.info("Found terminating ND response.. exiting");
							break;
						}
					} else {
						log.info("Ignoring non-ND AT response: " + response);
					}
				} else {
					log.info("Ignoring non-AT response" + response);
				}
				
				if (System.currentTimeMillis() - start > timeout) {
					log.error("Did not receive ND response correctly");
					break;
				}
			}
			
			log.info("Time is up!  You should have heard back from all nodes by now.  If not make sure all nodes are associated and/or try increasing the node timeout (NT)");
		} finally {
			xbee.close();
		}
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		new NodeDiscoverExample();
	}
}

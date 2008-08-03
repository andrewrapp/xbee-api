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

import com.rapplogic.xbee.transparent.SerialAsciiComm;

/**
 * This is my attempt at reliable communication between two XBees in transparent mode.
 * It works like this:
 * Sends a command with a sequence to XBee Pong and waits for a response.  
 * If a timeout occurs, we retry the command.
 * 
 * For best results, start XBeePong first, then XBeePing.
 * 
 * @author andrew
 * 
 */
public class XBeePing extends SerialAsciiComm {

	private final static Logger log = Logger.getLogger(XBeePing.class);
	
	private final int delay = 0;

	private long totalRoundTrip;
	private int commandSequence;
	private int timeouts;	
	
	//private Map ackMap = new HashMap();
	
	private XBeePing(String[] args) throws Exception {
		super();
		this.openSerialPort("COM4", 9600);
		this.run();
	}

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		new XBeePing(args);
	}
	
	protected Object getLock() {
		return this;
	}
	
	public void run() {
		
		try {		
			while (true) {
				
				synchronized(this.getLock()) {				
					long start = System.currentTimeMillis();
					
					this.sendCommand(String.valueOf(commandSequence), Command.COMMAND);
					
					// wait for response from 5678
					long now = System.currentTimeMillis();
					
					this.getLock().wait(TIMEOUT);
					
					if ((System.currentTimeMillis() - now) >= TIMEOUT) {
						log.debug("timeout exceeded waiting for response.  Timeout count = " + timeouts);
						timeouts++;
						// send same command
						continue;
					}
					
					//avg ~ 50 milli per roundtrip
					this.totalRoundTrip+= (System.currentTimeMillis() - start);
					
					log.debug("Timeouts: " + timeouts + ".  Average round trip in " + (1.0*this.totalRoundTrip/(commandSequence + 1)*1.0));
					
					Command ackCommand = Command.parse(this.getLastResponse());
					
					log.debug("Command is " + ackCommand.toString());
									
					if (ackCommand.getSequence().intValue() == commandSequence) {
						// ack received.  increment command sequence
						commandSequence++;	
					} else {
						// should not happen
						throw new RuntimeException("Received ack for " + ackCommand.getSequence() + ", but expected " + commandSequence);
					}
				}
				
				Thread.sleep(delay);
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}
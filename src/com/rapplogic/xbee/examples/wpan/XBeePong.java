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
 * This class is completely passive in that it only responds to a incoming command.
 * 
 * Receives a number from the ping XBeeApi and sends it back.
 * 
 * @author andrew
 * 
 */
public class XBeePong extends SerialAsciiComm {
	
	private final static Logger log = Logger.getLogger(XBeePong.class);
	
	private int timeout;	
	private boolean first = true;
	private int errors;
	private int lastSequence = -1;
	private int timeoutCount;
	
	private long roundTrip;
	
	/**
	 * TODO ping sends command, pong sends ACK with command number.  if ping does not receive ack, it will send retry
	 * @param args
	 * @throws Exception
	 */
	private XBeePong(String[] args) throws Exception {
		this.openSerialPort("COM15", 9600);
		this.run();
	}

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		new XBeePong(args);
	}
	
	protected Object getLock() {
		return this;
	}

	public void run() {
		
		try {		
			while (true) {
				synchronized(this.getLock()) {
					// wait here for the other radio to send something
					long now = System.currentTimeMillis();
					//log.debug("waiting for data");
					
					if (first) {
						// first time wait indefinitely
						this.getLock().wait();
						first = false;
					} else {
						this.getLock().wait(timeout);
						
						if ((System.currentTimeMillis() - now) >= timeout) {
							log.debug("Timeout exceeded with no response.  Timeouts = " + timeoutCount);
							timeoutCount++;
							// wait for next command
							continue;
						}
					}

					// send back what we received
					Command recCommand = Command.parse(this.getLastResponse());
					
					log.debug("Received command " + recCommand.toString());

					log.debug("Timeouts: " + timeoutCount + ". Errors " + errors + ".  Round trip in " + (System.currentTimeMillis() - roundTrip));
					
					roundTrip = System.currentTimeMillis();
					
					if (lastSequence == -1 || (lastSequence + 1) == recCommand.getSequence().intValue()) {
						// this is what we expect
						this.sendCommand(recCommand.getSequence().toString(), Command.ACK);	
					} else if (lastSequence == recCommand.getSequence().intValue()) {
						//ignore.. we already received this command.  send ack for last command
						// in this scenario, our response didn't make it to ping so he resent it
						this.sendCommand(recCommand.getSequence().toString(), Command.ACK);
						errors++;
						log.debug("Ping did not receive our last ack and is resending, as if we didn't receive.  Expected " + (lastSequence + 1) + ", but received " + recCommand.getSequence());
					} else {
						// could occur if ping never received ack and gave up on retries.  update our sequence to this value + 1
						throw new RuntimeException("something else");
					}

					lastSequence = recCommand.getSequence().intValue();						
				}
			}
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}
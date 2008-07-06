package com.rapplogic.xbee.api;

import java.io.InputStream;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.util.ByteUtils;

/**
 *  Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 *  This file is part of XBee-API.
 *  
 *  XBee-API is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  XBee-API is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  
 * @author Andrew Rapp
 *
 */
public class XBeePacketParser implements Runnable {
	
	private final static Logger log = Logger.getLogger(XBeePacketParser.class);
	private final int DEFAULT_TIMEOUT = 60000;
	
	private XBeePacketHandler handler;
	private Object newPacketNotification = null;
	private InputStream in;
	private int timeout = DEFAULT_TIMEOUT;
	
	private Thread thread;
	
	private boolean done = false;

	public XBeePacketParser(InputStream in, XBeePacketHandler handler, Object lock) {
		this.in = in;
		this.handler = handler;
		this.newPacketNotification = lock;
		
		thread = new Thread(this);
		thread.start();
		
		log.debug("starting reader thread");
	}
	
	public void run() {

		int val = -1;
		
		XBeeResponse response = null;
		PacketStream packetStream = null;
		
		while (!done) {
			
			try {
				if (in.available() > 0) {
					
					val = in.read();
					
					log.debug("Read " + ByteUtils.formatByte(val) + " from input stream");
					
					if (val == XBeePacket.START_BYTE) {
						packetStream = new PacketStream(in);
						response = packetStream.parsePacket();
						
						log.debug("Response is " + response.toString());
						
						// wrap around entire parse routine
						synchronized (this.newPacketNotification) {							
							// add to handler and newPacketNotification
							handler.handlePacket(response);
							log.debug("Notifying API user that packets are ready");
							newPacketNotification.notifyAll();
						}
					} else {
						log.warn("expected start byte but got this " + ByteUtils.toBase16(val) + ", discarding");
					}
				} else {
					log.info("no data available.. waiting for new data event or timeout");
					long start = System.currentTimeMillis();
					
					// we will wait here for RXTX to notify us of new data
					synchronized (this) {
						// There's a chance that we got notified after the first in.available check
						if (in.available() > 0) {
							continue;
						}
						
						// serial event will wake us up
						this.wait(timeout);
					}
					
					if (System.currentTimeMillis() - start >= timeout) {
						log.debug("timeout expired.. checking for data");
					} else {
						log.info("packet parser thread woken up");
					}
				}
			} catch(InterruptedException ie) {
				// we've been told to stop
				break;
			} catch (Exception e) {
				// handling exceptions in a thread is a bit dicey.  the rest of the packet will be discarded
				log.error("Exception in reader thread", e);
				handler.error(e);
				
				synchronized (this.newPacketNotification) {
					newPacketNotification.notify();
				}
			}				
		}
	}

	public int getTimeout() {
		return timeout;
	}

	/**
	 * This is how long we wait until we check for new data in the event RXTX fails to notify us.
	 * 
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	public void interrupt() {
		thread.interrupt();
	}
}
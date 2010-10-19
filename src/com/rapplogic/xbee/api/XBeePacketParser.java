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

package com.rapplogic.xbee.api;

import gnu.io.SerialPortEvent;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.RxTxSerialComm;
import com.rapplogic.xbee.RxTxSerialEventListener;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * Reads data from the input stream had hands off to PacketStream for packet parsing.
 * Notifies XBee class when a new packet is parsed
 * <p/>
 * @author andrew
 *
 */
// TODO rename to XBeeInputStreamThread, after commit
public class XBeePacketParser implements Runnable {
	
	private final static Logger log = Logger.getLogger(XBeePacketParser.class);
	
	private Thread thread;
	private volatile boolean done = false;
	private final RxTxSerialComm serialPort;
	
	public RxTxSerialComm getSerialPort() {
		return serialPort;
	}

	private final BlockingQueue<XBeeResponse> responseQueue = new LinkedBlockingQueue<XBeeResponse>();
	
	// TODO use weak references
	private final List<PacketListener> packetListenerList = new LinkedList<PacketListener>();
	
	public List<PacketListener> getPacketListenerList() {
		return packetListenerList;
	}

	private int maxPacketQueueSize = 100;

	public int getMaxPacketQueueSize() {
		return maxPacketQueueSize;
	}

	public void setMaxPacketQueueSize(int maxPacketQueueSize) {
		this.maxPacketQueueSize = maxPacketQueueSize;
	}

	public BlockingQueue<XBeeResponse> getResponseQueue() {
		return responseQueue;
	}

	public XBeePacketParser(final RxTxSerialComm serialPort) {
		this.serialPort = serialPort;
		
		final XBeePacketParser thiz = this;
		
		serialPort.setSerialEventHandler(new RxTxSerialEventListener() {

			public void handleSerialEvent(SerialPortEvent event) {
				switch (event.getEventType()) {	
					case SerialPortEvent.DATA_AVAILABLE:

						try {
							if (serialPort.getInputStream().available() > 0) {
								try {
									log.debug("serialEvent: " + serialPort.getInputStream().available() + " bytes available");
									synchronized (thiz) {
										thiz.notify();										
									}
								} catch (Exception e) {
									log.error("Error in handleSerialData method", e);
								}				
							}
						} catch (IOException ex) {
							// it's best not to throw the exception because the RXTX thread may not be prepared to handle
							log.error("RXTX error in serialEvent method", ex);
						}
					default:
						log.debug("Ignoring serial port event type: " + event.getEventType());
				}
			}
		});
		
		thread = new Thread(this);
		thread.setName("XBee Packet Parser Thread");
		thread.start();
		
		log.debug("starting packet parser thread");
	}
	
	private void addResponse(XBeeResponse response) throws InterruptedException {
		
		// trim the queue
		while (responseQueue.size() >= (this.getMaxPacketQueueSize() - 1)) {
			responseQueue.poll();
		}
		
		responseQueue.put(response);
				
		// TODO run in separate thread
		// must synchronize to avoid  java.util.ConcurrentModificationException at java.util.AbstractList$Itr.checkForComodification(Unknown Source)
		// this occurs if packet listener add/remove is called while we are iterating
		synchronized (packetListenerList) {
			for (PacketListener pl : packetListenerList) {
				try {
					if (pl != null) {
						pl.processResponse(response);	
					} else {
						log.warn("PacketListener is null, size is " + packetListenerList.size());
					}
				} catch (Throwable th) {
					log.warn("Exception in packet listener", th);
				}
			}			
		}		
	}
	
	public void run() {

		int val = -1;
		
		XBeeResponse response = null;
		PacketStream packetStream = null;

		try {
			while (!done) {
				try {
					if (serialPort.getInputStream().available() > 0) {
						log.debug("About to read from input stream");
						val = serialPort.getInputStream().read();
						log.debug("Read " + ByteUtils.formatByte(val) + " from input stream");
						
						if (val == XBeePacket.SpecialByte.START_BYTE.getValue()) {
							packetStream = new PacketStream(serialPort.getInputStream());
							response = packetStream.parsePacket();
							
							if (log.isInfoEnabled()) {
								log.info("Received packet from XBee: " + response);	
								log.debug("Received packet: int[] packet = {" + ByteUtils.toBase16(response.getRawPacketBytes(), ", ") + "};");	
							}
							
							// success
							this.addResponse(response);
						} else {
							log.warn("expected start byte but got this " + ByteUtils.toBase16(val) + ", discarding");
						}
					} else {
						log.debug("No data available.. waiting for new data event");
						
						// we will wait here for RXTX to notify us of new data
						synchronized (this) {
							// There's a chance that we got notified after the first in.available check
							if (serialPort.getInputStream().available() > 0) {
								continue;
							}
							
							// wait until new data arrives
							this.wait();
						}	
					}				
				} catch (Exception e) {
					if (e instanceof InterruptedException) throw ((InterruptedException)e);
					
					log.error("Error while parsing packet:", e);
					
//					ErrorResponse error = new ErrorResponse();
//					error.setException(e);
//					
//					try {
//						this.addResponse(error);
//					} catch (InterruptedException e1) {
//						log.warn("Unable to add error response to queues", e1);
//					}
					
					if (e instanceof IOException) {
						// this is thrown by RXTX if the serial device unplugged while we are reading data; if we are waiting then it will waiting forever
						log.error("Serial device IOException.. exiting");
						break;
					}
				}
			}
		} catch(InterruptedException ie) {
			// We've been told to stop -- the user called the close() method
			
			// TODO serialPort.getInputStream() case thread is waiting.. notify with error response
//			ErrorResponse error = new ErrorResponse();
//			error.setException(ie);
//			
//			try {
//				this.addResponse(error);
//			} catch (InterruptedException e) {
//				log.warn("Interrupted while applying error repsonse to queue", e);
//			}
			
			log.info("Packet parser thread was interrupted.  This occurs when close() is called");
		} finally {
			try {				
				serialPort.close();
			} catch (Exception e) {
				log.warn("Unable to shutdown input stream", e);
			};
		}
		
		log.info("Packet parser thread is exiting");
	}

	public void setDone(boolean done) {
		this.done = done;
	}
	
	public void interrupt() {
		if (thread != null) {
			try {
				thread.interrupt();	
			} catch (Exception e) {
				log.warn("Error interrupting parser thread", e);
			}
		}
	}
}
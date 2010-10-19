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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.RxTxSerialComm;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * This is an API for communicating with Digi XBee 802.15.4 and ZigBee radios
 * via the serial port
 * <p/>
 * @author Andrew Rapp <andrew.rapp at gmail>
 * 
 */
public class XBee implements IXBee {

	private final static Logger log = Logger.getLogger(XBee.class);

	// TODO create XBeeConfiguration object to be passed in constructor
	
	// object to synchronize on to protect access to sendPacket
	private Object sendPacketBlock = new Object();
	private boolean startupChecks = true;
	private RxTxSerialComm serialComm;
	private XBeePacketParser parser;	
	
	public XBee() {
//		final XBee xbeeRef = this;
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//		    public void run() {
//		    	if (xbeeRef.isConnected()) {
//		    		log.info("Shutting down");
//		    		xbeeRef.close();
//		    	}
//		    }
//		});
	}

	public void doStartupChecks() throws XBeeException {
		// Perform startup checks
		try {				
			AtCommandResponse ap = this.sendAtCommand(new AtCommand("AP"));
			
			if (!ap.isOk()) {
				throw new XBeeException("Attempt to query AP parameter failed");
			}
			
			if (ap.getValue()[0] != 2) {
				log.warn("XBee radio is in API mode without escape characters (AP=1).  The radio must be configured in API mode with escape bytes (AP=2) for use with this library.");
				
				log.info("Attempting to set AP to 2");
				ap = this.sendAtCommand(new AtCommand("AP", 2));
				
				if (ap.isOk()) {
					log.info("Successfully set AP mode to 2.  This setting will not persist a power cycle without the WR (write) command");	
				} else {
					throw new XBeeException("Attempt to set AP=2 failed");
				}
			} else {
				log.info("Radio is in correct AP mode (AP=2)");
			}

			ap = this.sendAtCommand(new AtCommand("HV"));
			
			if (!ap.isOk()) {
				throw new XBeeException("Attempt to query HV parameter failed");
			}
			
			if (ap.isOk()) {
				switch (ap.getValue()[0]) {
				case 0x17:
					log.info("XBee radio is Series 1");
					break;
				case 0x18:
					log.info("XBee radio is Series 1 Pro");
					break;
				case 0x19:
					log.info("XBee radio is Series 2");
					break;
				case 0x1a:
					log.info("XBee radio is Series 2 Pro");
					break;
				default:
					log.warn("Unknown radio type (HV): " + ap.getValue()[0]);
				}
			}	
			
			AtCommandResponse vr = this.sendAtCommand(new AtCommand("VR"));
			
			if (vr.isOk()) {
				log.info("Firmware version is " + ByteUtils.toBase16(vr.getValue()));
			}
			
			this.clearResponseQueue();
		} catch (XBeeTimeoutException ex) {
			throw new XBeeException("AT command timed-out while attempt to set/read in API mode.  The XBee radio must be in API mode (AP=2) to use with this library");
		}		
	}
	
	/**
	 * If startChecks is set to true (default), this method will check if the AP parameter
	 * is set correctly and attempt to update if AP=1.  If AP=0 (Transparent mode), an
	 * exception will be thrown.
	 */
	public void open(String port, int baudRate) throws XBeeException {
		try {
			if (serialComm != null && serialComm.getInputStream() != null) {			
				throw new IllegalStateException("Cannot open new connection -- existing connect still open.  Please close first");
			}
			
			serialComm = new RxTxSerialComm();
			
			serialComm.openSerialPort(port, baudRate);
			parser = new XBeePacketParser(serialComm);
			
			// startup heuristics
			if (!this.isStartupChecks()) {
				this.doStartupChecks();
			}
		} catch (XBeeException e) {
			throw e;
		} catch (Exception e) {		
			throw new XBeeException(e);
		}
	}

	public void addPacketListener(PacketListener packetListener) { 
		if (parser == null) {
			throw new IllegalStateException("No connection");
		}
		
		synchronized (parser.getPacketListenerList()) {
			this.parser.getPacketListenerList().add(packetListener);	
		}
	}

	public void removePacketListener(PacketListener packetListener) {
		if (parser == null) {
			throw new IllegalStateException("No connection");
		}
		
		synchronized (parser.getPacketListenerList()) {
			this.parser.getPacketListenerList().remove(packetListener);
		}
	}
	
	public void sendRequest(XBeeRequest request) throws IOException {
		log.info("Sending request to XBee: " + request);
		this.sendPacket(request.getXBeePacket());
	}
	
	/** 
	 * It's possible for packets to get interspersed if multiple threads send simultaneously.  
	 * This method is not thread-safe because doing so would introduce a synchronized performance penalty 
	 * for the vast majority of users that will not never need thread safety.
	 * That said, it is responsibility of the user to provide synchronization if multiple threads are sending.
	 * 
	 * Not thread safe.
	 *  
	 * @param packet
	 * @throws IOException
	 */
	public void sendPacket(XBeePacket packet) throws IOException {	
		this.sendPacket(packet.getByteArray());
	}
	
	/**
	 * This exists solely for the XMPP project.  Use sendRequest instead
	 * 
	 * Not Thread Safe
	 * 
	 * @param packet
	 * @throws RuntimeException when serial device is disconnected
	 */
	public void sendPacket(int[] packet)  throws IOException {
		// TODO should we synchronize on read lock so we are sending/recv. simultaneously?
		// TODO call request listener with byte array
		
		if (!this.isConnected()) {
			throw new RuntimeException("XBee is not connected");
		}
		
		if (log.isInfoEnabled()) {
			log.info("sending packet to XBee " + ByteUtils.toBase16(packet));	
		}

        for (int aPacket : packet) {
        	serialComm.getOutputStream().write(aPacket);
        }

        serialComm.getOutputStream().flush();
	}

	/**
	 * Sends an XBeeRequest though the XBee interface in an asynchronous manner, such that
	 * it will return immediately, without waiting for a response.
	 * Refer to the getResponse method for obtaining a response
	 * 
	 * Not thread safe
	 * 
	 * @param request
	 * @throws XBeeException
	 */
	public void sendAsynchronous(XBeeRequest request) throws XBeeException {

		try {
			this.sendRequest(request);			
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	/**
	 * Uses sendSynchronous to send an AtCommand and collect the response
	 * <p/>
	 * Timeout value is fixed at 5 seconds
	 * 
	 * @deprecated Use this.sendSynchronous(command, timeout);
	 * @param command
	 * @return
	 * @throws XBeeException
	 */
	public AtCommandResponse sendAtCommand(AtCommand command) throws XBeeException {
		return (AtCommandResponse) this.sendSynchronous(command, 5000);
	}
	
	/**
	 * Synchronous method for sending an XBeeRequest and obtaining the 
	 * corresponding response (response that has same frame id).
	 * <p/>
	 * This method returns the first response object with a matching frame id, within the timeout
	 * period, so it is important to use q unique frame id (relative to previous subsequent requests).
	 * <p/>
	 * This method must only be called with requests that receive a response of
	 * type XBeeFrameIdResponse.  All other request types with timeout.
	 * <p/>
	 * Keep in mind responses received here will also be available through the getResponse method
	 * and the packet listener.  You should call clearResponseQueue prior to calling this method
	 * if you don't want the same packet to be returned by getResponse.
	 * <p/>
	 * TX requests send status responses (ACK) that indicate if the packet was delivered.  
	 * In my brief testing with series 2 radios in a simple 2 radio network, I got a status 
	 * response of ADDRESS_NOT_FOUND in about 3 seconds when my end device is powered off.  
	 * Keep in mind that you'll want to make sure your timeout is always larger than this 
	 * value in order to receive status responses.
	 * <p/>
	 * This method is thread-safe 
	 * 
	 * @param xbeeRequest
	 * 
	 * @return
	 * @throws XBeeException 
	 * @throws XBeeTimeoutException thrown if no matching response is identified
	 */
	public XBeeResponse sendSynchronous(final XBeeRequest xbeeRequest, int timeout) throws XBeeTimeoutException, XBeeException {		
		if (xbeeRequest.getFrameId() == XBeeRequest.NO_RESPONSE_FRAME_ID) {
			throw new XBeeException("Frame Id cannot be 0 for a synchronous call -- it will always timeout as there is no response!");
		}		

		PacketListener pl = null;
		
		try {
			final List<XBeeResponse> container = new LinkedList<XBeeResponse>();

			// this makes it thread safe -- prevents multiple threads from writing to output stream simultaneously
			synchronized (sendPacketBlock) {
				this.sendRequest(xbeeRequest);	
			}
			
			pl = new PacketListener() {
				// TODO handle error response as well
				public void processResponse(XBeeResponse response) {
					if (response instanceof XBeeFrameIdResponse && ((XBeeFrameIdResponse)response).getFrameId() == xbeeRequest.getFrameId()) {
						// frame id matches -- yay we found it
						container.add(response);
						
						synchronized(container) {
							container.notify();	
						}
						
					}
				}
			};
			
			this.addPacketListener(pl);
			
			synchronized (container) {
				try {
					container.wait(timeout);
				} catch (InterruptedException e) { }
			}
			
			if (container.size() == 0) {
				// we didn't find a matching packet
				throw new XBeeTimeoutException();
			}
			
			return (XBeeResponse) container.get(0);
		} catch (IOException io) {
			throw new XBeeException(io);
		} finally {
			if (pl != null) {
				this.removePacketListener(pl);
			}
		}
	}
		
	/**
	 * Same as getResponse(int) but does not timeout.
	 * It's highly recommend that you always use a timeout because
	 * if the serial connection dies under certain conditions, you will end up waiting forever!
	 * <p/>
	 * Consider using the PacketListener for asynchronous (non-blocking) behavior
	 * 
	 * @return
	 * @throws XBeeException 
	 */
	public XBeeResponse getResponse() throws XBeeException {
		return getResponse(null);
	}
	
	/**
	 * This method returns an XBeeResponse from the queue, if available.
	 * There are three possible outcomes:
	 * <p/>
	 * 1.  Packet is returned <br/>
	 * 2.  XBeeTimeoutException is thrown (i.e. queue was empty for duration of timeout) <br/>
	 * 3.  Return null if timeout is 0 and queue is empty. <br/>
	 * <p/>
	 * @param timeout milliseconds to wait for a response.  A value of zero disables the timeout
	 * @return
	 * @throws XBeeException
	 * @throws XBeeTimeoutException if timeout occurs before a response is received
	 */
	public XBeeResponse getResponse(int timeout) throws XBeeException, XBeeTimeoutException {
		return this.getResponse(timeout);
	}
	
	private XBeeResponse getResponse(Integer timeout) throws XBeeException, XBeeTimeoutException {
		XBeeResponse response;
		try {
			if (timeout != null) {
				response = parser.getResponseQueue().poll(timeout, TimeUnit.MILLISECONDS);	
			} else {
				response = parser.getResponseQueue().take();
			}
		} catch (InterruptedException e) {
			throw new XBeeException("Error while attempting to remove packet from queue", e);
		}
		
		if (response == null && timeout > 0) {
			throw new XBeeTimeoutException();
		}
		
		return response;
	}
	
	
	/**
	 * Shuts down RXTX and packet parser thread
	 */
	public void close() {		

		if (!this.isConnected()) {
			throw new IllegalStateException("XBee is not connected");
		}
		
		// shutdown parser thread
		if (parser != null) {
			parser.setDone(true);
			// interrupts thread, if waiting.  does not interrupt thread if blocking on read
			// serial port close will be closed prior to thread exit
			parser.interrupt();
		}
		
		parser = null;
		serialComm = null;
	}

	/**
	 * Indicates if serial port connection has been established
	 * 
	 * @return
	 */
	public boolean isConnected() {
		try {
			if (parser.getSerialPort().getInputStream() != null && serialComm.getOutputStream() != null) {
				return true;
			}
			
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public boolean isStartupChecks() {
		return startupChecks;
	}

	/**
	 * Controls is a startup check is performed when connecting to the XBee.
	 * The startup check attempts to determine the firmware type and if it is 
	 * configured correctly for use with this software.  Default is true.
	 *  
	 * @param startupChecks
	 */
	public void setStartupChecks(boolean startupChecks) {
		this.startupChecks = startupChecks;
	}
	
	/**
	 * Sets the maximum size of the internal queue that supports the getResponse(..) method.
	 * Packets are removed from the head of the queue once this limit is reached.  The default is 100
	 * 
	 * @param size
	 */
	public void setMaxQueueSize(int size) {
		
		if (parser == null) {
			throw new IllegalStateException("No connection");
		}
		
		if (size > 0) {
			this.parser.setMaxPacketQueueSize(size);
		} else {
			throw new IllegalArgumentException("Size must be > 0");
		}
	}
	
	// TODO move to its own class
	private int sequentialFrameId = 0xff;
	
	public int getCurrentFrameId() {
		// TODO move to separate class (e.g. FrameIdCounter)
		return sequentialFrameId;
	}
	
	/**
	 * This is useful for obtaining a frame id when composing your XBeeRequest.
	 * It will return frame ids in a sequential manner until the maximum is reached (0xff)
	 * and it flips to 1 and starts over.
	 * 
	 * Not Thread-safe
	 * 
	 * @return
	 */
	public int getNextFrameId() {
		if (sequentialFrameId == 0xff) {
			// flip
			sequentialFrameId = 1;
		} else {
			sequentialFrameId++;
		}
		
		return sequentialFrameId;
	}
	
	/**
	 * Updates the frame id.  Any value between 1 and ff is valid
	 * 
	 * @param val
	 * Jan 24, 2009
	 */
	public void updateFrameId(int val) {
		if (val <=0 || val > 0xff) {
			throw new IllegalArgumentException("invalid frame id");
		}
		
		this.sequentialFrameId = val;
	}		
	
	/**
	 * Removes all packets off of the response queue
	 */
	public void clearResponseQueue() {
		parser.getResponseQueue().clear();
	}
}

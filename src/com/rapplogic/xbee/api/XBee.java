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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.RxTxSerialComm;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.ExceptionHandler;

/**
 * This is an API for Digi XBee 802.15.4 and ZigBee radios
 * 
 * TODO refactor to not extend RxTxSerialComm; hide visibility of handlePacket protected
 * 
 * @author Andrew Rapp <andrew.rapp at gmail>
 * 
 */
public class XBee extends RxTxSerialComm implements IXBee, XBeePacketHandler {

	private final static Logger log = Logger.getLogger(XBee.class);

	// object to synchronize on to protect access to sendPacket
	private Object sendPacketBlock = new Object();
	private Object newPacketNotification = new Object();

	private final int maxPacketListSize = 100;
	// TODO replace with ArrayBlockingQueue
	private final List<XBeeResponse> packetList = new ArrayList<XBeeResponse>();

	private final List<PacketListener> packetListenerList = new ArrayList<PacketListener>();

	private XBeePacketParser parser;
	
	private long packetCount;

	private int sequentialFrameId = 0xff;
	
	private XBeeResponse lastResponse;
	
	private boolean connected = false;
	
	public XBee() {

	}

	public void open(String port, int baudRate) throws XBeeException {
		try {
			this.openSerialPort(port, baudRate);
			parser = new XBeePacketParser(this.getInputStream(), this, newPacketNotification);	
			connected = true;
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	public void addPacketListener(PacketListener packetListener) { 
		synchronized (packetListenerList) {
			this.packetListenerList.add(packetListener);	
		}
	}

	public void removePacketListener(PacketListener packetListener) {
		synchronized (packetListenerList) {
			this.packetListenerList.remove(packetListener);
		}
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
		this.sendPacket(packet.getPacket());
	}
	
	/**
	 * This is a bit dangerous and may be made private in a future release.  Intended For XMPP
	 * 
	 * Not Thread Safe
	 * 
	 * @param packet
	 * @throws IOException when serial device is disconnected: java.io.IOException: Device not configured in writeByte
	 */
	public void sendPacket(int[] packet)  throws IOException {
		// TODO call request listener with byte array
		
		if (log.isInfoEnabled()) {
			log.info("sending packet to XBee " + ByteUtils.toBase16(packet));	
		}

        for (int aPacket : packet) {
            this.getOutputStream().write(aPacket);
        }

		this.getOutputStream().flush();
	}

	/**
	 * Sends an XBeeRequest though the XBee interface in an asynchronous manner, such that
	 * it will return immediately, without waiting for a response.
	 * Refer to the getResponse method for obtaining a response
	 * 
	 * Not thread safe
	 * 
	 * @param frameData
	 * @throws XBeeException
	 */
	public void sendAsynchronous(XBeeRequest frameData) throws XBeeException {

		try {
			XBeePacket packet = frameData.getXBeePacket();
			this.sendPacket(packet);			
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	/**
	 * Uses sendSynchronous to send an AtCommand and collect the response
	 * 
	 * Timeout value is fixed at 5 seconds.
	 * 
	 * TODO deprecate -- this method doesn't provide any value over sendSynchronous
	 * 
	 * @param command
	 * @return
	 * @throws XBeeException
	 */
	public XBeeResponse sendAtCommand(AtCommand command) throws XBeeException {
		return this.sendSynchronous(command, 5000);
	}
	
	/**
	 * Synchronous method for sending an XBeeRequest and obtaining the 
	 * corresponding response (response that has same frame id).
	 * 
	 * This method should only be called with requests that receive a response of
	 * type XBeeFrameIdResponse and you should attempt to use a unique frame id (see getNextFrameId)
	 * 
	 * This method returns the first response object with a matching frame id within the timeout
	 * period
	 * 
	 * TX requests send status responses (ACK) that indicate if the packet was delivered.  
	 * In my brief testing with series 2 radios in a simple 2 radio network, I got a status 
	 * response of ADDRESS_NOT_FOUND in about 3 seconds when my end device is powered off.  
	 * Keep in mind that you'll want to make sure your timeout is always larger than this 
	 * value in order to receive status responses.
	 * 
	 * NOTE: this method is thread-safe because it's not possible for the user to easily
	 * make it thread safe and perform well 
	 * 
	 * TODO responses received during synchronous send should not be added to the packetList
	 * 
	 * @param xbeeRequest
	 * 
	 * @return
	 * @throws XBeeException 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws XBeeTimeoutException 
	 */
	public XBeeResponse sendSynchronous(final XBeeRequest xbeeRequest, int timeout) throws XBeeTimeoutException, XBeeException {

		if (xbeeRequest.getFrameId() == XBeeRequest.NO_RESPONSE_FRAME_ID) {
			throw new XBeeException("Frame Id cannot be 0 for a synchronous call -- it will always timeout as there is no response!");
		}		

		PacketListener pl = null;
		
		try {
			XBeePacket txPacket = xbeeRequest.getXBeePacket();

			final List<XBeeResponse> responseList = new ArrayList<XBeeResponse>();
			
			// TODO verify that there is no possibility the response could be received in the milliseconds before sending packet
			
			pl = new PacketListener() {
				// TODO handle error response as well
				public void processResponse(XBeeResponse response) {
					if (response instanceof XBeeFrameIdResponse && ((XBeeFrameIdResponse)response).getFrameId() == xbeeRequest.getFrameId()) {
						// frame id matches -- yay we found it
						responseList.add(response);
						
						synchronized(responseList) {
							responseList.notify();	
						}
						
					}
				}
			};
			
			this.addPacketListener(pl);
			
			// this makes it thread safe -- prevents multiple threads from writing to output stream simultaneously
			synchronized (sendPacketBlock) {
				this.sendPacket(txPacket);	
			}
			
			synchronized (responseList) {
				try {
					responseList.wait(timeout);
				} catch (InterruptedException e) { }
			}
			
			if (responseList.size() == 0) {
				// we didn't find a matching packet
				throw new XBeeTimeoutException();
			}
			
			return (XBeeResponse) responseList.get(0);
		} catch (IOException io) {
			throw new XBeeException(io);
		} finally {
			if (pl != null) {
				this.removePacketListener(pl);
			}
		}
	}

	/**
	 * You can synchronize on this lock to get notified of new packets, however you must call wait() 
	 * to release the lock or buffers will overrun and general chaos will ensue.
	 * 
	 * TODO provide users a different lock for notification since hogging this lock would gum up the works
	 * 
	 * @return
	 */
	public Object getNewPacketNotification() {
		return newPacketNotification;
	}

	/**
	 * Called by the XBeePacketParser when a new packet has arrived
	 * After this method returns, notifyAll is called on newPacketNotification 
	 * to alert all threads waiting for packets
	 * 
	 * This method is called within a synchronized block
	 * 
	 * TODO this should not be public
	 */
	public void handlePacket(XBeeResponse packet) {
		packetCount++;
		
		// must synchronize to avoid  java.util.ConcurrentModificationException at java.util.AbstractList$Itr.checkForComodification(Unknown Source)
		// this occurs if packet listener add/remove is called while we are iterating
		synchronized (packetListenerList) {
			for (PacketListener pl : packetListenerList) {
				try {
					if (pl != null) {
						pl.processResponse(packet);	
					} else {
						log.warn("PacketListener is null, size is " + packetListenerList.size());
					}
				} catch (Throwable th) {
					log.warn("Exception in packet listener", th);
				}
			}			
		}

		
		while (packetList.size() >= maxPacketListSize) {
			// remove the tail of the list
			packetList.remove(packetList.size() - 1);
		}
		
		packetList.add(packet);
		this.lastResponse = packet;
	}

	/**
	 * Called by RXTX to notify us that data is available to be read.
	 * 
	 * This method calls notify on the parser to indicate it should start
	 * consuming packets.
	 * 
	 */
	protected void handleSerialData()  {
		log.info("RXTX serialEvent");

		// alert the parser we have new data
		// parser may not be waiting
		synchronized (parser) {
			parser.notify();
		}
	}

	/**
	 * This method blocks until an XBeeResponse has been received
	 * 
	 * @throws XBeeException
	 */
	public void waitForResponse() throws XBeeTimeoutException, XBeeException {
		this.waitForResponse(0);
	}
	
	/**
	 * Updated to throw XBeeTimeoutException
	 * 
	 * @param timeout
	 * @throws XBeeException
	 */
	public void waitForResponse(long timeout) throws XBeeTimeoutException, XBeeException {
		try {
			synchronized (this.getNewPacketNotification()) {
				long now = System.currentTimeMillis();
				// wait for packets
				this.getNewPacketNotification().wait(timeout);
		
				if (timeout > 0 && (System.currentTimeMillis() - now >= timeout)) {
					throw new XBeeTimeoutException();
				}
			}			
		} catch (Exception e) {
			ExceptionHandler.handleAndThrow(e);
		}
	}

	/**
	 * Same as getResponseBlocking(int) but does not timeout
	 * 
	 * @return
	 * @throws XBeeException
	 * @throws XBeeTimeoutException
	 */
	public XBeeResponse getResponseBlocking() throws XBeeException, XBeeTimeoutException {
		return this.getResponseBlocking(0);
	}
	
	/**
	 * This method blocks until a response is received or a timeout occurs.
	 * This method will not return packets in the packetBuffer.
	 * This method is guaranteed to return a response object or throw an Exception
	 * This method is thread-safe.  each thread that acquires the lock prior to notification
	 * will receive the same response object
	 * 
	 * @param timeout
	 * @return
	 * @throws XBeeException
	 * @throws XBeeTimeoutException
	 */
	public XBeeResponse getResponseBlocking(int timeout) throws XBeeException, XBeeTimeoutException {
		try {
			synchronized (this.getNewPacketNotification()) {
				
				long now = System.currentTimeMillis();
				// wait for packets
				this.getNewPacketNotification().wait(timeout);
					
				if (timeout > 0 && (System.currentTimeMillis() - now >= timeout)) {
					throw new XBeeTimeoutException();
				}
				
				// we got notified
				if (lastResponse == null) {
					throw new XBeeException("newPacketNotification was notified but no packets are available");
				}
				
				return this.lastResponse;
			}
		} catch (Exception e) {
			ExceptionHandler.handleAndThrow(e);
		}
		
		// to satisfy eclipse compiler
		return null;		
	}
	
	
	/**
	 * Same as getResponse(int) but does not timeout
	 * 
	 * @return
	 * @throws XBeeException 
	 */
	public synchronized XBeeResponse getResponse() throws XBeeException {
		return getResponse(0);
	}
	
	/**
	 * This method returns immediately with the zero-eth response (FIFO), if getPacketList().size() is greater than 0;
	 * otherwise it waits on the getNewPacketNotification() object until the next response arrives.
	 *  
	 * Be sure to clear the packetList prior to calling this if you are looking for new responses
	 * 
	 * WARNING: If you call this method from multiple threads, each XBeeResponse 
	 * will be returned to only one thread!
	 * 
	 * Consider a sendAndReceive method that returns that first response 
	 * Consider a separate thread that reads from the packetlist and notifies waiters
	 * 
	 * @param timeout
	 * @return
	 * @throws XBeeException
	 * @throws XBeeTimeoutException if timeout occurs before a response is received
	 */
	public XBeeResponse getResponse(int timeout) throws XBeeException, XBeeTimeoutException {
		try {
			synchronized (this.getNewPacketNotification()) {
				
				if (this.getPacketList().size() > 0) {
					return (XBeeResponse) this.getPacketList().remove(0);
				} else {
					long now = System.currentTimeMillis();
					// wait for packets
					this.getNewPacketNotification().wait(timeout);
					
					if (timeout > 0 && (System.currentTimeMillis() - now >= timeout)) {
						throw new XBeeTimeoutException();
					}
					
					// we got notified
					if (this.getPacketList().size() > 0) {
						return (XBeeResponse) this.getPacketList().remove(0);
					} else {
						throw new XBeeException("newPacketNotification was notified but no packets are available");
					}
				}
			}			
		} catch (Exception e) {
			ExceptionHandler.handleAndThrow(e);
		}
		
		// to satisfy eclipse compiler
		return null;
	}
	
	/**
	 * This list holds maximum of maxPacketListSize packets and discards
	 * older packets when the maximum limit is reached.  
	 * 
	 * @return
	 */
	public List<XBeeResponse> getPacketList() {
		return packetList;
	}

	public long getPacketCount() {
		return packetCount;
	}
	
	/**
	 * Shuts down RXTX and packet parser thread
	 */
	public void close() {
		super.close();
		this.connected = false;
		
		// shutdown parser thread
		if (parser != null) {
			parser.setDone(true);
			// wake up if it's waiting for data
			parser.interrupt();
		}
	}
	
	public int getCurrentFrameId() {
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
	 * Returns the last XBeeResponse that was received
	 * 
	 * If this is called after waitForResponse returns (without timeout), this *should* be the corresponding response
	 * 
	 * @return
	 */
	public XBeeResponse getLastResponse() {
		return lastResponse;
	}

	/**
	 * Indicates if serial port connection has been established
	 * 
	 * @return
	 * Feb 22, 2009
	 */
	public boolean isConnected() {
		return connected;
	}
}

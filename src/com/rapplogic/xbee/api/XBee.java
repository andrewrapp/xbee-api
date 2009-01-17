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
import com.rapplogic.xbee.util.ExceptionHandler;

/**
 * This is an API for Digi XBee 802.15.4 and ZigBee radios
 * 
 * Objectives: 
 * 	Focus on support for a single version of firmware for both Zigbee and 802.15.4 XBee radios; this would likely be the latest stable. 
 * 	Implement functionality to meet an expected 80% of usage
 *  Strive for correctness and reliability over percentage of features implemented
 *  
 * Disclaimers: 
 *  Although it is my intention to continue to develop and support this project, I can't commit to a time frame (that's one reason it's open source)
 *  I also can't commit to providing technical support but will attempt to provide help where possible.
 *  I recommend you are familiar with Java and basic electronics before investing in this API such that
 *  you are comfortable with maintaining it to support your objectives.
 *  I will attempt to maintain backwards compatibility but it's possible that future release will require some refactoring of your code.
 *  This code is provided as is and I do not assume responsibility for any damage it may cause to your hardware or otherwise.
 *  
 * Notes:
 * 
 * The API mode classes are designed for escaped byte mode (AP=2).  This applies to both varieties of XBee (ZigBee and 802.14.5).
 *  
 * Unfortunately I don't have a good solution in place for regression testing.  Since this API depends on hardware,
 * there can be significant work in configuration/setup/test iterations required to test all functionality.  That said
 * I may have broken previously working stuff.
 * 
 * Please send feedback to email address listed below
 * 
 * TODO Expose XBee object as a service to share coordinator with multiple apps
 * TODO testNG framework for unit tests 
 * 
 * Windows users: To locate your COM port on windows, go to Start->(right-click)My Computer->Manage, then Select Device Manager and Ports
 * 
 * This is disappointing (from the znet manual): "The WR command should be used sparingly. The EM250 supports a limited number of write cycles.“ How many is limited??

 * @author Andrew Rapp a l r a p p [4t] yahoo
 * 
 */
public class XBee extends RxTxSerialComm implements XBeePacketHandler {

	private final static Logger log = Logger.getLogger(XBee.class);

	// object to synchronize on to protect access to sendPacket
	private Object sendPacketBlock = new Object();
	private Object newPacketNotification = new Object();

	private final int maxPacketListSize = 100;
	private final List<XBeeResponse> packetList = new ArrayList<XBeeResponse>();

	private final List<PacketListener> packetListenerList = new ArrayList<PacketListener>();

	private XBeePacketParser parser;
	
	private long packetCount;

	private int sequentialFrameId = 0xff;
	
	private XBeeResponse lastResponse;
	
	public XBee() {

	}

	public void open(String port, int baudRate) throws XBeeException {
		try {
			this.openSerialPort(port, baudRate);
			parser = new XBeePacketParser(this.getInputStream(), this, newPacketNotification);			
		} catch (Exception e) {
			throw new XBeeException(e);
		}
	}

	public void addPacketListener(PacketListener packetListener) {
		this.packetListenerList.add(packetListener);
	}

	public void removePacketListener(PacketListener packetListener) {
		this.packetListenerList.remove(packetListener);
	}
	
	/** 
	 * It's possible for packets to get intersperse if multiple threads send simultaneously.  
	 * This method is not thread-safe becaue doing so would introduce a synchronized performance penalty 
	 * for the vast majority of users that will not never need thread safety.
	 * That said, it is responsibility of the user to provide synchronization if multiple threads are sending.
	 * 
	 * Not thread safe.
	 *  
	 * @param packet
	 * @throws IOException
	 */
	public void sendPacket(XBeePacket packet) throws IOException {
		log.debug("sending packet " + packet.toString());		
		this.sendPacket(packet.getPacket());
	}
	
	/**
	 * This is a bit dangerous and may be removed in a future release.  Intended For XMPP
	 * 
	 * Not Thread Safe
	 * 
	 * @param packet
	 * @throws IOException
	 */
	public void sendPacket(int[] packet)  throws IOException {
		for (int i = 0; i < packet.length; i++) {
			this.getOutputStream().write(packet[i]);
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
	 * TODO remove -- this method doesn't provide any value
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
	 * This method either returns the matching response object or throws a timeout exception
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

			// this makes it thread safe -- prevents multiple threads from writing to output stream simulataneously
			synchronized (sendPacketBlock) {
				this.sendPacket(txPacket);	
			}
			
			final List<XBeeResponse> responseList = new ArrayList<XBeeResponse>();
			
			// TODO verify that there is no possibility the response could be received in the milliseconds after sending packet and before listener is registered.
			
			pl = new PacketListener() {
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
	 * TODO need a bulletproof mechanism for client threads block until a response is ready 
	 * 
	 * @return
	 */
	public XBeeResponse getLastResponse() {
		return lastResponse;
	}
}

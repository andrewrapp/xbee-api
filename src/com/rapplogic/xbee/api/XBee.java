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
 * This is an API for XBee 802.15.4 and ZNet radios
 * 
 * Objectives: 
 * 	Focus on support for a single version of firmware for both ZNet and 802.15.4 XBee radios; this would likely be the latest stable. 
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
 * This API has been developed and tested with rxtx-2.1-7r2 on windows mac, and linux. Other versions/OSes may work.
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

	// Default timeout of 5 seconds
	public final int DEFAULT_TIMEOUT = 5000;
	
	private final int maxPacketListSize = 100;
	
	private Object newPacketNotification = new Object();

	private int sendSynchronousTimeout = DEFAULT_TIMEOUT;

	// TODO clear this list after it reaches a certain size
	private List<XBeeResponse> packetList = new ArrayList<XBeeResponse>();

	private List<XBeeResponse> synchronousSendPacketList = new ArrayList<XBeeResponse>();
	// flag used to only accumulate packets when the synchronousSend method is in use
	private boolean synchronousCollect = false;

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

	/**
	 * NOTE: This method is not thread safe. 
	 * 
	 * If multiple threads send simultaneously, it's possible for the packets to get interspersed.  
	 * It is responsibility of the client to provide synchronization if multiple threads are sending.
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
	 * @param command
	 * @return
	 * @throws XBeeException
	 */
	public XBeeResponse sendAtCommand(AtCommand command) throws XBeeException {
		return this.sendSynchronous(command);
	}
	
	/**
	 * Synchronous method for sending an XBeeRequest and obtaining the 
	 * corresponding response (response that has same frame id).
	 * 
	 * This method should only be called with requests that receive a response of
	 * type XBeeFrameIdResponse and you should attempt to use a unique frame id (see getNextFrameId)
	 * 
	 * You may get unexpected results if you are receiving sample data from radio while using this method.
	 * 
	 * WARNING this method should be considered experimental -- use sendAsynchronous and getResponse for best results.
	 * 
	 * TODO provide method with timeout arg.  modify this method to not timeout since it does not accept a timeout param.
	 * 
	 * NOTE: this method is not thread-safe
	 * 
	 * @param xbeeRequest
	 * 
	 * @return
	 * @throws XBeeException 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws XBeeTimeoutException 
	 */
	public XBeeResponse sendSynchronous(XBeeRequest xbeeRequest) throws XBeeException {

		if (xbeeRequest.getFrameId() == XBeeRequest.NO_RESPONSE_FRAME_ID) {
			throw new XBeeException("Frame Id cannot be 0 for a synchronous call -- it will always timeout as there is no response!");
		}

		XBeePacket txPacket = xbeeRequest.getXBeePacket();

		XBeeResponse response = null;
		
		try {
			this.sendPacket(txPacket);

			synchronized (newPacketNotification) {
				// start accumulating packets
				this.synchronousCollect = true;
				
				// first remove any old packets
				synchronousSendPacketList.clear();
				
				// log.debug("waiting");
				
				long now = System.currentTimeMillis();
				
				// releases newPacketNotification and waits for next packet
				newPacketNotification.wait(sendSynchronousTimeout);
				
				if ((System.currentTimeMillis() - now) >= sendSynchronousTimeout && synchronousSendPacketList.size() == 0) {
					throw new XBeeTimeoutException();
				} else {
					if (synchronousSendPacketList.size() == 0) {
						// didn't think this would happen?
						throw new RuntimeException("No response!");
					} else if (synchronousSendPacketList.size() > 1) {
						// TODO this is likely to occur if radio is sending back samples
	
						boolean waited = false;
						
						while (response == null) {
							for (XBeeResponse rxResponse : synchronousSendPacketList) {
								if (rxResponse instanceof XBeeFrameIdResponse && ((XBeeFrameIdResponse)rxResponse).getFrameId() == xbeeRequest.getFrameId()) {
									// frame id matches -- yay we found it
									response = rxResponse;
									break;	
								}
							}	
	
							if (response == null) {
								// hmm we didn't a packet.. we will
								// wait a bit more if this is the first time around
								
								if (!waited) {
									// the radio may be receiving I/O samples at
									// a high rate.. wait just a bit longer
									
									// TODO this is still not entirely correct since this gets notified once per packet
									// we should wait an arbitrary amount of time or # packets, whichever occurs first before giving up
									newPacketNotification.wait(250);
									
									waited = true;
								} else {
									throw new XBeeException("Packets were received but not a TX_16_STATUS_RESPONSE");
								}
							}
						}
					} else {
						response = (XBeeResponse) synchronousSendPacketList.get(0);
	
						if (response == null) {
							throw new XBeeException("I'm at a loss of words.  the response is null");
						}
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof XBeeException) {
				throw (XBeeException) e;
			} else {
				throw new XBeeException(e);	
			}
		} finally {
			// indicate we are not accumulating packets anymore
			this.synchronousCollect = false;
		}
			
		return response;
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
		
		if (synchronousCollect) {
			synchronousSendPacketList.add(packet);	
		}
		
		if (packetList.size() == maxPacketListSize) {
			packetList.remove(maxPacketListSize - 1);
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

	public int getSendSynchronousTimeout() {
		return sendSynchronousTimeout;
	}

	/**
	 * This affects how long we wait for a response during a synchronous call before giving up
	 * and throwing a timeout exception
	 * 
	 * @param sendSynchronousTimeout
	 */
	public void setSendSynchronousTimeout(int sendSynchronousTimeout) {
		this.sendSynchronousTimeout = sendSynchronousTimeout;
	}
}

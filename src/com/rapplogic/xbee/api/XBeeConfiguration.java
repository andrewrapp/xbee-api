package com.rapplogic.xbee.api;

public class XBeeConfiguration {

	private boolean shutdownHook = false;
	private boolean startupChecks = false;
	private int maxQueueSize = 100;
	private int sendSynchronousTimeout = 5000;
	private ResponseFilter responseQueueFilter;
	
	private final ResponseFilter noRequestResponseQueueFilter = new ResponseFilter() {
		public boolean accept(XBeeResponse response) {
			return response instanceof NoRequestResponse;
		}
	};
	
	public XBeeConfiguration() {

	}

	/**
	 * Controls is a startup check is performed when connecting to the XBee.
	 * The startup check attempts to determine the firmware type and if it is 
	 * configured correctly for use with this software.  Default is true.
	 *  
	 * @param startupChecks
	 */
	public XBeeConfiguration withShutdownHook(boolean shutdownHook) {
		this.shutdownHook = shutdownHook;
		return this;
	}
	
	/**
	 * Controls is a startup check is performed when connecting to the XBee.
	 * The startup check attempts to determine the firmware type and if it is 
	 * configured correctly for use with this software.  Default is true.
	 *  
	 * @param startupChecks
	 */
	public XBeeConfiguration withStartupChecks(boolean startupChecks) {
		this.startupChecks = startupChecks;
		return this;
	}

	/**
	 * Sets the maximum size of the internal queue that supports the getResponse(..) method.
	 * Packets are removed from the head of the queue once this limit is reached.  The default is 100
	 * 
	 * @param size
	 */
	public XBeeConfiguration withMaxQueueSize(int size) {	
		if (size <= 0) {
			throw new IllegalArgumentException("Size must be > 0");
		}
		
		this.maxQueueSize = size;
		return this;
	}
	
	public XBeeConfiguration withResponseQueueFilter(ResponseFilter filter) {
		this.responseQueueFilter = filter;
		return this;
	}

	public XBeeConfiguration withSendSynchronousTimeout(int sendSynchronousTimeout) {
		this.sendSynchronousTimeout = sendSynchronousTimeout;
		return this;
	}
	
	/**
	 * Only adds responses that implement NoRequestResponse
	 * 
	 * @return
	 */
	public XBeeConfiguration withNoRequestResponseQueueFilter() {
		this.responseQueueFilter = this.noRequestResponseQueueFilter;
		return this;
	}	

	public boolean isStartupChecks() {
		return startupChecks;
	}

	public int getMaxQueueSize() {
		return maxQueueSize;
	}

	public ResponseFilter getResponseQueueFilter() {
		return responseQueueFilter;
	}

	public int getSendSynchronousTimeout() {
		return sendSynchronousTimeout;
	}

	public boolean isShutdownHook() {
		return shutdownHook;
	}
}

package com.rapplogic.xbee.api;

/**
 * I usually detest checked exceptions but given this is a public api, it is reasonable to
 * notify users what they can expect.
 * 
 * @author andrew
 *
 */
public class XBeeException extends Exception {

	private static final long serialVersionUID = -5501299728920565639L;
	private Exception cause;
	
	public XBeeException(String message) {
		super(message);
	}
	
	public XBeeException() {
		super();
	}
	
	public XBeeException(Exception cause) {
		super();
		this.setCause(cause);
	}

	public Exception getCause() {
		return cause;
	}

	public void setCause(Exception cause) {
		this.cause = cause;
	}
}

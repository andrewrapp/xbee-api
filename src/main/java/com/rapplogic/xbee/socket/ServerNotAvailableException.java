package com.rapplogic.xbee.socket;

public class ServerNotAvailableException extends Exception {

	public ServerNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerNotAvailableException(String message) {
		super(message);
	}
}
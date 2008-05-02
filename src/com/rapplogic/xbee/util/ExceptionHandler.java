package com.rapplogic.xbee.util;

import com.rapplogic.xbee.api.XBeeException;

public class ExceptionHandler {

	public static Exception handleAndThrow(Exception e) throws XBeeException {
		if (e instanceof XBeeException) {
			throw (XBeeException) e;
		} else {
			throw new XBeeException(e);
		}
	}
}

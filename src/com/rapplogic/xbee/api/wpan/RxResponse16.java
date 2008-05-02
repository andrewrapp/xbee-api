package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.api.XBeeAddress16;

/**
 * 
 * @author Andrew Rapp
 *
 */
public class RxResponse16 extends RxResponse {
	
	public XBeeAddress16 getRemoteAddress() {
		return (XBeeAddress16) this.getSourceAddress();
	}
}
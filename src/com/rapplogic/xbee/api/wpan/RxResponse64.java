package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.api.XBeeAddress64;

/**
 * 
 * @author Andrew Rapp
 *
 */
public class RxResponse64 extends RxResponse {
	
	public XBeeAddress64 getRemoteAddress() {
		return (XBeeAddress64) this.getSourceAddress();
	}
}
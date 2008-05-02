package com.rapplogic.xbee.api.zigbee;

import com.rapplogic.xbee.util.ByteUtils;

/**
 * 
 * @author Andrew Rapp
 *
 */
public class ZNetRxResponse extends ZNetRxBaseResponse {

	private int[] data;
	
	public ZNetRxResponse() {
		super();
	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}
	
	public String toString() {
		return super.toString() + 
			",data=" + ByteUtils.toBase16(this.data);
	}
}
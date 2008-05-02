package com.rapplogic.xbee.api.wpan;

import com.rapplogic.xbee.util.ByteUtils;

/**
 * 
 * @author Andrew Rapp
 *
 */
public class RxResponse extends RxBaseResponse {
	
	private int[] data;
	
	public RxResponse() {

	}

	public int[] getData() {
		return data;
	}

	public void setData(int[] data) {
		this.data = data;
	}
	
	public String toString() {
		return super.toString() +
			"data=" + ByteUtils.toBase16(this.data);
	}	
}
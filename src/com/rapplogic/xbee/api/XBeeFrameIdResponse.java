package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.ByteUtils;

public abstract class XBeeFrameIdResponse extends XBeeResponse {
	
	private int frameId;

	public int getFrameId() {
		return frameId;
	}

	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}
	
	public String toString() {
		return super.toString() + ",frameId=" + ByteUtils.toBase16(this.frameId);
	}
}
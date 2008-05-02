package com.rapplogic.xbee.api;

import com.rapplogic.xbee.util.ByteUtils;

/**
 * TODO RX_64_RESPONSE, RX_64_IO_RESPONSE
 * 
 * @author andrew
 *
 */
public abstract class XBeeResponse {
	
	// TODO enum these
	public final static int RX_64_RESPONSE = 0x80;
	public final static int RX_16_RESPONSE = 0x81;
	public final static int RX_64_IO_RESPONSE = 0x82;
	public final static int RX_16_IO_RESPONSE = 0x83;
	public final static int AT_RESPONSE = 0x88;
	public final static int TX_16_STATUS_RESPONSE = 0x89;
	public final static int MODEM_STATUS_RESPONSE = 0x8a;
	public final static int ZNET_RX_RESPONSE = 0x90;
	public final static int ZNET_TX_STATUS_RESPONSE = 0x8b;
	public final static int ZNET_REMOTE_AT_RESPONSE = 0x97;
	public final static int ZNET_IO_SAMPLE_RESPONSE = 0x92;
	public final static int ZNET_IO_NODE_IDENTIFIER_RESPONSE = 0x95;
	
	public final static int ERROR_RESPONSE = -1;

	private int apiId;
	private int checksum;

	private XBeePacketLength length;
	
	// TODO create Error/ErrorList object
	private boolean error = false;
	private String errorMsg;
		
	public XBeeResponse() {

	}

	public XBeePacketLength getLength() {
		return length;
	}

	public void setLength(XBeePacketLength length) {
		this.length = length;
	}

	public int getApiId() {
		return apiId;
	}

	public void setApiId(int apiId) {
		this.apiId = apiId;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}
	
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String toString() {
		return "apiId=" + ByteUtils.toBase16(this.apiId) +
			",length=" + length.get16BitValue() + 
			",checksum=" + ByteUtils.toBase16(checksum) +
			",error=" + this.error +
			",errorMessage=" + this.errorMsg;
	}
}
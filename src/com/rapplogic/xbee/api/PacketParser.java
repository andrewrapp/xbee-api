/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rapplogic.xbee.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetNodeIdentificationResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.IIntInputStream;
import com.rapplogic.xbee.util.InputStreamWrapper;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Reads a packet from the input stream, verifies checksum and creates an XBeeResponse object
 * <p/>
 * Notes:
 * <p/>
 * Escaped bytes increase packet length but packet stated length only indicates un-escaped bytes.
 * Stated length includes all bytes after Length bytes, not including the checksum
 * <p/>
 * @author Andrew Rapp
 *
 */
public class PacketParser implements IIntInputStream, IPacketParser {

	private final static Logger log = Logger.getLogger(PacketParser.class);

	private IIntInputStream in;
	
	// size of packet after special bytes have been escaped
	private XBeePacketLength length;
	private Checksum checksum = new Checksum();
	
	private boolean done = false;
	
	private int bytesRead;
	private int escapeBytes;

	private XBeeResponse response;
	private ApiId apiId;
	private int intApiId;
	
	private static Map<Integer, Class<? extends XBeeResponse>> handlerMap = new HashMap<Integer, Class<? extends XBeeResponse>>();
	
	// TODO reuse this object for all packets
	
	// experiment to preserve original byte array for transfer over network (Starts with length)
	private IntArrayOutputStream rawBytes = new IntArrayOutputStream();
	
	static {
		// TODO put all response handlers in specific packet and load all.  implement static handlesApi method
		handlerMap.put(ApiId.AT_RESPONSE.getValue(), AtCommandResponse.class);
		handlerMap.put(ApiId.MODEM_STATUS_RESPONSE.getValue(), ModemStatusResponse.class);
		handlerMap.put(ApiId.REMOTE_AT_RESPONSE.getValue(), RemoteAtResponse.class);
		handlerMap.put(ApiId.RX_16_IO_RESPONSE.getValue(), RxResponseIoSample.class);
		handlerMap.put(ApiId.RX_64_IO_RESPONSE.getValue(), RxResponseIoSample.class);
		handlerMap.put(ApiId.RX_16_RESPONSE.getValue(), RxResponse16.class);
		handlerMap.put(ApiId.RX_64_RESPONSE.getValue(), RxResponse64.class);
		handlerMap.put(ApiId.TX_STATUS_RESPONSE.getValue(), TxStatusResponse.class);
		handlerMap.put(ApiId.ZNET_EXPLICIT_RX_RESPONSE.getValue(), ZNetExplicitRxResponse.class);
		handlerMap.put(ApiId.ZNET_IO_NODE_IDENTIFIER_RESPONSE.getValue(), ZNetNodeIdentificationResponse.class);
		handlerMap.put(ApiId.ZNET_IO_SAMPLE_RESPONSE.getValue(), ZNetRxIoSampleResponse.class);
		handlerMap.put(ApiId.ZNET_RX_RESPONSE.getValue(), ZNetRxResponse.class);
		handlerMap.put(ApiId.ZNET_TX_STATUS_RESPONSE.getValue(), ZNetTxStatusResponse.class);
	}
	
	static void registerResponseHandler(int apiId, Class<? extends XBeeResponse> clazz) {
		if (handlerMap.get(apiId) == null) {
			log.info("Registering response handler " + clazz.getCanonicalName() + " for apiId: " + apiId);
		} else {
			log.warn("Overriding existing implementation: " + handlerMap.get(apiId).getCanonicalName() + ", with " + clazz.getCanonicalName() + " for apiId: " + apiId);
		}
		
		handlerMap.put(apiId, clazz);
	}
	
	static void unRegisterResponseHandler(int apiId) {
		if (handlerMap.get(apiId) != null) {
			log.info("Unregistering response handler " + handlerMap.get(apiId).getCanonicalName() + " for apiId: " + apiId);
			handlerMap.remove(apiId);
		} else {
			throw new IllegalArgumentException("No response handler for: " + apiId);
		}
	}
	
	public PacketParser(InputStream in) {
		this.in = new InputStreamWrapper(in);
	}
	
	// for parsing a packet from a byte array
	public PacketParser(IIntInputStream in) {
		this.in = in;
	}
	
	/**
	 * This method is guaranteed (unless I screwed up) to return an instance of XBeeResponse and should never throw an exception
	 * If an exception occurs, it will be packaged and returned as an ErrorResponse. 
	 * 
	 * @return
	 */
	public XBeeResponse parsePacket() {
		
		Exception exception = null;
		
		try {
			// BTW, length doesn't account for escaped bytes
			int msbLength = this.read("Length MSB");
			int lsbLength = this.read("Length LSB");
			
			// length of api structure, starting here (not including start byte or length bytes, or checksum)
			this.length = new XBeePacketLength(msbLength, lsbLength);

			log.debug("packet length is " + ByteUtils.formatByte(length.getLength()));
			
			// total packet length = stated length + 1 start byte + 1 checksum byte + 2 length bytes
			
			intApiId = this.read("API ID");
			
			this.apiId = ApiId.get(intApiId);
			
			if (apiId == null) {
				this.apiId = ApiId.UNKNOWN;	
			}
			
			log.info("Handling ApiId: " + apiId);
			
			// TODO parse I/O data page 12. 82 API Identifier Byte for 64 bit address A/D data (83 is for 16bit A/D data)
			// TODO XBeeResponse should implement an abstract parse method
			
			for (Integer handlerApiId : handlerMap.keySet()) {
				if (intApiId == handlerApiId) {
					log.debug("Found response handler for apiId [" + ByteUtils.toBase16(intApiId) + "]: " + handlerMap.get(handlerApiId).getCanonicalName());		
					response = (XBeeResponse) handlerMap.get(handlerApiId).newInstance();
					response.parse(this);
					break;
				}
			}
			
			if (response == null) {
				log.info("Did not find a response handler for ApiId [" + ByteUtils.toBase16(intApiId) + "].  Returning GenericResponse");
				response = new GenericResponse();
				response.parse(this);
			}
			
			response.setChecksum(this.read("Checksum"));
			
			if (!this.isDone()) {
				throw new XBeeParseException("There are remaining bytes according to stated packet length but we have read all the bytes we thought were required for this packet (if that makes sense)");
			}
			
			response.finish();
		} catch (Exception e) {
			// added bytes read for troubleshooting
			log.error("Failed due to exception.  Returning ErrorResponse.  bytes read: " + ByteUtils.toBase16(rawBytes.getIntArray()), e);
			exception = e;
			
			response = new ErrorResponse();
			
			((ErrorResponse)response).setErrorMsg(exception.getMessage());	
			// but this isn't
			((ErrorResponse)response).setException(e);
		} finally {
			response.setLength(length);
			response.setApiId(apiId);			
			// preserve original byte array for transfer over networks
			response.setRawPacketBytes(rawBytes.getIntArray());
		}
		
		return response;
	}
	
	/**
	 * Same as read() but logs the context of the byte being read.  useful for debugging
	 */
	public int read(String context) throws IOException {
		int b = this.read();
		log.debug("Read " + context + " byte, val is " + ByteUtils.formatByte(b));
		return b;
	}
	
	/**
	 * This method should only be called by read()
	 * 
	 * @throws IOException
	 */
	private int readFromStream() throws IOException {
		int b = in.read();
		// save raw bytes to transfer via network
		rawBytes.write(b);		
		
		return b;
	}
	
	/**
	 * This method reads bytes from the underlying input stream and performs the following tasks:
	 * 1. Keeps track of how many bytes we've read
	 * 2. Un-escapes bytes if necessary and verifies the checksum.
	 */
	public int read() throws IOException {

		if (done) {
			throw new XBeeParseException("Packet has read all of its bytes");
		}
		
		int b = this.readFromStream();

		
		if (b == -1) {
			throw new XBeeParseException("Read -1 from input stream while reading packet!");
		}
		
		if (XBeePacket.isSpecialByte(b)) {
			log.debug("Read special byte that needs to be unescaped"); 
			
			if (b == XBeePacket.SpecialByte.ESCAPE.getValue()) {
				log.debug("found escape byte");
				// read next byte
				b = this.readFromStream();
				
				log.debug("next byte is " + ByteUtils.formatByte(b));
				b = 0x20 ^ b;
				log.debug("unescaped (xor) byte is " + ByteUtils.formatByte(b));
					
				escapeBytes++;
			} else {
				// TODO some responses such as AT Response for node discover do not escape the bytes?? shouldn't occur if AP mode is 2?
				// while reading remote at response Found unescaped special byte base10=19,base16=0x13,base2=00010011 at position 5 
				log.warn("Found unescaped special byte " + ByteUtils.formatByte(b) + " at position " + bytesRead);
			}
		}
	
		bytesRead++;

		// do this only after reading length bytes
		if (bytesRead > 2) {

			// when verifying checksum you must add the checksum that we are verifying
			// checksum should only include unescaped bytes!!!!
			// when computing checksum, do not include start byte, length, or checksum; when verifying, include checksum
			checksum.addByte(b);
			
			log.debug("Read byte " + ByteUtils.formatByte(b) + " at position " + bytesRead + ", packet length is " + this.length.get16BitValue() + ", #escapeBytes is " + escapeBytes + ", remaining bytes is " + this.getRemainingBytes());
			
			// escape bytes are not included in the stated packet length
			if (this.getFrameDataBytesRead() >= (length.get16BitValue() + 1)) {
				// this is checksum and final byte of packet
				done = true;
				
				log.debug("Checksum byte is " + b);
				
				if (!checksum.verify()) {
					throw new XBeeParseException("Checksum is incorrect.  Expected 0xff, but got " + checksum.getChecksum());
				}
			}
		}

		return b;
	}
		
	/**
	 * Reads all remaining bytes except for checksum
	 * @return
	 * @throws IOException
	 */
	public int[] readRemainingBytes() throws IOException {
		
		// minus one since we don't read the checksum
		int[] value = new int[this.getRemainingBytes() - 1];
		
		log.debug("There are " + value.length + " remaining bytes");
		
		for (int i = 0; i < value.length; i++) {
			value[i] = this.read("Remaining bytes " + i);
		}
		
		return value;
	}
	
	public XBeeAddress64 parseAddress64() throws IOException {
		XBeeAddress64 addr = new XBeeAddress64();
		
		for (int i = 0; i < 8; i++) {
			addr.getAddress()[i] = this.read("64-bit Address byte " + i);
		}	
		
		return addr;
	}
	
	public XBeeAddress16 parseAddress16() throws IOException {
		XBeeAddress16 addr16 = new XBeeAddress16();
		
		addr16.setMsb(this.read("Address 16 MSB"));
		addr16.setLsb(this.read("Address 16 LSB"));
		
		return addr16;
	}
	
	/**
	 * Returns number of bytes remaining, relative to the stated packet length (not including checksum).
	 * @return
	 */
	public int getFrameDataBytesRead() {
		// subtract out the 2 length bytes
		return this.getBytesRead() - 2;
	}
	
	/**
	 * Number of bytes remaining to be read, including the checksum
	 * @return
	 */
	public int getRemainingBytes() {
		// add one for checksum byte (not included) in packet length
		return this.length.get16BitValue() - this.getFrameDataBytesRead() + 1;
	}
	
	// get unescaped packet length
	// get escaped packet length
	
	/**
	 * Does not include any escape bytes
	 * @return
	 */
	public int getBytesRead() {
		return bytesRead;
	}

	public void setBytesRead(int bytesRead) {
		this.bytesRead = bytesRead;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone(boolean done) {
		this.done = done;
	}

	public int getChecksum() {
		return checksum.getChecksum();
	}

	public XBeePacketLength getLength() {
		return length;
	}

	public ApiId getApiId() {
		return apiId;
	}
	
	public int getIntApiId() {
		return this.intApiId;
	}
}

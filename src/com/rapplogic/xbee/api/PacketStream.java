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

import org.apache.log4j.Logger;

import com.rapplogic.xbee.api.AtCommandResponse.Status;
import com.rapplogic.xbee.api.wpan.IoSample;
import com.rapplogic.xbee.api.wpan.RxBaseResponse;
import com.rapplogic.xbee.api.wpan.RxResponse;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;
import com.rapplogic.xbee.api.zigbee.ZNetExplicitRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetNodeIdentificationResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxBaseResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZNetTxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;
import com.rapplogic.xbee.util.DoubleByte;
import com.rapplogic.xbee.util.IIntArrayInputStream;
import com.rapplogic.xbee.util.InputStreamWrapper;
import com.rapplogic.xbee.util.IntArrayOutputStream;

/**
 * Reads a packet from the input stream, verifies checksum and creates an XBeeResponse object
 * 
 * Notes:
 * 
 * Escaped bytes increase packet length but packet stated length only indicates un-escaped bytes.
 * Stated length includes all bytes after Length bytes, not including the checksum
 * 
 * @author Andrew Rapp
 *
 */
public class PacketStream implements IIntArrayInputStream {

	private final static Logger log = Logger.getLogger(PacketStream.class);

	private IIntArrayInputStream in;
	
	private XBeePacketLength length;
	private Checksum checksum = new Checksum();
	
	private boolean done = false;
	
	private int bytesRead;
	private int escapeBytes;

	private XBeeResponse response;
	private ApiId apiId;
	
	// experiment to preserve original byte array for transfer over network
	private IntArrayOutputStream out = new IntArrayOutputStream();
               
	public PacketStream(InputStream in) {
		this.in = new InputStreamWrapper(in);
	}
	
	// for parsing a packet from a byte array
	public PacketStream(IIntArrayInputStream in) {
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
			
			int intApiId = this.read("API ID");
			
			this.apiId = ApiId.get(intApiId);
			
			if (apiId == null) {
				throw new XBeeParseException("Unhandled Api id: " + ByteUtils.toBase16(intApiId));	
			}
			
			log.info("Handling ApiId: " + apiId);
			
			// TODO parse I/O data page 12. 82 API Identifier Byte for 64 bit address A/D data (83 is for 16bit A/D data)
			// TODO XBeeResponse subclasses should implement a parse method
			
			if (apiId == ApiId.MODEM_STATUS_RESPONSE) {
				parseModemStatusResponse();
			} else if (apiId == ApiId.RX_16_RESPONSE) {
				parseRxResponse();
			} else if (apiId == ApiId.RX_16_IO_RESPONSE) {
				parseRxResponse();
			} else if (apiId == ApiId.RX_64_RESPONSE) {
				parseRxResponse();
			} else if (apiId == ApiId.RX_64_IO_RESPONSE) {
				parseRxResponse();
			} else if (apiId == ApiId.AT_RESPONSE) {
				parseAtResponse();
			} else if (apiId == ApiId.TX_STATUS_RESPONSE) {
				parseTxStatusResponse();
			} else if (apiId == ApiId.ZNET_REMOTE_AT_RESPONSE) {
				parseRemoteAtResponse();
			} else if (apiId == ApiId.ZNET_TX_STATUS_RESPONSE) { 
				parseZNetTxStatusResponse();
			} else if (apiId == ApiId.ZNET_RX_RESPONSE) {
				parseZNetRxResponse();
			} else if (apiId == ApiId.ZNET_EXPLICIT_RX_RESPONSE) {
				parseZNetRxResponse();
			} else if (apiId == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
				parseZNetRxResponse();
			} else if (apiId == ApiId.ZNET_IO_NODE_IDENTIFIER_RESPONSE) {
				parseZNetNodeIdentifierResponse();		
			}
			
			response.setChecksum(this.read("Checksum"));
			
			if (!this.isDone()) {
				throw new XBeeParseException("There are remaining bytes according to stated packet length but we have read all the bytes we thought were required for this packet (if that makes sense)");
			}
		} catch (Exception e) {
			// added bytes read for troubleshooting
			log.error("Failed due to exception.  Returning ErrorResponse.  bytes read: " + ByteUtils.toBase16(out.getIntArray()), e);
			exception = e;
			
			response = new ErrorResponse();
			
			// TODO this is redundant
			((ErrorResponse)response).setErrorMsg(exception.getMessage());	
			// but this isn't
			((ErrorResponse)response).setException(e);
		}
		
		response.setLength(length);
		response.setApiId(apiId);
		
		// preserve original byte array for transfer over networks
		response.setPacketBytes(out.getIntArray());

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
	 * Only this method should call in.read()
	 * 
	 * @throws IOException
	 */
	private int readFromStream() throws IOException {
		int b = in.read();
		// save raw bytes to transfer via network
		out.write(b);		
		
		return b;
	}
	
	/**
	 * This method reads bytes from the underlying input stream and performs the following tasks:
	 * keeps track of how many bytes we've read, un-escapes bytes if necessary and verifies the checksum.
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

	private void parseRemoteAtResponse() throws IOException {
		
		response = new ZNetRemoteAtResponse();
		
		((ZNetRemoteAtResponse)response).setFrameId(this.read("Remote AT Response Frame Id"));
		
		((ZNetRemoteAtResponse)response).setRemoteAddress64(this.parseAddress64());
		
		((ZNetRemoteAtResponse)response).setRemoteAddress16(this.parseAddress16());
		
		char cmd1 = (char)this.read("Command char 1");
		char cmd2 = (char)this.read("Command char 2");
		//((ZNetRemoteAtResponse)response).setCommand(new String(new char[] {cmd1, cmd2}));
		((ZNetRemoteAtResponse)response).setChar1(cmd1);
		((ZNetRemoteAtResponse)response).setChar2(cmd2);
		
		int status = this.read("AT Response Status");
		((ZNetRemoteAtResponse)response).setStatus(ZNetRemoteAtResponse.Status.get(status));
		
		((ZNetRemoteAtResponse)response).setValue(this.readRemainingBytes());
	}
	
	private void parseAtResponse() throws IOException {
		//log.debug("AT Response");
		
		response = new AtCommandResponse();
		
		((AtCommandResponse)response).setFrameId(this.read("AT Response Frame Id"));
		((AtCommandResponse)response).setChar1(this.read("AT Response Char 1"));
		((AtCommandResponse)response).setChar2(this.read("AT Response Char 2"));
		((AtCommandResponse)response).setStatus(Status.get(this.read("AT Response Status")));
							
		((AtCommandResponse)response).setValue(this.readRemainingBytes());
	}

	private void parseZNetTxStatusResponse() throws IOException {
		
		response = new ZNetTxStatusResponse();
		
		((ZNetTxStatusResponse)response).setFrameId(this.read("ZNet Tx Status Frame Id"));

		((ZNetTxStatusResponse)response).setRemoteAddress16(this.parseAddress16());
		((ZNetTxStatusResponse)response).setRetryCount(this.read("ZNet Tx Status Tx Count"));
		
		// TODO need more efficient method of looking up value, e.g hashtable
		int deliveryStatus = this.read("ZNet Tx Status Delivery Status");
		((ZNetTxStatusResponse)response).setDeliveryStatus(ZNetTxStatusResponse.DeliveryStatus.get(deliveryStatus));
		
		int discoveryStatus = this.read("ZNet Tx Status Discovery Status");
		((ZNetTxStatusResponse)response).setDiscoveryStatus(ZNetTxStatusResponse.DiscoveryStatus.get(discoveryStatus));
	}

	private void parseZNetRxResponse() throws IOException {
		
		// TODO this needs OO refactoring
		if (this.apiId == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
			response = new ZNetRxIoSampleResponse();
		} else if (this.apiId == ApiId.ZNET_RX_RESPONSE){
			response = new ZNetRxResponse();	
		} else {
			response = new ZNetExplicitRxResponse();
		}
		
		((ZNetRxBaseResponse)response).setRemoteAddress64(this.parseAddress64());
		((ZNetRxBaseResponse)response).setRemoteAddress16(this.parseAddress16());
		
		if (this.apiId == ApiId.ZNET_EXPLICIT_RX_RESPONSE) {
			((ZNetExplicitRxResponse)response).setSourceEndpoint(this.read("Reading Source Endpoint"));
			((ZNetExplicitRxResponse)response).setDestinationEndpoint(this.read("Reading Destination Endpoint"));
			DoubleByte clusterId = new DoubleByte();
			clusterId.setMsb(this.read("Reading Cluster Id MSB"));
			clusterId.setLsb(this.read("Reading Cluster Id LSB"));
			((ZNetExplicitRxResponse)response).setClusterId(clusterId);
			
			DoubleByte profileId = new DoubleByte();
			profileId.setMsb(this.read("Reading Profile Id MSB"));
			profileId.setMsb(this.read("Reading Profile Id LSB"));
			((ZNetExplicitRxResponse)response).setProfileId(profileId);
		}
		
		int option = this.read("ZNet RX Response Option");
		
		if (this.apiId == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
			parseZNetIoSampleResponse((ZNetRxIoSampleResponse)response);
		} else {
			// TODO option only set for rx response??
			((ZNetRxBaseResponse)response).setOption(ZNetRxBaseResponse.Option.get(option));		
			((ZNetRxResponse)response).setData(this.readRemainingBytes());			
		}
	}
	
	private void parseZNetNodeIdentifierResponse() throws IOException {
			
		response = new ZNetNodeIdentificationResponse();	
		
		((ZNetNodeIdentificationResponse)response).setRemoteAddress64(this.parseAddress64());
		((ZNetNodeIdentificationResponse)response).setRemoteAddress16(this.parseAddress16());
		
		int option = this.read("Option");
		((ZNetNodeIdentificationResponse)response).setOption(ZNetNodeIdentificationResponse.Option.get(option));		

		// again with the addresses
		((ZNetNodeIdentificationResponse)response).setRemoteAddress64_2(this.parseAddress64());
		((ZNetNodeIdentificationResponse)response).setRemoteAddress16_2(this.parseAddress16());
		
		StringBuffer ni = new StringBuffer();
		
		int ch = 0;
		
		// NI is terminated with 0
		while ((ch = this.read("Node Identifier")) != 0) {
			ni.append((char)ch);			
		}
		
		((ZNetNodeIdentificationResponse)response).setNodeIdentifier(ni.toString());
		((ZNetNodeIdentificationResponse)response).setParentAddress(this.parseAddress16());		
		
		int deviceType = this.read("Device Type");
		
		((ZNetNodeIdentificationResponse)response).setDeviceType(ZNetNodeIdentificationResponse.DeviceType.get(deviceType));		
		
		int sourceAction = this.read("Source Action");
		((ZNetNodeIdentificationResponse)response).setSourceAction(ZNetNodeIdentificationResponse.SourceAction.get(sourceAction));	
		
		DoubleByte profileId = new DoubleByte();
		profileId.setMsb(this.read("Profile MSB"));
		profileId.setLsb(this.read("Profile LSB"));
		((ZNetNodeIdentificationResponse)response).setProfileId(profileId);
		
		DoubleByte mfgId = new DoubleByte();
		mfgId.setMsb(this.read("MFG MSB"));
		mfgId.setLsb(this.read("MFG LSB"));
		((ZNetNodeIdentificationResponse)response).setMfgId(mfgId);
	}
	
	private void parseZNetIoSampleResponse(ZNetRxIoSampleResponse response) throws IOException {
		// TODO expose as interface
		response.parse(this);
	}
	
	private void parseModemStatusResponse() throws IOException {		
		response = new ModemStatusResponse();
		((ModemStatusResponse)response).setStatus(ModemStatusResponse.Status.get(this.read("Modem Status")));
	}
	
	/**
	 * TODO untested after 64-bit refactoring
	 * 
	 * @throws IOException
	 */
	private void parseRxResponse() throws IOException {

		if (apiId == ApiId.RX_16_RESPONSE || apiId == ApiId.RX_64_RESPONSE) {
			if (apiId == ApiId.RX_16_RESPONSE) {
				response = new RxResponse16();	
				((RxBaseResponse)response).setSourceAddress(this.parseAddress16());
			} else {
				response = new RxResponse64();
				((RxBaseResponse)response).setSourceAddress(this.parseAddress64());
			}
		} else {
			// TODO subclass RxResponseIoSample with 16 and 64bit classes
			response = new RxResponseIoSample();
			
			if (apiId == ApiId.RX_16_IO_RESPONSE) {
				((RxBaseResponse)response).setSourceAddress(this.parseAddress16());	
			} else {
				((RxBaseResponse)response).setSourceAddress(this.parseAddress64());
			}	
		}
		
		int rssi = this.read();
		
		// rssi is a negative dbm value
		((RxBaseResponse)response).setRssi(-rssi);
		
		int options = this.read();
		
		((RxBaseResponse)response).setOptions(options);
		
		if (apiId == ApiId.RX_16_RESPONSE || apiId == ApiId.RX_64_RESPONSE) {
			int[] payload = new int[length.getLength() - this.getFrameDataBytesRead()];
			
			int bytesRead = this.getFrameDataBytesRead();
			
			for (int i = 0; i < length.getLength() - bytesRead; i++) {
				payload[i] = this.read("Payload byte " + i);
				//log.debug("rx data payload [" + i + "] " + payload[i]);
			}				
			
			((RxResponse)response).setData(payload);
		} else {
			// I/O sample
			log.debug("this is a I/O sample!");
			
			this.parseIoResponse((RxResponseIoSample)response);
		}
	}
	
	private void parseTxStatusResponse() throws IOException {
		//log.debug("TxStatus");
		
		response = new TxStatusResponse();
		
		// parse TxStatus
		
		// frame id
		int frameId = this.read("TxStatus Frame Id");
		((TxStatusResponse)response).setFrameId(frameId);
		
		//log.debug("frame id is " + frameId);

		// Status: 0=Success, 1= No Ack, 2= CCA Failure, 3= Purge
		int status = this.read("TX Status");
		((TxStatusResponse)response).setStatus(TxStatusResponse.Status.get(status));
		
		//log.debug("status is " + status);
	}
	
	public void parseIoResponse(RxResponseIoSample response) throws IOException {

		// first byte is # of samples
		int samples = this.read("# I/O Samples");
		
		// create i/o samples array
		response.setSamples(new IoSample[samples]);
		
		// channel indicator 1
		response.setChannelIndicator1(this.read("Channel Indicator 1"));
		
		log.debug("channel indicator 1 is " + ByteUtils.formatByte(response.getChannelIndicator1()));
		
		// channel indicator 2 (dio)
		response.setChannelIndicator2(this.read("Channel Indicator 2"));
		
		log.debug("channel indicator 2 is " + ByteUtils.formatByte(response.getChannelIndicator2()));	
		
		// collect each sample
		for (int i = 0; i < response.getSamples().length; i++) {
			
			log.debug("parsing sample " + (i + 1));
			
			IoSample sample = parseIoSample(response);
			
			// attach sample to parent
			response.getSamples()[i] = sample;
		}
	}
	
	private IoSample parseIoSample(RxResponseIoSample response) throws IOException {

		IoSample sample = new IoSample(response);
		
		// DIO 8 occupies the first bit of the adcHeader
		if (response.containsDigital()) {
			// at least one DIO line is active
			// next two bytes are DIO
			
			log.debug("Digital I/O was received");
			
			sample.setDioMsb(this.read("DIO MSB"));
			sample.setDioLsb(this.read("DIO LSB"));
		}
		
		// ADC is active if any of bits 2-7 are on
		if (response.containsAnalog()) {
			// adc is active
			// adc is 10 bits
			
			log.debug("Analog input was received");
			
			// 10-bit values are read two bytes per sample
			
			int analog = 0;
			
			// Analog inputs A0-A5 are bits 2-7 of the adcHeader
			
			if (response.isA0Enabled()) {
				sample.setAnalog0(ByteUtils.parse10BitAnalog(this, analog));
				analog++;				
			}

			if (response.isA1Enabled()) {
				sample.setAnalog1(ByteUtils.parse10BitAnalog(this, analog));
				analog++;
			}

			if (response.isA2Enabled()) {
				sample.setAnalog2(ByteUtils.parse10BitAnalog(this, analog));
				analog++;
			}

			if (response.isA3Enabled()) {
				sample.setAnalog3(ByteUtils.parse10BitAnalog(this, analog));
				analog++;
			}

			if (response.isA4Enabled()) {
				sample.setAnalog4(ByteUtils.parse10BitAnalog(this, analog));
				analog++;
			}
			
			if (response.isA5Enabled()) {
				sample.setAnalog5(ByteUtils.parse10BitAnalog(this, analog));
				analog++;
			}
			
			log.debug("There are " + analog + " analog inputs turned on");
		}
		
		return sample;
	}
	
	private int[] readRemainingBytes() throws IOException {
		int[] value = new int[length.getLength() - this.getFrameDataBytesRead()];
		
		for (int i = 0; i < value.length; i++) {
			value[i] = this.read("Remaining bytes " + i);
			//log.debug("byte " + i + " is " + value[i]);
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
}

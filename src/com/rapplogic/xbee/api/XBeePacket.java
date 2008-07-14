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

import org.apache.log4j.Logger;

import com.rapplogic.xbee.util.ByteUtils;

public class XBeePacket {
	
	private final static Logger log = Logger.getLogger(XBeePacket.class);
	
	public final static int START_BYTE = 0x7e;
	public final static int ESCAPE = 0x7d;
	public final static int XON = 0x11;
	public final static int XOFF = 0x13;
	
	private int[] packet;
	
	/**
	 * I started off using bytes but quickly realized that java bytes are signed, so effectively only 7 bits.
	 * We should be able to use int instead.
	 * 
	 * 
	 * @param data
	 */
	public XBeePacket(int[] frameData) {
	
		// save pre escape length
		int preEscapeLength = frameData.length;
		
		// checksum is always computed on pre-escaped packet
		Checksum checksum = new Checksum();
		
		for (int i = 0; i < frameData.length; i++) {			
			checksum.addByte(frameData[i]);
		}
		
		checksum.compute();
		
		// packet size is frame data + start byte + 2 length bytes + checksum byte
		packet = new int[frameData.length + 4];
		packet[0] = START_BYTE;
		
		// Packet length does not include escape bytes 
		XBeePacketLength length = new XBeePacketLength(preEscapeLength);
		
		// msb length
		packet[1] = length.getMsb();
		// lsb length
		packet[2] = length.getLsb();
		
		for (int i = 0; i < frameData.length; i++) {
			if (frameData[i] > 255) {
				throw new RuntimeException("Value is greater than one byte: " + frameData[i]);
			}
			
			packet[3 + i] = frameData[i];
		}
		
		// set last byte as checksum
		// note: if checksum is not correct, XBee won't send out packet or return error.  ask me how I know.

		packet[packet.length - 1] = checksum.getChecksum();

//		for (int i = 0; i < packet.length; i++) {
//			log.debug("XBeeApi pre-escape packet byte " + i + " is " + ByteUtils.toBase16(packet[i]));
//		}
		
		packet = this.escapePacket(packet);
		
		for (int i = 0; i < packet.length; i++) {
			log.debug("Packet byte " + i + " is " + ByteUtils.toBase16(packet[i]));
		}
		
		if (packet.length >= 34) {
			log.warn("p.48 indicates DI can handle 100 bytes: RF transmission will also commence after 100 Bytes (maximum packet size) are received in the DI buffer.");
			// WTF p.48 indicates DI can handle 100 bytes: RF transmission will also commence after 100 Bytes (maximum packet size) are received in the DI buffer.
			//throw new RuntimeException("This packet exceeds the DI buffer limit of 34 bytes this API does not use Hardware Flow Control (XBee manual p.11)");
		}
	}
	
	/**
	 * Escape all bytes in packet after start byte, and including checksum
	 * 
	 * @param packet
	 * @return
	 */
	private int[] escapePacket(int[] packet) {
		int escapeBytes = 0;
		
		// escape packet.  start at one so we don't escape the start byte 
		for (int i = 1; i < packet.length; i++) {
			if (isSpecialByte(packet[i])) {
				log.debug("escapeFrameData: packet byte requires escaping byte " + ByteUtils.toBase16(packet[i]));
				escapeBytes++;
			}
		}

		if (escapeBytes == 0) {
			return packet;
		} else {
			log.debug("packet requires escaping");
			
			int[] escapePacket = new int[packet.length + escapeBytes];
			
			int pos = 1;
			
			escapePacket[0] = START_BYTE;
				
			for (int i = 1; i < packet.length; i++) {
				if (isSpecialByte(packet[i])) {
					escapePacket[pos] = ESCAPE;
					escapePacket[++pos] = 0x20 ^ packet[i];
					
					log.debug("escapeFrameData: xor'd byte is 0x" + Integer.toHexString(escapePacket[pos]));
				} else {
					escapePacket[pos] = packet[i];
				}
				
				pos++;
			}
			
			return escapePacket;
		}
	}
	
	public int[] getPacket() {
		return packet;
	}
	
	public static boolean isSpecialByte(int b) {
		if (b == XBeePacket.START_BYTE || b == XBeePacket.ESCAPE || b == XBeePacket.XON || b == XBeePacket.XOFF) {
			return true;
		}
		
		return false;
	}
	
	public String toString() {
		return ByteUtils.toBase16(this.packet);
	}
	
	public static void main(String[] args) {
		int[] payload = new int[4];
		
		payload[0] = 0x08;
		payload[1] = 0x52;
		payload[2] = 0x44;
		payload[3] = 0x4c;
		
//		for (int i = 0; i < payload.length; i++) {
//			payload[i] = (i + 245);
//		}
		
		new XBeePacket(payload);
	}
}
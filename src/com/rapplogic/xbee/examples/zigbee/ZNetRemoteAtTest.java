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

package com.rapplogic.xbee.examples.zigbee;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeRequest;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtRequest;
import com.rapplogic.xbee.api.zigbee.ZNetRemoteAtResponse;

/** 
 * Here are some examples of API usage.  Most of these examples rely on a pre-existing configuration
 * and should not be run blindly.
 * 
 * 
 * Crashed RXTX oops:
# An unexpected error has been detected by HotSpot Virtual Machine:
#
#  EXCEPTION_ACCESS_VIOLATION (0xc0000005) at pc=0x10009e69, pid=1820, tid=6480
#
# Java VM: Java HotSpot(TM) Client VM (1.5.0_06-b05 mixed mode, sharing)
# Problematic frame:
# C  [rxtxSerial.dll+0x9e69]
#
# An error report file with more information is saved as hs_err_pid1820.log
#
# If you would like to submit a bug report, please visit:
#   http://java.sun.com/webapps/bugreport/crash.jsp
#
 * @author andrew
 *
 */
public class ZNetRemoteAtTest {

	private final static Logger log = Logger.getLogger(ZNetRemoteAtTest.class);
	
	private ZNetRemoteAtTest() throws XBeeException, InterruptedException {
		
		XBee xbee = new XBee();
		
		try {
			xbee.open("COM5", 9600);			
			
			XBeeAddress64 addr64 = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);
			
			// coordinator SL is 0x40 0x3e 0x0f 0x30, SH same as remote
			
			// turn on D0 (Digital output high) 
			ZNetRemoteAtRequest request = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, XBeeAddress16.ZNET_BROADCAST, true, "D0", new int[] {5});
			xbee.sendAsynchronous(request);
			XBeeResponse response = xbee.getResponse();
			
			if (response.getApiId() == XBeeResponse.ZNET_REMOTE_AT_RESPONSE) {
				ZNetRemoteAtResponse remote = (ZNetRemoteAtResponse) response;
				log.info("turn on D0 remote at command status is " + remote.getStatus());
			}
			
			Thread.sleep(5000);
//			
//			// turn on D0 off 
			request = new ZNetRemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, addr64, XBeeAddress16.ZNET_BROADCAST, true, "D0", new int[] {4});
			xbee.sendAsynchronous(request);
			response = xbee.getResponse();
			
			if (response.getApiId() == XBeeResponse.ZNET_REMOTE_AT_RESPONSE) {
				ZNetRemoteAtResponse remote = (ZNetRemoteAtResponse) response;
				log.info("turn off D0 remote at command status is " + remote.getStatus());
			}			
		} finally {
			xbee.close();
		}
	}
	
	private ZNetRemoteAtResponse getZNetRemoteAtResponse(XBeeResponse response) {
		return (ZNetRemoteAtResponse) response;
	}
	
	public static void main(String[] args) throws XBeeException, InterruptedException {
		PropertyConfigurator.configure("log4j.properties");
		new ZNetRemoteAtTest();
	}
}

**_I set the remote radio address to MY=1234 in X-CTU, but I get a NO\_ACK response when sending a TX16 packet using "XBeeAddress16(12, 34)".  I've configured everything correctly and verified the radios are associated.  What's wrong?_

The unit for "MY" and nearly all XBee parameters is hexadecimal, so you would need to prepend the address with hexadecimal notation ("0x"), like so: "XBeeAddress16(0x12, 0x34)".**

**_I'm getting a timeout exception when sending a simple AT command (e.g. xbee.sendAtCommand(new AtCommand("MY")).  I know the COM port and baud rate are correct.  What's wrong?_

A timeout exception when sending an AT command almost always indicates a problem with the communication parameters.  Did you configure your radio in API mode, by setting AP=2 (This software does not support transparent mode.  See WhyApiMode)?  Are you connecting at the correct baud rate (default=9600) and port?**

_**My XBee address is 0x13 0xa2... but when I send a packet, I see 0x7d 0x33 0xa2... in the log.  What's going on here?**_

There are four special bytes that the software must escape prior to sending the packet to the radio: START\_BYTE (0x7e), ESCAPE (0x7d), XON (0x11) and XOFF (0x13).   Whenever one of these bytes is encountered, the byte is escaped by performing an XOR 0x20 and preceding it with the escape byte (0x7d).  So 0x13 becomes 0x7d 0x33.  Similarly, when the packet is received, any escaped bytes are un-escaped with another XOR 0x20 operation and the escape byte is discarded.

**_I copied my 64-bit address (SH+SL) correctly from X-CTU into my code but when I send packets to this address I get delivery failures. What is the problem?_

Make sure that when you use X-CTU, you check the "Use escape characters [ATAP=2]" checkbox on the PC Settings tab when using API mode (AP=2), which is required for this software. Otherwise all the data read from the radio may be incorrect. The problem occurs when the radio configuration contains special bytes (e.g. 0x13), which is common in places like the SH/SL address. When this occurs and the checkbox isn't checked, these values don't get unescaped and you see 0x7d, 0x33 instead of 0x13.**

**_I'm getting the following error when calling xbee.open(...): "java.lang.UnsatisfiedLinkError: librxtxSerial.jnilib:  no suitable image found, or Exception type: Bus Error (0xa) at pc=1072f4f74).  What's this about?_

The RXTX binary provided with this project is compiled in 32-bit mode but you are running a 64-bit version of Java.  To solve the problem you will need to pass in the "-d32" argument to force 32-bit mode. However, if you are using Oracle's Java 7 on the mac this won't work since they don't support 32-bit mode. Instead you can either use Java 6 or go to http://blog.iharder.net/2009/08/18/rxtx-java-6-and-librxtxserial-jnilib-on-intel-mac-os-x and download the 64-bit librxtxSerial.jnilib You'll also need to run sudo mkdir /var/lock; sudo chmod a+wrx /var/lock**

**Mac Users** Since the Yosemite release you may need to download Java 6 again from this link https://support.apple.com/kb/DL1572?locale=en_US

Afterwards, you should be able to find Java 6 in /System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/

Oracle Java is installed in /Library/Java/JavaVirtualMachines but DO NOT USE THESE!

If you are not running in Eclipse or the RXTX native library is located in a different directory than your application, the path to the native library must be specified as an argument to java, for example: java -Djava.library.path=/usr/lib/jni/ -classpath "**:/usr/share/java/RXTXcomm.jar" com.foo.MyApp**

_**Can I communicate with ZigBee hardware by other manufacturers (e.g. Texas Instruments' CC2480 Z-Stack)?**_

According to Digi, Series 2 XBee with the ZB Pro firmware is compatible with devices from other manufacturers that support the "ZigBee Feature Set" or "ZigBee PRO Feature Set".  Keep in mind that this software requires a Digi XBee (Series 2) connected to the serial port.  More information on [Digi XBee ZigBee](http://www.digi.com/products/wireless/zigbee-mesh/xbee-zb-module.jsp) and [TI Z-Stack](http://focus.ti.com/docs/toolsw/folders/print/z-stack.html) (Note: I have not had the chance to verify interoperability as I only have Digi XBee hardware)

_**I see a lot of these WARN messages in the log: "expected start byte but got this 0x00, discarding"**_

XBee packets start with a start byte (0x7e).  When the input stream does not start with a start byte, the software will discard bytes until it reads a start byte.  This condition is nearly always associated with running multiple XBee-API java processes on the same XBee radio.  Only one instance of XBee-API may be connected to the XBee's serial port.  You may also see this if you connect to an XBee radio that has been receiving packets.  When the UART is not being read, it will overflow the buffer once full.  To solve this problem you should connect to your XBee prior to receiving packets.  Another situation where you can see this message is if you mistakenly connected to a device other than an XBee radio, for example, to an Arduino.

_**What is the maximum payload size of an XBee Packet?**_

The maximum payload size depends on the radio type and firmware.  In general the maximum payload size is:

  * Series 1 XBee: 100 bytes
  * Series 2 XBee (ZNet Firmware): 72 bytes
  * Series 2 XBee (ZB Pro Firmware) 84 bytes

However, payload size can be reduced by certain configurations, including encryption.  The ZB Pro firmware provides the "NP" AT command to determine the radio's maximum payload size.

When a packet exceeds the the maximum payload size, the packet will not be sent and your only hint of failure will be that the XBee radio will not reply with a status response.  The exception to this is the ZB Pro firmware which will respond with PAYLOAD\_TOO\_LARGE.

Due to the various factors affecting maximum payload size, this software does not prevent you from sending a payload that is larger than the maximum, but you can specify the maximum size with the setMaxPayloadSize(int) method (available for ZNetTxRequest and TxRequest16/64).  If the payload size exceeds the user-defined maximum, an IllegalArgumentException will be thrown.

Note: Escaped bytes do not count towards the maximum payload size.
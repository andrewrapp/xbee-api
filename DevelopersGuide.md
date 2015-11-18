The [XBee](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/XBee.html) class is the primary interface for communicating with an XBee radio and a gateway to the wireless network. The XBee class coordinates sending and receiving data, via packets.  An instance of the XBee class is created as follows:

```
XBee xbee = new XBee();
```

This software communicates with XBee radios through serial communication, so you will need to connect an XBee radio (typically a coordinator) to your computer's serial port.  Since most computers don't have serial ports these days, a USB-serial adapter is a popular choice for interfacing with XBees.

The open() method must be called to establish a serial connection to the radio. The arguments are COM port and baud rate. The COM port depends on the operating system.
The default baud rate is 9600, unless it was changed it.  For example, this opens a connection to the XBee on the COM1 serial port at 9600 baud:

```
xbee.open("COM1", 9600);
```

The COM port varies depending on the operating system:

| **Operating System** | **How to Find COM Port** |
|:---------------------|:-------------------------|
| Windows              | Select Start->My Computer->Manage, then select Device Manager and expand "Ports". If you have more than one XBee attached you may need to unplug the others first.  |
| Mac OS               | The COM port appears under "/dev/tty.usbserial" (e.g /dev/tty.usbserial-A6005v5M). Open a terminal and run ls -l /dev/tty.u (hit tab twice to see all entries) |
| Linux                |  I found my COM port to be "/dev/ttyUSB0" on Ubuntu but I think it could easily be different for other distros. |

## API Mode ##

In API mode, we communicate with XBee radios by sending and receiving packets.  The types of packets that can be sent and received depend on the type of XBee radio you are using: Series 1 or Series 2, and the firmware version.  In general, there are two types of packets: Transmit and Receive.  Transmit packets are sent to the XBee radio, and Receive packets are received from the XBee radio.  All Transmit packets extend the [XBeeRequest](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/XBeeRequest.html) class, and similarly all Receive packets extend the [XBeeResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/XBeeResponse.html) class, either directly or through intermediate classes.  Each Transmit and Receive packet is identified by a unique API ID.  (In hindsight it would have been better to name the receive packet classes XxxReceive and transmit packets XxxTransmit, instead of XxxResponse and XxxRequest).

## Transmit Packets ##

| **Class** | **Description** | **Series** | **API ID Enum** |
|:----------|:----------------|:-----------|:----------------|
| [AtCommand](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/AtCommand.html) | For executing/querying AT commands a local XBee | Both       | [ApiId.AT\_COMMAND](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#AT_COMMAND) |
| [RemoteAtRequest](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/RemoteAtRequest.html) | For executing/querying AT commands on a remote XBee.  | Both (requires 10C8 firmware or later for Series 1) | [ApiId.REMOTE\_AT\_REQUEST](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#REMOTE_AT_REQUEST) |
| [TxRequest16](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/TxRequest16.html) | For sending data to a remote XBee, via a 16-bit Address | Series 1   | [ApiId.TX\_REQUEST\_16](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#TX_REQUEST_16) |
| [TxRequest64](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/TxRequest64.html) | For sending data to a remote Series 1 XBee, via a 64-bit Address | Series 1   | [ApiId.TX\_REQUEST\_64](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#TX_REQUEST_64) |
| [ZNetTxRequest](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/zigbee/ZNetTxRequest.html) | For sending data to a remote Series 2 XBee | Series 2   | [ApiId.ZNET\_TX\_REQUEST](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#ZNET_TX_REQUEST) |

## Receive Packets ##

| **Class** | **Description** | **Series** | **API ID Enum** |
|:----------|:----------------|:-----------|:----------------|
| [AtCommandResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/AtCommandResponse.html) | Sent in response to an AtCommand and indicates if the command was successful.  If the command was a query, it will contain the configuration value | Both       | [ApiId.AT\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#AT_RESPONSE) |
| [ModemStatusResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ModemStatusResponse.html) | Sent by the local XBee on certains events, such as Association, Disassociation | Both       | [ApiId.MODEM\_STATUS\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#MODEM_STATUS_RESPONSE) |
| [RemoteAtResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/RemoteAtResponse.html) | Sent in response to a RemoteAtRequest | Both (requires 10C8 firmware or later for Series 1) | [ApiId.REMOTE\_AT\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#REMOTE_AT_REQUEST) |
| [TxStatusResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/TxStatusResponse.html) | Sent after a TxRequest16 or TxRequest64 packet.  Indicates if transmission was successful | Series 1   | [ApiId.TX\_STATUS\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#TX_STATUS_RESPONSE) |
| [RxResponse16](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/RxResponse16.html) | Received on the remote radio after a TxRequest16 packet is sent | Series 1   | [ApiId.RX\_16\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#RX_16_RESPONSE) |
| [RxResponse64](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/RxResponse64.html) | Received on the remote radio after a TxRequest64 packet is sent | Series 1   | [ApiId.RX\_64\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#RX_64_RESPONSE) |
| [RxResponseIoSample](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/wpan/RxResponseIoSample.html) | Sent by a remote XBee, configured to send I/O Samples.  Note: getSourceAddress() returns either a XBeeAddress16 or XBeeAddress64 depending on if the packet is configured for 16 or 64 bit addressing  | Series 1   | Either [ApiId.RX\_16\_IO\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#RX_16_IO_RESPONSE) or [ApiId.RX\_64\_IO\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#RX_64_IO_RESPONSE) (depending on 16 or 64 bit address configuration) |
| [ZNetTxStatusResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/zigbee/ZNetTxStatusResponse.html) |  Sent after a ZNetTxRequest packet.  Indicates if transmission was successful | Series 2   | [ApiId.ZNET\_TX\_STATUS\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#ZNET_TX_STATUS_RESPONSE) |
| [ZNetRxResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/zigbee/ZNetRxResponse.html) | Received on the remote radio after a ZNetTxRequest packet is sent | Series 2   | [ApiId.ZNET\_RX\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#ZNET_RX_RESPONSE) |
| [ZNetExplicitRxResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/zigbee/ZNetExplicitRxResponse.html) | Received on the remote radio a ZNetExplicitTxRequest packet is sent |Series 2    | [ApiId.ZNET\_EXPLICIT\_RX\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#ZNET_EXPLICIT_RX_RESPONSE) |
| [ZNetRxIoSampleResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/zigbee/ZNetRxIoSampleResponse.html) |  Sent by a remote XBee, configured to send I/O Samples | Series 2   | [ApiId.ZNET\_IO\_SAMPLE\_RESPONSE](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/ApiId.html#ZNET_IO_SAMPLE_RESPONSE) |

## Sending Packets ##

The XBee class provides two methods for sending packets: sendAsynchronous and sendSynchronous.  As its name suggests, the sendAsynchronous method sends packets asynchronously.  This means that the method returns immediately, after sending the packet to the XBee over the serial line, but does not return anything.  The sendAsynchronous is capable of sending any class that extends XBeeRequest).  Here's an example of sending a Node Timeout query:

```
xbee.sendAsynchronous(new AtCommand("NT"));
```

As an alternative, the [sendSynchronous](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/XBee.html#sendSynchronous(com.rapplogic.xbee.api.XBeeRequest,%20int)) method sends the packet but then waits for a response, up to the timeout.

```
try {
    // send a request and wait up to 10 seconds for the response
    XBeeResponse response = xbee.sendSynchronous(new AtCommand("NT"), 10*1000);
} catch (XBeeTimeoutException e) {
    // no response was received in the allotted time
}
```

The sendSynchronous method matches a response to the request with the frame id.  For that reason is a good idea to chose a unique frame id when using this method.  Additionally this method is only allowed for requests that return a response.  As a validation, the method checks the frame id of the request.  If the frame id is not greater than zero, the request will not return a response and an exception is thrown.

## Receiving Packets ##

There are two methods for receiving packets: getResponse() and the PacketListener.  The getResponse() method is only recomended for simple, single-threaded apps, while the PacketListener is suitable for more complex multi-threaded applications.

The following code receives packets with the getResponse() method.

```
while (true) {
    XBeeResponse response = xbee.getResponse();
    // now do something with the response
}
```

Keep in mind that s, as with our next example.

The method call to getResponse() will block (wait) indefinitely until a response is received.  For this reason, it is sometimes a good idea to use a timeout because if a packet is never received, you would be waiting forever. For example, some requests, such as Remote AT, do not return a response if the remote radio is off or out of range.  This example will wait for up to 10 seconds for a response (multiple seconds x 1000 to get milliseconds).  If a response is not received a XBeeTimeoutException will be thrown.

```
while (true) {
    try {
        XBeeResponse response = xbee.getResponse(10000);
        // we got a response!
    } catch (XBeeTimeoutException e) {
        // we timed out without a response
    }
}
```

The PacketListener is added by calling the addPacketListener() method with a class that implements the PacketListener interface:

```
xbee.addPacketListener(new PacketListener() {
    public void processResponse(XBeeResponse response) {
        // handle the response
    }
});
```

// main thread continues on here!

Because the PacketListener is invoked by a separate thread, your code will not wait/block on the addPacketListener(), or processResponse() methods.


## Casting the Response ##

All methods for receiving packets return an instance of [XBeeResponse](http://xbee-api.googlecode.com/svn/trunk/docs/api/com/rapplogic/xbee/api/XBeeResponse.html), the super class of all Receive packets.  This class must be "cast" into the appropriate subclass in order to access the packet specific data.  To do this you can check the API ID to determine the appropriate class:

```
xbee.sendAsynchronous(new AtCommand("NT"));

XBeeResponse response = xbee.getResponse();

if (response.getApiId() == ApiId.AT_RESPONSE) {
   // since this API ID is AT_RESPONSE, we know to cast to AtCommandResponse
    AtCommandResponse atResponse = (AtCommandResponse) response;

    if (atResponse.isOk()) {
        // command was successful
        System.out.println("Command returned " + ByteUtils.toBase16(atResponse.getValue()));
    } else {
        // command failed!
    }
}
```

## Packet Delivery Acknowledgement (ACK) ##

XBees provide a delivery confirmation, or ACK feature, that indicates if a packet was received by a remote XBee.  This feature only works with unicast packets, not broadcast.  To enable ACK, you must specify a frame ID greater than zero, and between 1 and 255, inclusive.  By default, this software uses a frame id of 1.  Secondly you must specify the packet as unicast (default setting).

Here's an example of sending a Series 1 packet and receiving the ACK:

```
// create a unicast packet to be delivered to remote radio with 16-bit address: B071, with payload "Hi"
TxRequest16 request = new TxRequest16(new XBeeAddress16(0xb0, 0x71), new int[] {'H','i'});

// send the packet and wait up to 12 seconds for the transmit status reply (you should really catch a timeout exception)
TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronous(request, 12000);

if (response.isSuccess()) {
	// packet was delivered successfully
} else {
	// packet was not delivered
	System.out.println("Packet was not delivered.  status: " + response.getStatus());
}
```

Note: While receiving a successful ACK response is a guarantee the packet was delivered, not receiving a successful ACK does not always indicate the remote radio did not receive the packet.  There is a possibility that the remote radio received the packet, but the ACK response to the originating radio was not successful.  This situation is unlikely however is more likely to occur when the radio is operating at the edge of its range.  It can also occur if you are transmitting from a XBee Pro and the receiving radio is a less powerful non-Pro XBee.

## AT Command ##

The AT command allows you to query and set  the configuration of the XBee connected to your serial port. AT commands are defined by two characters and are supported by both series 1 and series 2 XBees .  You can query the value of a command by sending the command without a value.  For example:

```
// query the Serial Low Address
AtCommand  at = new AtCommand("SL")
```

To set the value of a command, specify the value as the second argument.  The value must be either an int:

```
// set D2 digital input
AtCommand  at = new AtCommand("D2", 3);
```

or int[.md](.md) (array):

```
// set PAN ID:
AtCommand  at = new AtCommand("ID", new int[] {0x1a, 0xaa}));
```

To execute the command and get a response, send the request to the radio:

```
// I choose 5 seconds as an arbitrary value for the timeout.  AT commands usually respond very quickly.
AtCommandResponse response = (AtCommandResponse) xbee.sendSynchronous(command, 5000);  
```

The isOK() method will return true if the command was successful.

```
if (response.isOk()) {
// success
}
```

For query commands, the value of the command can be accessed by calling the getValue() method.   This method returns an int[.md](.md) (array), so you will need to know the size of the response.

In the case of the "CH" (Channel) command, the value is 1 byte.  Here's how to query the current channel.

```
AtCommand  at = new AtCommand("CH");

AtCommandResponse response = (AtCommandResponse) xbee.sendSynchronous(at, 5000);

if (response.isOk()) {
	// success
	System.out.println("The channel is " + response.getValue()[0]);
}
```

Refer to the manual for the complete listting of commands that apply to your radio/firmware.  Keep in mind that some commands are read-only, and some represent actions, such as FR (software reset), and WR (write).

## Remote AT ##

The Remote AT command allows you to send an AT Command to a remote radio; where "remote" means a radio that has joined the network (same PAN ID and channel) of the serially connected XBee.

The Remote AT command is identical to the AT Command, except for three additional parameters: 64-bit address of remote radio, 16-bit address of the remote radio and Apply Changes.  When setting AT commands, the changes do not become effective until either the Apply Changes parameter is set to true, or the AC (Apply Changes) or WR (write) and FR (reboot) commands are sent.

Originally, the Remote AT packet was only supported by Series 2 radios, but recently it has been supported by series 1 XBees, starting with firmware 10c8.

### Series 1 ###

To send a Remote AT command to a series 1 radio (10c8 or higher firmware), you must either specify the 64-bit address (SH + SL), or the 16-bit address (MY).  If you use the 64-bit address, you must set the 16-bit address to the broadcast address.  For example:

```
// this is the SH + SL address of the remote radio
XBeeAddress64 remoteAddress = new XBeeAddress64(0, 0, 0, 0, 0, 0, 0, 0);
RemoteAtRequest request = new RemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, remoteAddress, XBeeAddress16.BROADCAST, false, "NI");
```

To address the remote radio with the 16-bit address, use the 64-bit broadcast address:

```
// this is the MY address of the remote radio
XBeeAddress16 remoteAddress = new XBeeAddress16(0xaa, 0xbb);
RemoteAtRequest request = new RemoteAtRequest(XBeeRequest.DEFAULT_FRAME_ID, XBeeAddress64.BROADCAST, remoteAddress, false, "NI");
```

### Series 2 ###

For series 2 radios, the MY address is read-only, as it is determined by the radio, so you only need to provide the 64-bit address of the remote radio.

```
// this is the SH + SL address of the remote radio
XBeeAddress64 remoteAddress = new XBeeAddress64(0, 0, 0, 0, 0, 0, 0, 0); 
RemoteAtRequest request = new RemoteAtRequest(remoteAddress, "NI");
```

## I/O Samples ##

I/O Samples or I/O line monitoring is a feature of XBee radios for receiving I/O data from a remote XBee.  Enabling this feature requires a few configuration changes on the remote radio.

First specify the sample rate, in milliseconds.  This is a two byte value.  For example:

```
IR=ffff
```

This sets the sample rate to the maximum of 1 sample every ffff milliseconds, or 65535/1000 -> 65 seconds.  Note: when using X-CTU for configuration, the "0x" must not be used for hexadecimal numbers, but it is required in Java.

For series 1 radios only, you can specify the number of I/O readings per packet with the IT command.  Series 2 radios always send 1 sample per packet.

```
IT=5
```

This value affects the sample rate, so multiple IT\*IR to get the effective sample rate. In this case since IR is 65 seconds, we would receive a sample every 65\*5 ->325 seconds.

Now enable an I/O pin:

```
D0=2
```

This configures D0 (pin 20) as an analog input.  Pins may also be configured for digital input.  Refer to this [wiki](http://code.google.com/p/xbee-api/wiki/XBeePins) for available analog and digital pins for your radio

```
D4=3
```

This sets D4 (pin 11) as a digital input.


Series 2 radios will automatically send I/O sample data to the coordinator but series 1 radios require you to specify the address of the coordinator.  If you are using 16-bit address, set DL to the coordinator's MY address. For example:

```
DL=1874 
```

For 64-bit addressing, set DL to the coordinator's SL address, and DH to the coordinator's SH address.

Remember to send the WR command to save the configuration, if you want it to remain after the device is powered off.

Now your coordinator should start receiving I/O packets.  For series 1, you receives samples as follows:

```
XBeeResponse response = xbee.getResponse();

if (response.getApiId() == ApiId.RX_16_IO_RESPONSE || response.getApiId() == ApiId.RX_64_RESPONSE) {
	RxResponseIoSample ioSample = (RxResponseIoSample)response;
	
	System.out.println("Received a sample from " + ioSample.getSourceAddress());
	System.out.println("RSSI is " + ioSample.getRssi());
	
	// loops IT times
	for (IoSample sample: ioSample.getSamples()) {		
		System.out.println("Analog D0 (pin 20) 10-bit reading is " + sample.getAnalog0());
		System.out.println("Digital D4 (pin 11) is " + (sample.isD4On() ? "on" : "off"));
	}
}
```

For series 2:

```
XBeeResponse response = xbee.getResponse();

if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
	ZNetRxIoSampleResponse ioSample = (ZNetRxIoSampleResponse) response;
	
	System.out.println("Received a sample from " + ioSample.getRemoteAddress64());

	System.out.println("Analog D0 (pin 20) 10-bit reading is " + ioSample.getAnalog0());
	System.out.println("Digital D4 (pin 11) is " + (ioSample.isD4On() ? "on" : "off"));
}
```


Change Detect is a feature for sending I/O data when a digital input changes.  Change Detect is enabled with the IC command.  The value of this command is a bitmask that corresponds to Digital inputs 0-n.  For example, to enable Change Detect for inputs D3 and D4, set IC to 2`^`3 + 2`^`4 = 24 (or 0x18).

```
IC=18
```

The XBee will now send a I/O sample packet when either D3 (pin 17) or D4 (pin 11) go to 0V.  If you are also receiving analog samples, you may want to distinguish between a periodic sample and a change detect sample.  Unfortunately the packet does not contain a change detect indicator, however if you have analog inputs enabled, you can call "containsAnalog()":

```
if (!ioSample.containsAnalog()) {
// this is a change detect packet
}
```

If the packet doesn't contain analog, and analog is enabled, you know this must be a Change Detect packet.  So enable at least one analog pin if you want to use this method.


## Force Sample ##

An alternative to periodic I/O samples is to request a I/O sample, using the "Force Sample" command.

[TODO](TODO.md)

## ZDO Commands ##

TODO

## Package Structure ##

The library is organized into the following packages:

| **Package** | **Description** |
|:------------|:----------------|
| com.rapplogic.xbee | Contains classes that are common to all XBee radios |
| com.rapplogic.xbee.api | Contains classes that are common to all XBee radios, configured in API mode |
| com.rapplogic.xbee.wpan | Contains classes that are common to Series 1 XBee radios. The "wpan" designation refers to Wireless Personal Area Network, the IEEE 802.15.4 standard for Series 1 XBee. |
| com.rapplogic.xbee.zigbee | Contains classes that are common to Series 2 XBee radios. This refers to ZigBee radios with either ZNet 2.5 or ZB Pro firmware. |
| com.rapplogic.xbee.examples | Contains examples of using the API |

Note on class naming:  The ZNetXxx classes are applicable to Series 2 radios.  They were naming prior to the existing of the ZB Pro firmware, and renaming them now would break existing code, although I may need to do this at some point in the future.

## Logging ##

Logging is provided by the [Log4J](http://logging.apache.org/log4j/1.2/index.html) library. The log settings are defined in "log4j.properties", located in the project root folder. The default log configuration sends log messages to a file (XBee.log) and the console. If you are using Eclipse, you will see log messages in the "Console" window.

You can alter the amount of logging by changing the log levels. For example, to see more log messages, set the log level to INFO:

log4j.logger.com.rapplogic.xbee=INFO, main-appender, console-appender

THE INFO level will show all log messages at INFO, WARN, ERROR and FATAL levels. At this level. all packet data sent and received from the radio will be logged.

You can set the log level to DEBUG to see even more log messages but this is not recommended unless you are troubleshooting a problem.

Log4j is heirarchical, so that the following configuration

log4j.logger.com.rapplogic.xbee=WARN, main-appender, console-appender

will log all statements in com.rapplogic.xbee. packages; this includes the entire library. You can override the default log setting for any package, or class. For example, this line sets all classes under the xbee/examples to the DEBUG log level.

log4j.logger.com.rapplogic.xbee.examples=DEBUG

The RXTX library (serial communication) is a native C application (not Java) and therefore does not log to Log4j.  Instead, it logs messages to the console.

You may add your custom log messages by.... log.debug(..)

When you log an object, (e.g. log.debug(response)), it calls the toString() method of that object.  All request/response classes have implemented the toString method to output the contents of the object. This can be especially helpful when developing/testing.
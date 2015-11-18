# Release Notes #

Version 0.9 (11/7/10)

This release includes the following updates

  * Added protocol independent XBeeConnection interface.  This allows developers to develop support for communicating with the radio over protocols other than serial (e.g. socket, xmpp).  The implementation overrides the providerConnection method.
  * PacketListener is now handled through a thread pool.
  * Added ResponseQueueFilter to determine which packet types will be added to the response queue.
  * Created NoRequestResponse interface for tagging XBeeResponse classes that are send by the radio/network w/o a corresponding request.
  * Created XBeeConfiguration class for specifying config information
  * XBee no longer extends RXTX.  Additionally, you can call open and close methods to your hearts content without issues
  * Removed the ZNET prefix from ZNET\_REMOTE\_AT\_REQUEST as it is present in Series 1 now.  Same with ZNET\_REMOTE\_AT\_RESPONSE
  * Fixed bug with Series 1 NodeDiscover example
  * ZB I/O Samples: Added parseIsSample(AtCommandResponse response) for parsing an I/O sample via RemoteAT.  Added Boolean isDigitalOn(int pin),  Integer getAnalog(int pin), boolean isDigitalEnabled(int pin), and boolean isAnalogEnabled(int pin) for dynamic access to digital and analog pins
  * WPAN I/O Samples: Added parse(IIntArrayInputStream ps), Added Integer getAnalog(int pin) , Boolean isDigitalOn(int pin), boolean isDigitalEnabled(int pin), and boolean isAnalogEnabled(int pin) for dynamic access to digital and analog pins
  * Removed getLastResponse(), getResponseBlocking(int), getResponseBlocking(), waitForResponse(int), and waitForResponse() methods from XBee class. Use getResponse and PacketListener interface instead.
  * Added GenericResponse.  If an unknown packet is encountered, it will be parsed into a Generic Response and the packet data may be accessed. Previously an exception was thrown if an unknown packet was read
  * ZNetRemoteAtRequest was renamed to RemoteAtRequest; ZNetRemoteAtResponse renamed to RemoteAtResponse
  * Added a startup heuristic to determine radio type and if it's configured correctly
  * getResponse now uses a BlockingQueue.
  * added clearResponseQueue method to XBee class
  * added equals and hashCode implementation to many classes
  * added getRawPacketBytes() and getProcessedPacketBytes() methods to XBeeResponse
  * iosamples getAnalog(pin)
  * renamed       DEFAULT\_OPTION (0),DISABLE\_ACK\_OPTION (1),BROADCAST\_OPTION(4); to UNICAST (0),DISABLE\_ACK (1),BROADCAST(4);
  * removed       public boolean isError() from TxStatusResponse.  use !isSuccess() instead
  * added getAnalog(int pin) and isDigitalOn(int pin) methods to ZNetRxIoSampleResponse
  * ZNetTxRequest now has Option enum instead of int
  * added BroadcastSender/Receiver examples

Version 0.5.5 (5/10/09)

This release includes the following updates

  * Maximum payload size is no longer enforced for both series 1 and 2 XBees.  The reason for this change is the max size varies depending on the firmware.  For example ZNet is 72 bytes but ZB Pro is up to 84 bytes and less based on certain configurations such as encryption.   A new method (setMaxPayloadSize) was added to allow the user to define a max size.  An exception will be thrown during send if the payload size exceeds the user defined limit.
  * Renamed TX\_16\_STATUS\_RESPONSE to TX\_STATUS\_RESPONSE in ApiId to eliminate potential confusion as it also applies to 64 bit addressing
  * Added NodeDiscover class for Series 1 XBees
  * Added example of using NodeDiscover for series 2: NodeDiscoverTest.java
  * Added isSuccess() method to TxStatusResponse (series 1) and ZNetTxStatusResponse (series 2)
  * Added NO\_RESPONSE (4) status response enum that is new in series 1 10C8 firmware
  * Renamed MAX\_PAYLOAD\_SIZE to ZNET\_MAX\_PAYLOAD\_SIZE since it doesn't apply to ZB Pro firmware
  * Added ZbForceSample class.  Going forward I'll use the generic ZB (ZigBee) prefix instead of ZNet.  Except where noted, both ZNet and ZB Pro firmware apply to all ZNet and ZB classes
  * Fixed bug with node id.  Thanks to Nerio Montoya for reporting
  * Added INFO log level for packets going in/out of the serial.  this will make it easier to analyze traffic w/o the verbosity of the debug level
  * Added equals implementation for 64-bit address to resolve RuntimeException issue
  * XBee now stops parsing packets when serial cable is disconnected.  Previously RXTX would go bonkers and fill up your hard drive with log errors
  * Added isAddressBroadcast and isPanBroadcast methods for series 1 RX
  * Added new ZB PRO specific status to tx status response class
  * Replaced toChar with toString in byte utils
  * Added ZB Pro manual and updated other manuals.
  * Modified ZNetRemoteAtRequest to subclass AtCommand and ZNetRemoteAtResponse to subclass AtCommandResponse.   To perform this change it was necessary to change the return type of the getStatus method in AtCommandResponse from int to Status (enum).  If you are using the isOk() method then no change is required.
  * Created interface IXBee.java for XBee.java
  * Added all pins to XBeePins.java
  * Added abbreviated contructors for TX classes
  * Added isConnected method to XBee class
  * Added ZBForceSampleRequest class and corresponding example
  * Cleaned up examples
  * Added WPAN I/O Samples example
  * Added ZNet/ZB Pro I/O Samples example
  * Removed redundant synchronous send example

Version 0.5.1 (1/25/09)

This release fixes the following:

  * Source address on series 1 RX 64 bit packets was incorrectly set
  * Stackoverflow bug with the series 1 isD7On method.

See revisions 90-93 for line-by-line differences: http://code.google.com/p/xbee-api/source/list

Version 0.5 (1/17/09)

This release includes:

  * Support for Explicit TX/RX packets (0x11 and 0x91).  I have also included sample classes that demonstrate sending/receiving Explicit packets.
  * Added a PacketListener interface to receive packets as they are parsed
  * JavaDoc is now included in the download and is also a task in the Ant build.xml
  * Added toString() method for I/O samples
  * Updated I/O classes to return Integer (analog) and Boolean (digital) instead of a primitive types in order to distinguish between enabled and disabled I/0 pins.
  * Rewrote the synchronousSend method in the XBee class and added an example
  * Improved several of the examples and added ZNetApiAtTest class.  Hopefully they are more clear now.
  * Improved documentation (JavaDoc)

You can see full details of each update involved in this release in the updates tab http://code.google.com/p/xbee-api/updates/list (revisions 74-86)

In this release, XBeeRequest/XBeeResponse API constants have been changed from primitive int to the Enum type and consolidated in a single Enum. This will require changes in your code if you access the previous api constants.  So instead of response.getApiId() == XBeeResponse.ZNET\_RX\_RESPONSE you would do response.getApiId() == ApiId.ZNET\_RX\_RESPONSE

Version 0.4 (8/3/08)

This release includes a bug fix for receiving ACK errors (series 1 only) and improved error handling.  See http://code.google.com/p/xbee-api/source/detail?r=65 for more details about this release.

Version 0.3.1 (6/10/08)

This release contains a bug fix for receiving 16-bit I/O samples with 802.15.4 (series 1) XBees (apparently I broke it when adding ZNet support).

Version 0.3 (4/29/08)

This release includes support for XBee ZNet (ZigBee) radios.  Aside from mesh networking, one of the most interesting features of the ZNet radios is Remote AT.  The Remote AT feature allows you to send AT commands to a remote XBee (e.g. turn on/off outputs, request a sample etc.).  Also in this release I have tested the API on the Mac.

Version 0.2 (4/5/08)

First public release.  Yay!
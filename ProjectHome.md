## Overview ##

This is a Java API for communication with XBee/XBee-Pro series 1 (802.15.4) and series 2 (ZB/ZigBee Pro) OEM RF Modules, in API mode.  The objective of this project is to provide a flexible and simple to use API to interact with XBee radios.  In terms of flexibility, the goal is to not constrain you to a particular implementation (e.g. GUI, Web App, Processing etc.), but instead let you decide how to use it.  This software has been tested on Windows, Mac, and Linux and can run on any other platform that supports Java 5 or greater and RXTX, including the [Rasberry Pi](http://www.amazon.com/gp/product/B009SQQF9C/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B009SQQF9C&linkCode=as2&tag=xbapra-20)!

**Note: This software requires API mode, by setting  AP=2. If you are using Series 2 XBee, you'll need to install API Firmware (coordinator, router or endpoint) with X-CTU (windows only) since they ship with AT firmware.  This software will not work correctly with AP=1**  Refer to [XBeeConfiguration](XBeeConfiguration.md) and WhyApiMode for more info.

## News ##
  * 2/28/15 The code is now on [github](https://github.com/andrewrapp/xbee-api). I will attempt to keep this repo up to date as well but I recommend cloning or downloading from github. The documentation will stay here for now.
  * 2/19/15 I've created [xbee-socket](https://github.com/andrewrapp/xbee-socket) and [xbee-serial-server](https://github.com/andrewrapp/xbee-serial-server) projects for sharing a single xbee radio with multiple applications, over sockets. This is very beta. Also, [this](https://github.com/andrewrapp/xbee-api) code is now on github.
  * 2/1/14 Converted repository from Subversion to Git
  * 6/11/13 Yeah, you can use [xbee-api with your Raspberry Pi](http://rapplogic.blogspot.com/2013/06/xbee-on-raspberry-pi.html)
  * 4/3/11 I have created a [XBeeUseCases](http://code.google.com/p/xbee-api/wiki/XBeeUseCases) wiki that describes several use cases for communicating with XBees.
  * 11/7/10 The 0.9 release is now available!  This release includes a lot of new features and an extensive redesign of the internals.  See ReleaseNotes for details.
  * 8/13/10 I've added a [wiki](Processing.md) that describes how to use XBee-API with Processing
  * 3/29/09 I have released [XBee Arduino](http://code.google.com/p/xbee-arduino/).  This is a port of XBee API to the Arduino platform, allowing you to send/receive packets on Arduino.
  * 4/5/08 Initial Release

## Documentation ##

  * [Developer's Guide](DevelopersGuide.md)
  * [Javadocs](http://xbee-api.googlecode.com/svn/trunk/docs/api/index.html)
  * [Wiki Docs](http://code.google.com/p/xbee-api/w/list)
  * [Digi XBee Series 1/802.15.4/S1](http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/point-multipoint-rfmodules/xbee-series1-module#docs) [(Product Manual)](http://ftp1.digi.com/support/documentation/90000982_S.pdf)
  * [Digi XBee Series 2/ZB/S2](http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/zigbee-mesh-module/xbee-zb-module#docs) [(Product Manual)](http://ftp1.digi.com/support/documentation/90000976_V.pdf)

## Learning/Books ##

  * [Wireless Sensor Networks: with ZigBee, XBee, Arduino, and Processing](http://www.amazon.com/gp/product/0596807732?ie=UTF8&tag=xbapra-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=0596807732Building) (Kindle version available)
  * [Making Things Talk: Using Sensors, Networks, and Arduino to see, hear, and feel your world](http://www.amazon.com/gp/product/1449392431/ref=as_li_ss_tl?ie=UTF8&camp=1789&creative=390957&creativeASIN=1449392431&linkCode=as2&tag=xbapra-20)

<a />

## Examples ##

Here are a few examples that demostrate some common functionality:

Turn on an I/O port on a remote Series 2 (ZNet/ZB Pro) XBee:

```
XBee xbee = new XBee();
xbee.open("COM5", 9600);

// this is the Serial High (SH) + Serial Low (SL) of the remote XBee			
XBeeAddress64 addr64 = new XBeeAddress64(0xa, 0xb, 0xc, 0xd, 0xe, 0xf, 0, 1);

// Turn on DIO0 (Pin 20)
RemoteAtRequest request = new RemoteAtRequest(addr64, "D0", new int[{XBeePin.Capability.DIGITAL_OUTPUT_HIGH.getValue()});

xbee.sendAsynchronous(request);

RemoteAtResponse response = (RemoteAtResponse) xbee.getResponse();

if (response.isOk()) {
    System.out.println("Successfully turned on DIO0");
} else {
    System.out.println("Attempt to turn on DIO0 failed.  Status: " + response.getStatus());
}

// shutdown the serial port and associated threads
xbee.close();

```

Receive I/O samples from a remote Series 1 (802.15.4) XBee:

```
XBee xbee = new XBee();		
xbee.open("COM15", 9600);
			
while (true) {
    RxResponseIoSample ioSample = (RxResponseIoSample) xbee.getResponse();

    System.out.println("We received a sample from " + ioSample.getSourceAddress());	
			
    if (ioSample.containsAnalog()) {
        System.out.println("10-bit temp reading (pin 19) is " + ioSample.getSamples()[0].getAnalog1();
    }
}
```

There are many more detailed examples included in the code for both ZigBee and 802.15.4.  Here are just a few:

This example contains a few sample configurations to get your ZigBee coordinator and end devices configured quickly, without needing X-CTU (windows only) [ZNetApiAtExample.java](http://code.google.com/p/xbee-api/source/browse/trunk/src/com/rapplogic/xbee/examples/zigbee/ZNetApiAtExample.java)

Example of sending packets from a ZigBee coordinator to an end device [ZNetSenderExample.java](http://code.google.com/p/xbee-api/source/browse/trunk/src/com/rapplogic/xbee/examples/zigbee/ZNetSenderExample.java)

This example receives the packets sent in the previous example [ZNetReceiverExample.java](http://code.google.com/p/xbee-api/source/browse/trunk/src/com/rapplogic/xbee/examples/zigbee/ZNetReceiverExample.java)

## Feature Support ##

The majority of the XBee specification has been implemented for both 802.15.4 (Series 1) and ZNet 2.5/ZB Pro (Series 2).  See FeatureSupport for a full listing of supported API types.

## About ##

I received my first pair of XBee Pro radios in Nov '07, after learning about them on the Arduino website.  I quickly learned that to take advantage of the real power in XBee radios (e.g. I/O Samples, ACK/Delivery Status, Remote AT etc.), you need to configure the radio in API mode.  I searched for open source XBee software with support for API mode, but couldn't find anything, so I decided to write my own.  I started coding on 12/15/2007 and after about 300 commits and several months later I released the software here, on Google Code.

## Support ##

Please report any bugs on the [Issue Tracker](http://code.google.com/p/xbee-api/issues/list)  For questions regarding this software, not covered in the documentation, use the [Forum](http://groups.google.com/group/xbee-api?pli=1)


## Questions/Feedback ##

Questions about this project should be posted to  http://groups.google.com/group/xbee-api?pli=1  Be sure to provide as much detail as possible (e.g. what radios s1 or s2, firmware versions, configuration and code).

## Consulting/Commercial Licenses ##

I'm available for consulting to assist businesses or entrepreneurs that need help getting their projects up and running. I can also provide a commercial license for situations where you need to distribute code to clients/third parties that would otherwise conflict with GPL. For these matters I can be contacted at andrew.rapp `[`at`]` gmail.
# Getting Started #

This software communicates with XBee radios through the Java RXTX serial library, so it will run on any computer with Java and RXTX support. This includes Mac, Windows, Linux and also the Raspberry Pi!. If you are looking for an XBee library that runs on a microcontroller, xbee-arduino, is a port of XBee API for the Arduino.

To get started we'll need two XBee radios. We'll also need a usb-serial device to connect the XBee to the computer. In this guide we'll connect two XBee radios to the computer to demonstrate transmitting and receiving packets. See the [Hardware](Hardware.md) for information and links to suppliers.

Once you have your hardware, place the XBee into the usb-serial socket and plug into the computer's usb port.

Most all usb-serial devices use the FTDI chip for usb-to-serial conversion. Mac and Linux should not require drivers but my experience is that Windows always requires the driver installation from FTDI. Drivers are available from FTDI [here](http://www.ftdichip.com/FTDrivers.htm). Note: if you have installed Arduino and use it with an older Arduino (Arduino Duemilanove and earlier), you should already have the drivers as these older Arduinos use FTDI chips.

Finding the serial port depends on the operating system:

**Mac**: It should appear under /dev/tty.usbserial-[char identifier](8.md) For example /dev/tty.usbserial-A60055uRz]. In a terminal, execute "ls -l /dev/ | grep tty.usbserial". If you don't see a device, install the drivers from FTDI.

**Linux**: Similar to Mac, the device will appear under /dev, however on Linux it will be named. /dev/USB[number](number.md), for example /dev/USB0. If you have multiple serial devices they will be named /dev/USB1, /dev/USB2 and so on. In a terminal execute "ls -l /dev/ | grep USB" to see if your radio is present.

**Windows**: Select My Computer->Manage, then select Device Manager and expand "Ports". It should appear as COM[number](number.md), e.g. COM1. If you see multiple ports, unplug the device check, then plug in to see which port was added (this process works for Windows XP. I don't have Vista or latest version of Windows. Please let me know if this has changed).

Note: if you have an older Arduino plugged in you may see duplicate ports since it uses the same FTDI chip.

### Configuration ###

Before we can use this software, the XBee radios must have the correct configuration to work with this software. Refer to the [XBeeConfiguration](XBeeConfiguration.md) wiki for necessary configuration.

Series 1: Refer to the [XBeeConfiguration](XBeeConfiguration.md) to apply the minimal configuration. Configuration can be applied with the X-CTU application (recommended if you have Windows). It can also be applied with this software, using the AtCommand. See the [DevelopersGuide](DevelopersGuide.md) for more information

Series 2: these radios require the API firmware to use this xbee-api since they always come from Digi with AT firmware. Firmware updates requires X-CTU application, which unfortunately only runs on Windows. If you use Mac or Linux and don't have access to a Windows computer, I recommend using a Virtual Machine. I use Windows XP on VMWare Fusion to run X-CTU on my Mac. If using VMWare, go to Virtual Machine->Settings->USB Devices and click the checkbox next to the FTDI device. On Mac this appears as "Future Devices FT232R USB UART". Once this is checked, the serial port is available to Windows and will show up as USB Serial Port COM[number](number.md), eg. COM3. Start X-CTU and select the "Modem Configuration" tab. Click the "Read" button. After a few seconds you should see the radio's configuration appear. Click "Download new versions" to get the latest firmwares. This make take a few minutes. Under Modem, select XB24-ZB (for Series 2, non Pro). Under "Function Set" select "ZIGBEE ROUTER API". Under version select the latest version. I used 23A7. Now click the "Always update firmware" checkbox and click "Write". Make sure you don't unplug the radio while the firmware is written. After the firmware completes, go back to the "PC Settings" tab and check the "Enable API" checkbox. Now set the minimum configuration specified on [XBeeConfiguration](XBeeConfiguration.md) (PANID, and AP=2). Again, go back to
"PC Settings" tab and click the "Use escape characters (ATAP=2)" checkbox. Select "Modem Settings" and read the configuration and verify it is as expected.

Often it's a good idea to capture the 64-bit address of the radio save it somewhere while you have the radio hooked up to X-CTU. The 64-bit address is comprised of the SH and SL config values. It might look something like

SH: 13A200
SL: 400A3E02

Two things are very important here:

1. make sure that the "Enable API" and "Use escape characters (ATAP=2)" checkboxes are check IF you have applied API firmware (series 2 only) and have set AP=2 (both series 1 and 2).

2. It's important to understand that X-CTU config values are in hexidecimal, so in Java or C/C++ the number must be represented with hex notation. For example, 13 is represented as 0x13.

To create an address with the API, it will use an integer array since there is no unsigned 64-bit data type in Java. This looks like:

`XBeeAddress64 address = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);`

If you didn't get the address, you can still get it by running a Node Discover command. This will return all the radios on the network.

TODO Run examples with Eclipse

<a href='Hidden comment: 
You may find this library useful if you want to doing things like provide internet access to your XBee network (e.g. query XBee from internet), log your XBee data to a local file or database.

The recommended method for connecting the XBee to your computer is the XBee Explorer USB. This is essentially a USB-serial board with an XBee socket, providing power and communication to the radio. There are other variants available that perform the same function. The XBee Explorer USB should show up as a serial port on your computer. If not you will likely need to download and install FTDI drivers for your OS. Both the Arduino and XBee Explorer USB use the FTDI USB/Serial chip. FTDI enables a virtual serial port over USB and provides free drivers http://www.ftdichip.com/FTDrivers.htm for nearly all platforms (win/mac/linux etc.). If you have installed Arduino software and successfully communicated with Arduino, you already have the drivers.
'></a>

Read the DevelopersGuide,  [Javadoc](http://xbee-api.googlecode.com/svn/trunk/docs/api/index.html), [FAQ](FAQ.md) and the other [Wikis](http://code.google.com/p/xbee-api/w/list)

The XBee manuals are well written and I highly recommend reading them to understand how XBees operate:

  * [Digi XBee Series 1/802.15.4/S1](http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/point-multipoint-rfmodules/xbee-series1-module#docs) [(Product Manual)](http://ftp1.digi.com/support/documentation/90000982_L.pdf)
  * [Digi XBee Series 2/ZB/S2](http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/zigbee-mesh-module/xbee-zb-module#docs) [(Product Manual)](http://ftp1.digi.com/support/documentation/90000976_P.pdf)

Download the software try the example code (located in the com.rapplogic.xbee.examples packages).

Post your questions in the [Forum](http://groups.google.com/group/xbee-api)
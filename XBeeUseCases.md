# Introduction #

This guide describes several use cases for using this software and [XBee Arduino](http://code.google.com/p/xbee-arduino/) to communicate with XBee radios in API mode.

## PC to XBee (Stand-alone) ##

![http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/computer-to-stand-alone.png](http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/computer-to-stand-alone.png)

In this configuration we have an XBee connected to a computer through a serial connection, possibly by using a [SparkFun Explorer](http://www.sparkfun.com/products/8687). I'll refer to this XBee (on left) as the local XBee.  The XBee on the right side is the remote or stand-alone XBee. This XBee has only power and a few components (LED, accelerometer, and push button) connected to the XBee's I/O pins.  The XBee's I/O pins support digital input, digital output and analog input (refer to XBeePins for a listing of pin capabilities). The [SparkFun Explorer Regulated](http://www.sparkfun.com/products/9132) board is a good choice for interfacing with a remote XBee.  The [XBee API](http://code.google.com/p/xbee-api/) software is used to send commands via the local XBee to the remote XBee.

There are two possibilities for communicating with the remote XBee in this configuration: Remote AT and I/O samples.  The remote XBee is limited to I/O in this configuration; it can turn on/off digital outputs, read digital inputs and read analog inputs.  Refer to the DevelopersGuide and XBee documentation to learn about Remote AT and I/O Samples.

In the following code example we demonstrate the digital output capability by flashing the LED (which is connected to pin 20 on the XBee w/ a resistor).  The code issues a command to turn it on the LED, waits a few seconds, then turns it off.  This code sample is compatible with Series 1 and 2 XBees.

```
// replace with your coordinator com/baud
xbee.open("/dev/tty.usbserial-A6005v5M", 9600);

// replace with SH + SL of your end device
XBeeAddress64 address = new XBeeAddress64(0, 0x13, 0xa2, 0, 0x40, 0x0a, 0x3e, 0x02);

// pin 20 corresponds to D0, and 5 activates the output (Digital output high) 
RemoteAtRequest request = new RemoteAtRequest(address, "D0", new int[] {5});

// turn on LED
RemoteAtResponse response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);

if (response.isOk()) {
	// success
}

// pause for 2 seconds
Thread.sleep(2000);

// command to turn off pin 20
request = new RemoteAtRequest(address, "D0", new int[] {4});

// send the command
response = (RemoteAtResponse) xbee.sendSynchronous(request, 10000);

if (response.isOk()) {
	// success
	System.out.println("LED is off");	
}
```

Using I/O samples, we can periodically receive analog input readings of a component connected to the remote XBee, for example an analog accelerometer.  You will need to configure your radio for I/O samples, as described in the DevelopersGuide.  In this example I have an analog accelerometer outputs x, y, and z connected to pins 18, 19, and 20.  I have a push button connected to pin 12.  The remote XBee has been configured to send samples ever 200 milliseconds.

```
xbee.open("/dev/tty.usbserial-A6005v5M", 9600);

while (true) {
	XBeeResponse response = xbee.getResponse();
	
	if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
		RxResponseIoSample ioSample = (RxResponseIoSample)response;

		for (IoSample sample: ioSample.getSamples()) {		
			if (ioSample.containsAnalog()) {
				System.out.println("Analog pin 20 10-bit reading is " + sample.getAnalog0());
				System.out.println("Analog pin 19 10-bit reading is " + sample.getAnalog1());
				System.out.println("Analog pin 18 10-bit reading is " + sample.getAnalog2());
			} else {
				// we know it's change detect since analog was not sent
				log.info("Received change detect for Digital pin 12: " + (sample.isD7On() ? "on" : "off"));
			}
		}
	}
}
```

There is a third option for obtaining samples from the remote XBee on an ad-hoc basic. This requires the Force Sample command.  This useful for situations where you want to receive data at irregular times, perhaps triggered by external events, for example, a web page request.   The Force Sample uses the "IS" AT command, and is supported by both Series 1 and 2.  An example for Series 2 can be found in class ZBForceSampleExample.

This configuration is good for interacting with simple components that don't require real-time control, or precise timing.  For that, we'd need to introduce an Arduino to the remote XBee.

## PC to XBee/Arduino ##

![http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/computer-to-arduino.png](http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/computer-to-arduino.png)

In this configuration an Arduino is connected to the remote XBee via the Arduino's serial port.  The [XBee Shield](http://www.sparkfun.com/products/9841) is a good option for connecting the XBee radio to the Arduino.  Now that we have an Arduino on the remote XBee, we can do pretty much anything that you would normally do with an Arduino, except it's wireless now!  It's important to note that the XBee requires exclusive use of the Arduino's serial port, so if you want to connect a Serial device to the Arduino you'll need to use New Software Serial (NSS), or you'll need an Arduino Mega, which has multiple serial ports.

The mechanism to communicate with the Arduino in API mode are TX and RX packets.  In this example we are sending TX packets to the Arduino, using [XBee API](http://code.google.com/p/xbee-api/), to tell it to position its servos to the specified angles: 90 degrees and 180 degrees.

```
// replace with port and baud rate of your XBee
xbee.open("/dev/tty.usbserial-A6005uPi", 9600);

// Note: we are using the Java int data type, since the byte data type is not unsigned, but the payload is limited to bytes.  That is, values must be between 0-255.
int[] payload = new int[] { 90, 180 };

// specify the remote XBee 16-bit MY address
XBeeAddress16 destination = new XBeeAddress16(0x18, 0x74);

TxRequest16 tx = new TxRequest16(destination, payload);

TxStatusResponse status = (TxStatusResponse) xbee.sendSynchronous(tx);

if (status.isSuccess()) {
	// the Arduino XBee received our packet
}
```

On the Arduino side, we need to download and install the [XBee Arduino](http://code.google.com/p/xbee-arduino/) library on the Arduino and upload a sketch to receive the packet do something with it.

The following sketch receives the packet sent by the computer and sets the servos positions.  I didn't include the servo code since that is well documented on the Arduino site.

```
#include <XBee.h>

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
// create reusable response objects for responses we expect to handle 
Rx16Response rx16 = Rx16Response();

void setup() {
  // start serial
  xbee.begin(9600);
}

void loop() {    
    xbee.readPacket();
    
    if (xbee.getResponse().isAvailable()) {
      // got something
      
      if (xbee.getResponse().getApiId() == RX_16_RESPONSE) {
        // got a rx packet
        xbee.getResponse().getRx16Response(rx16);
       	data = rx16.getData(0);
       
       // set servo 1 to angle in first byte of payload
       // e.g. servo.setAngle(rx16.getData(0));
       // set servo 2 to angle in second byte of payload
       // e.g. servo2.setAngle(rx16.getData(1));
    }
}
```

Now there's a slight problem here.  The XBee payload supports bytes, but bytes support a maximum value of 255, which means we can't specify angles greater than 255.  There are couple solutions to this.  We could to translations, for example where 1 = 90 degress, 2 = 180, and so on.  Or, if we require 360 degrees of control, we need to use two bytes to represent a degree.  The first byte would be the first 8-bits (270 & 0xff) and the second byte would be the remaining bits ((270 >> 8) & 0xff).  When we received the byte array on the Arduino it will be assembled back to an int data type.

We can also send data from the Arduino back to the computer.  In this example, I'm going to pretend that I'm reading the temperature from a one-wire temperature sensor every 5 seconds and sending it to the computer.  After sending the packet, we wait for the status response, indicating if the computer received it.

I chose one-wire for the example since it requires a microcontroller -- if it was an analog temperature sensor we wouldn't need an Arduino and could simply use the XBee's I/O pins, as in the first use-case.  This sketch could be combined with the first so that we are both receiving data from the computer and sending data back.

```
#include <XBee.h>

XBee xbee = XBee();

// allocate two bytes for to hold a 10-bit analog reading
uint8_t payload[] = { 0, 0 };

// 16-bit addressing: Enter address of remote XBee, typically the coordinator
Tx16Request tx = Tx16Request(0x1111, payload, sizeof(payload));

TxStatusResponse txStatus = TxStatusResponse();

void setup() {
  xbee.begin(9600);
}

void loop() {
   
      // break down 10-bit reading into two bytes and place in payload
      int temp = // get temp from one wire sensor, i2c device etc.
      payload[0] = temp >> 8 & 0xff;
      payload[1] = temp & 0xff;
      // send to computer
      xbee.send(tx);
  
    // after sending a tx request, we expect a status response
    // wait up to 5 seconds for the status response
    if (xbee.readPacket(5000)) {
        // got a response!

        // should be a znet tx status            	
    	if (xbee.getResponse().getApiId() == TX_STATUS_RESPONSE) {
    	   xbee.getResponse().getZBTxStatusResponse(txStatus);
    		
    	   // get the delivery status, the fifth byte
           if (txStatus.getStatus() == SUCCESS) {
            	// success.  time to celebrate
           } else {
            	// the remote XBee did not receive our packet. is it powered on?
           }
        }      
    } else {
      // local XBee did not provide a timely TX Status Response -- should not happen
    }
    
    delay(5000);
}
```

## Arduino to Arduino ##

![http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/arduino-to-arduino.png](http://xbee-api.googlecode.com/svn/trunk/docs/diagrams/arduino-to-arduino.png)

Another possibility is communicate wirelessly between two Arduinos. This involves writing two sketches, for sending and receiving or even both, since XBees are duplex.

These are just a few use cases for XBee communication.  Of course you can have multiple radios and a myriad of other configurations (e.g. PC to PC, Arduino to PC etc.
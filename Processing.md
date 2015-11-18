# Introduction #

Processing is a powerful tool for visualizing physical computing projects.  Since both [Processing](http://processing.org/) and XBee-API are written in Java, it's easy to use them together.  This entry describes how.

**update 3/2/14** Processing no long uses RXTX for serial port communication. They are now using  JSSC (http://code.google.com/p/java-simple-serial-connector/). This means you need to use xbee-api with Processing you'll need to install RXTX and place in Processing classpath and library path or simply use an earlier version of Processing

# Setup #

After starting Processing, the first thing we need to do is add the XBee-API JAR files.  The easiest way to do this is to drag the JAR files into the Processing window.  The required files are xbee-api-version-.jar and log4j.jar.  The RXTX libraries are already included in Processing.

Now copy and paste the code below into the Processing editor.  You will need to change the com port to setting to the port of the XBee radio (See the DevelopersGuide for determining the com port).

The logging setup is recommended in the event things aren't working quite as planned.  You just need to specify the path to log4j.properties (included in the XBee-API download).

# Explanation #

The _setup_ method creates an XBee object and connects to the radio.  Next, it adds a _PacketListener_ to collect the responses from the radio and add to a queue for later retrieval.  If an error occurs during setup, the program will exit and print an error message.

The _readPackets_ method will retrieve all packets that are waiting in the queue.  This method does not block (wait), so it will not impact any other code in the _draw_ method.  This particular code is for receiving I/O samples but you can replace with any code to suit your needs.  See the examples folder in the download for ideas.  You can even send packets to remote radios.

This example doesn't include any graphics code, such as visualizing your data, but there are plenty of examples on the Processing site for help there.

So that's it.  Please report any issues on the [XBee-API Group](http://groups.google.com/group/xbee-api).

# Code #

```
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.PropertyConfigurator;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponseIoSample;

XBee xbee;
Queue<XBeeResponse> queue = new ConcurrentLinkedQueue<XBeeResponse>();
boolean message;
XBeeResponse response;
  
void setup() {
  try { 
    //optional.  set up logging
    PropertyConfigurator.configure(dataPath("") + "log4j.properties");

    xbee = new XBee();
    // replace with your COM port
    xbee.open("/dev/tty.usbserial-A6005v5M", 9600);

    xbee.addPacketListener(new PacketListener() {
      public void processResponse(XBeeResponse response) {
        queue.offer(response);
      }
    }
    );
  } 
  catch (Exception e) {
    System.out.println("XBee failed to initialize");
    e.printStackTrace();
    System.exit(1);
  }
}

void draw() {
  try {
    readPackets();
  } 
  catch (Exception e) {
    e.printStackTrace();
  }
}

void readPackets() throws Exception {

  while ((response = queue.poll()) != null) {
    // we got something!
    try {
      RxResponseIoSample ioSample = (RxResponseIoSample) response;

      println("We received a sample from " + ioSample.getSourceAddress());

      if (ioSample.containsAnalog()) {
        println("10-bit temp reading (pin 19) is " +
          ioSample.getSamples()[0].getAnalog1());
      }
    } 
    catch (ClassCastException e) {
      // not an IO Sample
    }
  }
}
```
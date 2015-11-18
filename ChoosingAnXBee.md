XBees are available in two different product lines, both offering several configurations.  The first decision you need to make is if you want the 804.15.4 (Series 1) or the newer ZB/ZigBee Pro (Series 2); this software supports both. Secondly, each module comes in a standard or higher power (Pro) version. And thirdly, with each module you have three antenna options: chip, whip/wire, and UFL. In general the whip antenna (recommended) offers better range but occupies more space with its 1" wire antenna. The chip antenna is almost flush with the chip but offers slightly less range. While Series 1 and Series 2 are not compatible with each other, you can however mix standard and Pro modules within the same series.

Here are some pros/cons to help decide between the two series.  Please note that this is solely based on my experience, so please do your own research and also let me know of anything that I may have missed.  If you are just beginning and want a quick recommendation, I suggest going with the Series 1 modules, with wire antenna.

### Series 1 (802.15.4): ###

Series 1 is based on the IEEE 802.15.4 WPAN Spec http://en.wikipedia.org/wiki/IEEE_802.15.4. This supports point-to-point, and point-to-multipoint (broadcast) communication. This radio provides 6 analog and 8 digital I/O pins, with 6 pins shared between digital and analog.  This module uses a Freescale chip.

Pros
  * Does not require a firmware change to support API mode or switch between Coordinator and End Device, or AT mode
  * I/O Line Monitoring supports > 1 sample per packet.
  * Supports a larger payload size per packet than series 2 (100 bytes vs 72 or 84)

Cons
  * No mesh networking, although you may not even need it.

### Series 2 (ZB Pro): ###

Series 2 is ZigBee compliant. ZigBee introduces mesh networking, where networks can be extended beyond typical wireless range limits through the use of routers. This radio supports 4 analog and 11 digital I/O pins, with 4 pins shared between digital and analog.  This module uses an Ember chip.

Pros
  * ZigBee mesh networking
  * Non-pro version has better range than series 1 equivalent (400 ft. vs 300 ft.)
  * Both pro and non-pro versions consume less power than their series 1 equivalents.
  * You can extend the range of the network beyond the limit of a single transmission by adding routers.
  * Interoperability with ZigBee hardware from other vendors that support the ZigBee PRO Feature Set

Cons
  * Analog inputs can only measure voltages between 0 and 1.2, requiring a voltage divider to step down from the reference voltage of 3.3V.
  * ZB Pro API End Device firmware (2941) does not support the "No Sleep" option (SM=0).  The default sleep option is "Cyclic Sleep" (SM=4).  This means that if you want to keep the XBee awake at all times, you should select the Router firmware.
  * Requires separate firmware for API and transparent (AT) mode. You can't switch between API and transparent mode with the same firmware as with Series 1; instead you must choose between installing the API firmware, or transparent firmware. Additionally, the firmware is specific for Coordinator, and Router/End Devices.
  * According to manual, only eight "children" allowed by a single coordinator, meaning that you would need to introduce a router XBee to scale your network beyond eight end devices.

## Where to Buy ##

I purchased my XBees from DigiKey, although many other suppliers stock them (NKC Electronics, SparkFun, Mouser etc.)

See [Hardware](Hardware.md) wiki links to suppliers
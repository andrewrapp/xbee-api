## XBee Radio ##

See [ChoosingAnXBee](ChoosingAnXBee.md) for help deciding which series XBee is best for your project

XBee 804.15.4 Models http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/point-multipoint-rfmodules/xbee-series1-module#models (recommended)

  * XBee 804.15.4 Wire Antenna http://www.digikey.com/product-detail/en/XB24-AWI-001/XB24-AWI-001-ND/935965
  * XBee Pro 804.15.4 Wire Antenna http://search.digikey.com/scripts/DkSearch/dksus.dll?Detail?name=XBP24-AWI-001-ND

XBee ZB Models http://www.digi.com/products/wireless-wired-embedded-solutions/zigbee-rf-modules/zigbee-mesh-module/xbee-zb-module#models

  * XBee ZB Wire Antenna http://www.digikey.com/scripts/dksearch/dksus.dll?vendor=0&keywords=XB24-Z7WIT-004
  * XBee Pro ZB S2B Wire Antenna http://www.digikey.com/product-detail/en/XBP24BZ7WIT-004/602-1181-ND/2344902

## USB-Serial Device with XBee Socket ##

The [XBee Explorer USB](http://www.amazon.com/gp/product/B004G4XUXU/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B004G4XUXU&linkCode=as2&tag=xbapra-20) is the easiest device for connecting an XBee to your computer.  You can also find these on ebay for much less, but keep in mind delivery will often be much slower and they may not be ROHS compliant. It took 10 days for mine to arrive from China (I'm in the US). Note: You will need to solder headers to the board if you want to access Reset or I/O pins.

## Remote XBee ##

The simplest way to hookup a remote XBee is with a [XBee Explorer Regulated](http://www.amazon.com/gp/product/B008O9431G/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B008O9431G&linkCode=as2&tag=xbapra-20) This board exposes the XBee's IO pins and regulates power to 3.3V. According to the website you can provide up to 16V power supply to power the board.

## Arduino XBee Shield ##

The [Arduino XBee Shield](http://www.amazon.com/gp/product/B006TQ30TW/ref=as_li_qf_sp_asin_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B006TQ30TW&linkCode=as2&tag=xbapra-20) is the easiest method for interfacing an XBee to an Arduino. There are lots of different shields available. Again, these can be found on ebay from China at lower cost.

# Updating Firmware #

Series 2 radios require firmware update to work with this software (API mode) since they always come from the factory with AT firmware. Firmware is updated with the [X-CTU](http://www.digi.com/support/productdetail?pid=3352&osvid=57&type=utilities) application; it is only is available for Windows (I run it on VMWare on Mac). It's not required but often a good idea to upgrade the firmware on Series 1 radios as they often ship with old firmware.

<a href='Hidden comment: 
As of this writing, I recommend the following firmware versions for use with this software: series 1 (10C8), series 2 ZB Pro (2.X.4.1). Note: you need Windows to run the X-CTU software for firmware upgrades.
'></a>

See XBee Firmware release information for all radios for more details http://www.digi.com/support/kbase/kbaseresultdetl?id=2209
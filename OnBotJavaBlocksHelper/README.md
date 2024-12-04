# FTC NeoDriver Blocks Helper

This code allows using the NeoDriver in OnBotJava or Blocks.

This only supports a single NEODriver device per robot to keep the code simple.

## Installation

First, follow the guide to get the driver installed on the robot and configure 
the device.  Once the device is working with code via Android Studio, you can 
upload the java class `NeoLEDsBlocks.java` in the OnBotJava tab when connected
to the controller hub.

After uploading, you can confirm under Blocks by going to the Java Classes 
section in the side toolbar and seeing new blocks show up.

## Usage

The same steps apply for both OnBotJava and Blocks.

In your init section, call `NeoLEDsBlocks.setLEDCount` with the name from robot
config for the device and the number of LEDs connected.

After setting the LED count, you can control the LEDS either by calling 
`NeoLEDsBlocks.setColor` with an RGB string or setting individual LEDs using
`NeoLEDsBlocks.setSingleColorLED` with the LED (zero-based) number and then
calling `NeoLEDsBlocks.updateLEDs` once to update all LEDs.   `updateLEDs` is
not needed when calling `setColor`.

## Troubleshooting

If the LEDs are not enabling, but you can see the activity light on the NEODriver
flashing, you may need to reboot the robot and try the mode again.

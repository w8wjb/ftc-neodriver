package com.w8wjb.ftc;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.RobotLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

@I2cDeviceType
@DeviceProperties(name = "Adafruit NeoDriver", xmlTag = "AdafruitNeoDriver", description = "an Adafruit NeoDriver board", builtIn = false)
public class AdafruitNeoDriverImpl extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynch, AdafruitNeoDriverImpl.Parameters>
        implements AdafruitNeoDriver, I2cAddrConfig, OpModeManagerNotifier.Notifications {

    public static final I2cAddr I2CADDR_DEFAULT = I2cAddr.create7bit(0x60);

    private static final byte SEESAW_BASE_REGISTER_ADDR = 0x00;
    private static final byte NEO_BASE_REGISTER_ADDR = 0x0E;

    /**
     * Number of the N
     */
    private static final byte NEOPIXEL_PIN = 15;

    /**
     * Maximum number of bytes that can be sent in one I2C frame
     */
    private static final int MAX_TX_BYTES = 24;

    private static final String TAG = "NeoDriver";

    private enum FunctionRegister {

        STATUS(0x00),
        PIN(0x01),
        SPEED(0x02),
        BUF_LENGTH(0x03),
        BUF(0x04),
        SHOW(0x05);

        public final byte bVal;

        FunctionRegister(int i) {
            this.bVal = (byte) i;
        }

    }

    public AdafruitNeoDriverImpl(I2cDeviceSynch deviceClient, boolean deviceClientIsOwned) {
        this(deviceClient, deviceClientIsOwned, new Parameters());
    }

    protected AdafruitNeoDriverImpl(I2cDeviceSynch i2cDeviceSynch, boolean deviceClientIsOwned, @NonNull Parameters defaultParameters) {
        super(i2cDeviceSynch, deviceClientIsOwned, defaultParameters);

        this.deviceClient.setI2cAddress(I2CADDR_DEFAULT);
        this.deviceClient.setLogging(true);
        this.deviceClient.setLoggingTag("NeoDriverI2C");

        // We ask for an initial call back here; that will eventually call internalInitialize()
        this.registerArmingStateCallback(true);

        this.deviceClient.engage();
    }


    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Adafruit;
    }

    @Override
    public String getDeviceName() {
        return "NeoDriver";
    }


    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
        sendSeesawReset();
    }

    @Override
    public void onOpModePreInit(OpMode opMode) {
        // no-op
    }

    @Override
    public void onOpModePreStart(OpMode opMode) {
        // no-op
    }

    @Override
    public void onOpModePostStop(OpMode opMode) {

        // Turn all the lights off when the OpMode is stopped
        fill(0);
        show();

    }

    @Override
    protected boolean internalInitialize(@NonNull Parameters parameters) {

        RobotLog.vv(TAG, "internalInitialize()...");


        // Make sure we're talking to the correct I2c address
        this.deviceClient.setI2cAddress(parameters.i2cAddr);

        // Can't do anything if we're not really talking to the hardware
        if (!this.deviceClient.isArmed()) {
            Log.d(TAG, "not armed");
            return false;
        }

        sendSeesawReset();
        setNeopixelPin();

        return true;
    }


    private void sendSeesawReset() {
        RobotLog.vv(TAG, "Resetting Seesaw");
        byte[] bytes = new byte[]{SEESAW_BASE_REGISTER_ADDR, 0x7F, (byte) 0xFF};

        this.deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes, I2cWaitControl.ATOMIC);

        // Wait for the Seesaw code to resetxs
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public I2cAddr getI2cAddress() {
        return this.parameters.i2cAddr;
    }

    @Override
    public void setI2cAddress(I2cAddr newAddress) {
        this.parameters.i2cAddr = newAddress;
        this.deviceClient.setI2cAddress(newAddress);
    }

    public void setNeopixelPin() {
        byte[] bytes = new byte[]{FunctionRegister.PIN.bVal, NEOPIXEL_PIN};
        this.deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes, I2cWaitControl.WRITTEN);
        RobotLog.vv(TAG, "Wrote NEOPIXEL_PIN");
    }

    @Override
    public void setNumberOfPixels(int numPixels) {


        parameters.numPixels = numPixels;

        int bufferLength = parameters.numPixels * parameters.bytesPerPixel;

        ByteBuffer buffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(FunctionRegister.BUF_LENGTH.bVal);
        buffer.putShort((short) bufferLength);

        byte[] bytes = buffer.array();
        Log.v("NeoDriver", "BUF_LENGTH " + toHex(bytes));
        deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes, I2cWaitControl.WRITTEN);
    }

    @Override
    public void setPixelColor(int index, String colorString) {
        setPixelColor(index, Color.parseColor(colorString));
    }

    @Override
    public void setPixelColor(int index, int color) {

        if (index > parameters.numPixels) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " is out of bounds of the pixel array");
        }

        int bufferSize = 3 + parameters.bytesPerPixel;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.BIG_ENDIAN);
        buffer.put(FunctionRegister.BUF.bVal);
        buffer.putShort((short) (index * parameters.bytesPerPixel));
        buffer.put(colorsToBytes(parameters.bytesPerPixel, parameters.colorOrder, color));

        byte[] bytes = buffer.array();
        deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes);

    }

    @Override
    public void setPixelColors(String... colorStrings) {
        int[] colors = new int[colorStrings.length];
        for (int i = 0; i < colorStrings.length; i++) {
            colors[i] = Color.parseColor(colorStrings[i]);
        }
        setPixelColors(colors);
    }

    @Override
    public void setPixelColors(@ColorInt int[] colors) {


        if (colors.length > parameters.numPixels) {
            throw new ArrayIndexOutOfBoundsException("Incoming color array is larger than the pixel array");
        }

        byte[] colorData = colorsToBytes(parameters.bytesPerPixel, parameters.colorOrder, colors);

        for (int chunkStart = 0; chunkStart < colorData.length; chunkStart += MAX_TX_BYTES) {
            int chunkLength = Math.min(MAX_TX_BYTES, colorData.length - chunkStart);
            sendPixelData((short) chunkStart, colorData, chunkStart, chunkLength);
        }


    }

    private void sendPixelData(short memOffset, byte[] colorData, int offset, int length) {
        int bufferSize = 3 + length;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize).order(ByteOrder.BIG_ENDIAN);
        buffer.put(FunctionRegister.BUF.bVal);
        buffer.putShort(memOffset);
        buffer.put(colorData, offset, length);

        byte[] bytes = buffer.array();
        deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes);
    }

    @Override
    public void fill(int color) {
        int[] colors = new int[parameters.numPixels];
        Arrays.fill(colors, color);
        setPixelColors(colors);
    }

    @Override
    public void fill(String colorString) {
        fill(Color.parseColor(colorString));
    }


    @Override
    public void show() {

        byte[] bytes = new byte[]{FunctionRegister.SHOW.bVal};
        deviceClient.write(NEO_BASE_REGISTER_ADDR, bytes, I2cWaitControl.WRITTEN);

    }

    private static byte[] colorsToBytes(int bytesPerPixel, ColorOrder order, int... colors) {

        byte[] colorData = new byte[colors.length * bytesPerPixel];

        for (int colorIndex = 0; colorIndex < colors.length; colorIndex++) {
            int color = colors[colorIndex];
            int dataIndex = colorIndex * bytesPerPixel;

            colorData[dataIndex + order.redIndex] = (byte) (color >> 16);
            colorData[dataIndex + order.greenIndex] = (byte) (color >> 8);
            colorData[dataIndex + order.blueIndex] = (byte) color;
            if (bytesPerPixel == 4) {
                colorData[dataIndex + 3] = (byte) (color >> 24);
            }
        }

        return colorData;
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 3);

        for (byte b : bytes) {
            builder.append(String.format(" %02X", b));
        }

        return builder.toString();
    }


    enum ColorOrder {
        RGB(0, 1, 2),
        RBG(0, 2, 1),
        GRB(1, 0, 2),
        GBR(2, 0, 1),
        BRG(1, 2, 0),
        BGR(2, 1, 0);

        private final int redIndex;
        private final int greenIndex;
        private final int blueIndex;

        private ColorOrder(int redIndex, int greenIndex, int blueIndex) {
            this.redIndex = redIndex;
            this.greenIndex = greenIndex;
            this.blueIndex = blueIndex;
        }

    }

    static class Parameters implements Cloneable {
        /**
         * the address at which the sensor resides on the I2C bus.
         */
        public I2cAddr i2cAddr = I2CADDR_DEFAULT;

        /**
         * Number of pixels in the string
         */
        public int numPixels = 1;

        /**
         * Number of bytes per pixel. Use 3 for RGB or 4 for RGBW
         */
        public int bytesPerPixel = 3;

        /**
         * Order that the pixel colors are in
         */
        public ColorOrder colorOrder = ColorOrder.GRB;


        @Override
        public Parameters clone() {
            try {
                return (Parameters) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

}

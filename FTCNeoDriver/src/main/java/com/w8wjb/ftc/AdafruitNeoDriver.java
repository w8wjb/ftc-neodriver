package com.w8wjb.ftc;

import androidx.annotation.ColorInt;

import com.qualcomm.robotcore.hardware.HardwareDevice;

public interface AdafruitNeoDriver extends HardwareDevice  {

    /**
     * Sets the number of pixels that are available in the strand
     * @param numPixels
     */
    void setNumberOfPixels(int numPixels);

    /**
     * Sets a specific pixel in a strand to a color
     * @param index Index of pixel, 0-based
     * @param colorString Hex color string, i.e. #RRGGBB
     */
    void setPixelColor(int index, String colorString);

    /**
     * Sets a specific pixel in a strand to a color
     * @param index Index of pixel, 0-based
     * @param color Color encoded as an int. See {@link  android.graphics.Color} for useful color utilities
     */
    void setPixelColor(int index, int color);

    /**
     * Sets a sequence of pixels to colors, starting at index 0
     * @param colorStrings 1 or more hex color strings, i.e. #RRGGBB
     */
    void setPixelColors(String... colorStrings);

    /**
     * Sets a sequence of pixels to colors, starting at index 0
     * @param colors Colors encoded as an int array. See {@link  android.graphics.Color} for useful color utilities
     */
    void setPixelColors(@ColorInt int[] colors);

    void fill(int color);

    void fill(String colorString);

    /**
     * This must be called to signal that the color data should be sent to the NeoPixel strand
     */
    void show();
}

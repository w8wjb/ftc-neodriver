package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.w8wjb.ftc.AdafruitNeoDriver;

@Autonomous
public class RainbowColorWheelTest extends OpMode {

    private static final int NUM_PIXELS = 30;

    AdafruitNeoDriver neopixels;

    Timer timer = new Timer();

    private int redStart = 0;
    private int hueGap = 0;

    @Override
    public void init() {

        neopixels = hardwareMap.get(AdafruitNeoDriver.class, "neopixels");

        neopixels.setNumberOfPixels(NUM_PIXELS);

        timer.reset();

        hueGap = 360 / NUM_PIXELS;

    }

    @Override
    public void loop() {

        if (timer.hasElapsed(0.25)) {

            int[] colors = new int[NUM_PIXELS];

            for (int i=0; i < colors.length; i++) {
                int hue = ((redStart + i) * hueGap) % 360;
                int color = Color.HSVToColor(new float[] { hue, 1, 1 });
                colors[i] = color;
            }

            neopixels.setPixelColors(colors);
            neopixels.show();

            timer.reset();

            redStart = (redStart + 1) % NUM_PIXELS;
        }

    }
}

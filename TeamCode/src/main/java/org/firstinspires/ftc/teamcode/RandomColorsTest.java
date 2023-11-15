package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.w8wjb.ftc.AdafruitNeoDriver;

import java.util.Random;

@Autonomous
public class RandomColorsTest extends OpMode {

    private static final int NUM_PIXELS = 30;

    AdafruitNeoDriver neopixels;

    private Random rng = new Random();
    private Timer timer = new Timer();

    @Override
    public void init() {
        neopixels = hardwareMap.get(AdafruitNeoDriver.class, "neopixels");

        neopixels.setNumberOfPixels(NUM_PIXELS);
        timer.reset();
    }

    @Override
    public void loop() {

        if (timer.hasElapsed(1)) {

            for (int p=0; p<=NUM_PIXELS; p++) {
                int color = Color.rgb(rng.nextInt(255), rng.nextInt(255), rng.nextInt(255));
                neopixels.setPixelColor(p, color);
            }
            neopixels.show();
            timer.reset();
        }

    }
}

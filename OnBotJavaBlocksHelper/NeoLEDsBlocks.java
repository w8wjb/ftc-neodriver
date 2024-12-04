package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion;
import org.firstinspires.ftc.robotcore.external.ExportToBlocks;

import java.lang.reflect.Method;
import android.graphics.Color;

public class NeoLEDsBlocks extends BlocksOpModeCompanion {

    private static Object neoleds = null;
    private static Method fill = null;
    private static Method show = null;
    private static Method setPixelColor = null;
    private static Method setPixelColors = null;
    
    private static int[] pixelColors = null;

    @ExportToBlocks (
        comment = "Call this to setup the LEDs",
        parameterLabels = {"Name of device in robot config", "Number of LEDs to use" }
    )
    public static void setLEDCount(String name, int ledCount) {
        try {
            neoleds = hardwareMap.get(name);
            if(neoleds != null) {
                Class neoClass = neoleds.getClass();
                Method initMethod = neoClass.getDeclaredMethod("setNumberOfPixels", new Class<?>[] {int.class} );
                initMethod.invoke(neoleds, ledCount);
                fill = neoClass.getDeclaredMethod("fill", new Class<?>[] {String.class} );
                show = neoClass.getDeclaredMethod("show", new Class<?>[] {} );
                setPixelColors = neoClass.getDeclaredMethod("setPixelColors", new Class<?>[] {int[].class} );
                setPixelColor = neoClass.getDeclaredMethod("setPixelColor", new Class<?>[] {int.class, String.class} );
                pixelColors = new int[ledCount];
                for(int i = 0; i < pixelColors.length; i++) {
                    pixelColors[i] = Color.parseColor("#000000");
                }
            }
        } catch(Exception e) {
            telemetry.addLine("Error updating LEDS:" + e.toString());
        }
    }
    
    @ExportToBlocks (
        comment = "Set the LEDs to a single color",
        parameterLabels = {"Hex Color Code" },
        tooltip = "Format is '#RRGGBB', for example '#EE00000' is red and '#999900' is a yellow color"
    )
    public static void setColor(String colorCode) {
        if(fill != null) {
            try {
                fill.invoke(neoleds, colorCode);
                show.invoke(neoleds);
            } catch(Exception e) {
                telemetry.addLine("Error updating LEDS:" + e.toString());
            }
        }
    }
    
    @ExportToBlocks (
        comment = "Set a single LED color",
        parameterLabels = {"LED number", "Hex Color Code" },
        tooltip = "Counts from zero, Use color code like '#EE00000' for red"
    )
    public static void setSingleColorLED(int pixel, String colorCode) {
        if(pixelColors != null) {
            if(pixel > -1 && pixel < pixelColors.length) {
                pixelColors[pixel] = Color.parseColor(colorCode);
            }
        }
    }
    
    @ExportToBlocks (
        comment = "Push color changes to LEDs",
        tooltip = "call after setting single colors"
    )
    public static void updateLEDs() {
        if(show != null && setPixelColors != null && pixelColors != null) {
            try {
                setPixelColors.invoke(neoleds, pixelColors);
                show.invoke(neoleds);
            } catch(Exception e) {
                telemetry.addLine("Error updating LEDS:" + e.toString());
            }
        }
    }
    
}
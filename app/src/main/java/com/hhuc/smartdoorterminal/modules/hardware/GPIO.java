package com.hhuc.smartdoorterminal.modules.hardware;

import java.io.DataOutputStream;
import java.io.IOException;

public class GPIO {
    static Process process = null;
    static DataOutputStream dos = null;
    private static int gpio_number;
    private static String exportPath;
    private static String directionPath;
    private static String valuePath;

    public static void gpio_crtl(String gpio, String direction, int level) {
        if (gpio.isEmpty())
            return;
        gpio_number = Integer.parseInt(gpio);
        exportPath = "echo " + gpio_number + " > /sys/class/gpio/export";
        if (direction.equals("out")) {
            directionPath = "echo out > " + " /sys/class/gpio/gpio" + gpio_number + "/direction";
            valuePath = "echo " + level + " > /sys/class/gpio/gpio" + gpio_number + "/value";
        } else if (direction.equals("in")) {
            directionPath = "echo in > " + " /sys/class/gpio/gpio" + gpio_number + "/direction";
        } else {
            return;
        }
        try {
            process = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(process.getOutputStream());
            dos.writeBytes(exportPath + "\n");
            dos.flush();
            dos.writeBytes(directionPath + "\n");
            dos.flush();
            dos.writeBytes(valuePath + "\n");
            dos.flush();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}

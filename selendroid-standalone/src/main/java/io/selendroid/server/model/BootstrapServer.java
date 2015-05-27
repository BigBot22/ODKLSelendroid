package io.selendroid.server.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class BootstrapServer extends Thread {
    private String startingServerCmd;
    private Logger log;
    private Process proc;
    private String deviceSerial;


    public BootstrapServer(String device, Logger log) {
        this.log = log;
        this.proc = null;
        this.deviceSerial = device;
        startingServerCmd = "adb -s "  + deviceSerial +
                " shell uiautomator runtest AppiumBootstrap.jar -c io.appium.android.bootstrap.Bootstrap";
    }

    @Override
    public void run() {
        try {
            Runtime rt = Runtime.getRuntime();
            while (true) {
                log.info("[MYDEBUG] starting bootstrap server");
                log.info("[MYDEBUG] executing cmd:" + startingServerCmd);
                proc = rt.exec(startingServerCmd);
                InputStream is = proc.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                log.info("[MYDEBUG] readingLine");
                while ((line = br.readLine()) != null) {
                    line = line.replace("[APPIUM-UIAUTO]", "[UIAUTO]");
                    line = line.replace("[/APPIUM-UIAUTO]", "[/UIAUTO]");
                    if (line.contains("[error] Could not start socket server")) {
                        log.info("Error:" + line);
                        return;
                    }
                    log.info(line);
                }
                proc.destroy();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void down() {
        log.info("[MYDEBUG] destroying bootstrap server");
        proc.destroy();
        proc = null;
        this.interrupt();
    }
}

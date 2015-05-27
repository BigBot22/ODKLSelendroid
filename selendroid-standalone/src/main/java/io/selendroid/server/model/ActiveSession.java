/*
 * Copyright 2012-2014 eBay Software Foundation and selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.selendroid.server.model;

import io.selendroid.SelendroidCapabilities;
import io.selendroid.android.AndroidApp;
import io.selendroid.android.AndroidDevice;
import io.selendroid.server.handler.UIAutomatorClient;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActiveSession {
  private final String sessionKey;
  private AndroidApp aut;
  private AndroidDevice device;
  private SelendroidCapabilities desiredCapabilities;
  private final int selendroidServerPort;
  private boolean invalid = false;
  private final Timer stopSessionTimer = new Timer(true);
  private boolean uiAutomationModeOn = false;
  private UIAutomatorClient uiAutomatorClient = null;
  private BootstrapServer bootstrapServer;

  private final Logger log = Logger.getLogger(this.getClass().getName());

  ActiveSession(String sessionKey, SelendroidCapabilities desiredCapabilities, AndroidApp aut,
                AndroidDevice device, int selendroidPort, SelendroidStandaloneDriver driver) {
    this.selendroidServerPort = selendroidPort;
    this.sessionKey = sessionKey;
    this.aut = aut;
    this.device = device;
    this.desiredCapabilities = desiredCapabilities;
    this.bootstrapServer = null;
    stopSessionTimer.schedule(new SessionTimeoutTask(driver, sessionKey), driver
        .getSelendroidConfiguration().getSessionTimeoutMillis());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ActiveSession other = (ActiveSession) obj;
    if (sessionKey == null) {
      if (other.sessionKey != null) return false;
    } else if (!sessionKey.equals(other.sessionKey)) return false;
    return true;
  }

  public AndroidApp getAut() {
    return aut;
  }

  public int getSelendroidServerPort() {
    return selendroidServerPort;
  }

  public SelendroidCapabilities getDesiredCapabilities() {
    return desiredCapabilities;
  }

  public AndroidDevice getDevice() {
    return device;
  }

  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sessionKey == null) ? 0 : sessionKey.hashCode());
    return result;
  }

  public boolean isInvalid() {
    return invalid;
  }

  /**
   * marks the session as invalid. This happens when e.g. the hardware device has been disconnected.
   */
  public void invalidate() {
    this.invalid = true;
  }

  public void stopSessionTimer() {
    stopSessionTimer.cancel();
    disconnectFromUiAutomatorServer();
  }

  @Override
  public String toString() {
      return "ActiveSession [sessionKey=" + sessionKey + ", aut=" + aut + ", device=" + device + "]";
  }

  public void setUiAutomationModeOn() {
      if (!uiAutomationModeOn) {
          startBootstrapServer();
          try {
              Thread.sleep(2000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          connectToUiAutomatorServer();
          uiAutomationModeOn = true;
      }
  }

  public void setUiAutomationModeOff() {
      if (uiAutomationModeOn) {
          disconnectFromUiAutomatorServer();
          uiAutomationModeOn = false;
          uiAutomatorClient = null;
          stopBootstrapServer();
      }
  }

  public boolean isUiAutomationMode() {
      return this.uiAutomationModeOn;
  }

  public UIAutomatorClient getUiAutomatorClient() {
      return uiAutomatorClient;
  }

  private void connectToUiAutomatorServer() {
      try {
          uiAutomatorClient = new UIAutomatorClient(device.getWlan());
          uiAutomatorClient.connect();
      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  private void disconnectFromUiAutomatorServer() {
      if (uiAutomatorClient != null) {
          uiAutomatorClient.disconnect();
          uiAutomatorClient = null;
      }
  }

  private void startBootstrapServer() {
      // need to start bootstrap server
      log.info("[MYDEBUG] bootstrapServer starting");
      log.info("[MYDEBUG] deice serial:" +  device.getSerial());
      bootstrapServer = new BootstrapServer(device.getSerial(), log);
      bootstrapServer.start();
      log.info("[MYDEBUG] bootstrapServer started");
  }

  private void stopBootstrapServer() {
      log.info("[MYDEBUG] bootstrapServer stoping");
      bootstrapServer.stop();
      bootstrapServer = null;
      device.stopBootstrapServer();
  }
}


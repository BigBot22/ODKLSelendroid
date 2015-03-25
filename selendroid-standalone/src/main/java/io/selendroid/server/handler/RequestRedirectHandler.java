/*
 * Copyright 2012-2013 eBay Software Foundation and selendroid committers.
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
package io.selendroid.server.handler;

import io.selendroid.android.AndroidDevice;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.exceptions.ShellCommandException;
import io.selendroid.io.ShellCommand;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.ActiveSession;
import io.selendroid.server.util.HttpClientUtil;
import org.apache.commons.exec.CommandLine;
import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import org.webbitserver.HttpRequest;

import java.util.logging.Logger;

public class RequestRedirectHandler extends BaseSelendroidServerHandler {
  private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());
  private boolean isUIAutomatorMode;
  private UIAutomatorClient uiAutomatorClient;

  public RequestRedirectHandler(String mappedUri) {
    super(mappedUri);
    isUIAutomatorMode = false;
    uiAutomatorClient = null;
  }

  @Override
  public Response handle(final HttpRequest request) throws JSONException {

    log.info("[DEBUG] request uri:" + request.uri());
    log.info("[DEBUG] request body:" + request.body());
    log.info("[DEBUG] request data:" + request.data());

    if (request.uri().contains("window")) {
        log.info("[DEBUG] get window request");
        if (request.body().contains("UIAUTOMATOR")) {
            log.info("[DEBUG] tryig switch to uiautomator mode ");
            if (isUIAutomatorMode) {
                final String sessionId = getSessionId(request);
//                return new Response() {
//                    @Override
//                    public String getSessionId() {
//                        return sessionId;
//                    }
//
//                    @Override
//                    public String render() {
//                        return "it is already UIAUTOMATOR mode";
//                    }
//                };
                log.info("[DEBUG] it is already UIAUTOMATOR mode");
                new SelendroidResponse(sessionId, 200, "it is already UIAUTOMATOR mode");
            } else {
                isUIAutomatorMode = initUIAutomationMode();
                log.info("[DEBUG] UIAutomatorMode ON");
            }
        } else {
            if (request.body().contains("INSTRUMENTATION")) {
                log.info("[DEBUG] try to switch to instrumentation mode");

                if (!isUIAutomatorMode) {
                    final String sessionId = getSessionId(request);
                    log.info("[DEBUG] it is already INSTRUMENTATION mode");

//                return new Response() {
//                    @Override
//                    public String getSessionId() {
//                        return sessionId;
//                    }
//
//                    @Override
//                    public String render() {
//                        return "it is already UIAUTOMATOR mode";
//                    }
//                };
                    new SelendroidResponse(sessionId, 200, "it is already INSTRUMENTATION mode");
                } else {
                    stopUIAutomationMode();
                    isUIAutomatorMode = false;
                    log.info("[DEBUG] UIAutomatorMode OFF");
                }
            } else {
                return instumentationHendler(request);
            }
        }
    }

    if (isUIAutomatorMode) {
        return uiAutomatorHendler(request);
    } else {
        return instumentationHendler(request);
    }
  }

    private void stopUIAutomationMode() {

    }

    private Response uiAutomatorHendler(HttpRequest request) {
        log.info("[DEBUG] hendling uiautomator instruction");
        String sessionId = getSessionId(request);
        int status = 200;
        String value = "";
        log.info("[DEBUG] selendroidRequestMOdifier begin");
        String modifiedRequest = selendroidRequestMOdifier(request);

        try {
            value = uiAutomatorClient.send(modifiedRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new SelendroidResponse(sessionId, status, value);
    }

    private String selendroidRequestMOdifier(HttpRequest request) {
        return "{\"cmd\":\"action\",\"action\":\"wake\",\"params\":{}}\n";
    }

    private boolean initUIAutomationMode() {
        //TODO: install bootstrap to phoneddd
//        String cmd = "/Users/bogdan/android-sdk-macosx/platform-tools/adb -s 4df154e30f2e9fb1 shell uiautomator runtest AppiumBootstrap.jar -c io.appium.android.bootstrap.Bootstrap";
//        try {
//            log.info("[DEBUG] execAsync:" + cmd);
//            ShellCommand.execAsync(new CommandLine(cmd));
//        } catch (ShellCommandException e) {
//            log.info("[DEBUG] ShellCommandException:");
//            e.printStackTrace();
//        }
        try {
            log.info("[DEBUG] ceating UIAutomatorClient");
            uiAutomatorClient = new UIAutomatorClient();
        } catch (Exception e) {
            log.info("[DEBUG] UIAutomatorClient Exception:");
            e.printStackTrace();
            return false;
        }
        log.info("[DEBUG] ceated UIAutomatorClient");
        return true;
    }

    private Response instumentationHendler(HttpRequest request) throws JSONException {
      log.info("[DEBUG] hendling instruments instruction");
      String sessionId = getSessionId(request);
      log.info("forward request command: for session " + sessionId);

      ActiveSession session = getSelendroidDriver(request).getActiveSession(sessionId);
      if (session == null) {
          return new SelendroidResponse(sessionId, 13, new SelendroidException(
                  "No session found for given sessionId: " + sessionId));
      }
      if (session.isInvalid()) {
          return new SelendroidResponse(sessionId, 13, new SelendroidException(
                  "The test session has been marked as invalid. "
                          + "This happens if a hardware device was disconnected but a "
                          + "test session was still active on the device."));
      }
      String url = "http://localhost:" + session.getSelendroidServerPort() + request.uri();

      String method = request.method();

      JSONObject response = null;

      int retries = 3;
      while (retries-- > 0) {
          try {
              response = redirectRequest(request, session, url, method);
              break;
          } catch (Exception e) {
              if (retries == 0) {
                  AndroidDevice device = session.getDevice();
                  log.info("getting logs");
                  device.setVerbose();
                  for (LogEntry le : device.getLogs()) {
                      System.out.println(le.getMessage());
                  }
                  return new SelendroidResponse(sessionId, 13,
                          new SelendroidException(
                                  "Error occured while communicating with selendroid server on the device: ",
                                  e));
              } else {
                  log.severe("failed to forward request to Selendroid Server");
              }
          }
      }
      Object value = response.opt("value");
      if (value != null) {
          String displayed = String.valueOf(value);
          // 2 lines of an 80 column display
          if (displayed.length() > 160) {
              displayed = displayed.substring(0, 157) + "...";
          }
          log.info("return value from selendroid android server: " + displayed);
      }
      int status = response.getInt("status");

      log.fine("return value from selendroid android server: " + value);
      log.fine("return status from selendroid android server: " + status);

      return new SelendroidResponse(sessionId, status, value);
  }

  private JSONObject redirectRequest(HttpRequest request, ActiveSession session, String url, String method)
      throws Exception, JSONException {

    HttpResponse r = null;
    if ("get".equalsIgnoreCase(method)) {
      log.info("GET redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.GET);
    } else if ("post".equalsIgnoreCase(method)) {
      log.info("POST redirect to: " + url);
      JSONObject payload = getPayload(request);
      log.info("Payload? " + payload);
      r =
          HttpClientUtil.executeRequestWithPayload(url, session.getSelendroidServerPort(),
              HttpMethod.POST, payload.toString());

    } else if ("delete".equalsIgnoreCase(method)) {
      log.info("DELETE redirect to: " + url);
      r = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
    } else {
      throw new SelendroidException("Http method not supported.");
    }
    return HttpClientUtil.parseJsonResponse(r);
  }
}

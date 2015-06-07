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

import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.ActiveSession;
import org.json.JSONException;
import org.webbitserver.HttpRequest;

import java.util.logging.Logger;

public class RequestRedirectHandler extends BaseSelendroidServerHandler {
    private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());

    public RequestRedirectHandler(String mappedUri) {
        super(mappedUri);
    }

    @Override
    public Response handle(final HttpRequest request) throws JSONException {

        log.info("[DEBUG] request \nuri:" + request.uri() +
                "\nbody:" + request.body() +
                "\ndata:" + request.data());

        final String sessionId = getSessionId(request);
        final ActiveSession activeSession = getSelendroidDriver(request).getActiveSession(sessionId);

        if (request.uri().contains("window")) {
            log.info("[DEBUG] get window request");
            if (request.body().contains("UIAUTOMATOR")) {
                log.info("[DEBUG] tryig switch to uiautomator mode ");
                Object value = "";
                if (activeSession.isUiAutomationMode()) {
                    log.info("[DEBUG] it is already UIAUTOMATOR mode");
                    return new SelendroidResponse(sessionId, 0, value);
                } else {
                    log.info("[DEBUG] setUiAutomationModeOn for session:" + activeSession);
                    activeSession.setUiAutomationModeOn();
                    log.info("[DEBUG] UIAutomatorMode " + (activeSession.isUiAutomationMode() ? "ON" : "OFF"));
                    return new SelendroidResponse(sessionId, 0, value);
                }
            } else {
                if (request.body().contains("INSTRUMENTATION")) {
                    log.info("[DEBUG] try to switch to instrumentation mode");
                    Object value = "";
                    if (!activeSession.isUiAutomationMode()) {
                        log.info("[DEBUG] it is already INSTRUMENTATION mode");
                        return new SelendroidResponse(sessionId, 0, value);
                    } else {
                        activeSession.setUiAutomationModeOff();
                        log.info("[DEBUG] UIAutomatorMode OFF , isUiAutomationMoge:" + activeSession.isUiAutomationMode());
                        return new SelendroidResponse(sessionId, 0, value);
                    }
                }
            }
        }

        if (activeSession.isUiAutomationMode()) {
            return new UiAutomatorHandler(mappedUri).handle(request);
        } else {
            return new InstrumentationHandler(mappedUri).handle(request);
        }
    }
}

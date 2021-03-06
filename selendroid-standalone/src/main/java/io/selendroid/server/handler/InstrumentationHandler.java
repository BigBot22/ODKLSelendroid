package io.selendroid.server.handler;

import io.selendroid.android.AndroidDevice;
import io.selendroid.exceptions.SelendroidException;
import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import io.selendroid.server.model.ActiveSession;
import io.selendroid.server.util.HttpClientUtil;
import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import org.webbitserver.HttpRequest;

import java.util.logging.Logger;

public class InstrumentationHandler extends BaseSelendroidServerHandler {
    private static final Logger log = Logger.getLogger(InstrumentationHandler.class.getName());

    public InstrumentationHandler(String mappedUri) {
        super(mappedUri);
    }

    public Response handle(HttpRequest request) throws JSONException {
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

        log.fine("return response from selendroid android server: " + response.toString());
        log.fine("return value from selendroid android server: " + String.valueOf(value));
        log.fine("return status from selendroid android server: " + status);

        return new SelendroidResponse(sessionId, status, value);
    }

    private JSONObject redirectRequest(HttpRequest request, ActiveSession session, String url, String method)
            throws Exception {

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


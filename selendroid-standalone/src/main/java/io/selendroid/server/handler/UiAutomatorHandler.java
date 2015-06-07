package io.selendroid.server.handler;

import io.selendroid.server.BaseSelendroidServerHandler;
import io.selendroid.server.Response;
import io.selendroid.server.SelendroidResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

import java.util.logging.Logger;

public class UiAutomatorHandler extends BaseSelendroidServerHandler {
    private static final Logger log = Logger.getLogger(UiAutomatorHandler.class.getName());

    public UiAutomatorHandler(String mappedUri) {
        super(mappedUri);
    }

    public Response handle (HttpRequest request) throws JSONException {
        log.info("[DEBUG] hendling uiautomator instruction");
        String sessionId = getSessionId(request);
        int status = 0;
        Object value = "";
        log.info("[DEBUG] request begin");

        JSONObject result = UiAutomatorConnector.request(getSelendroidDriver(request).getActiveSession(sessionId).getUiAutomatorClient(), request);
        if (result.has("value")) {
            try {
                value = result.get("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (result.has("status")) {
            try {
                status = result.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new SelendroidResponse(sessionId, status, value);
    }
}

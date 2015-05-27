package io.selendroid.server.handler;

import org.json.JSONException;
import org.json.JSONObject;
import org.webbitserver.HttpRequest;

import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class UiAutomatorConnector {


    static UIAutomatorClient client;
    public static JSONObject request(UIAutomatorClient uiAutomatorClient, HttpRequest request) throws JSONException {
        client = uiAutomatorClient;
        String uri = request.uri();
        if (uri.contains("/touch/")) {
            return UiAutomatorConnector.touchHendler(request);
        }
        if (uri.endsWith("/element")) {
            return UiAutomatorConnector.elementHendler(request);
        }
        if (uri.contains("/element/")) {

            if (uri.endsWith("/active")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/element")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/elements")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/click")) {
                return UiAutomatorConnector.clickHendler(request);
            }
            if (uri.endsWith("/submit")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/text")) {
                return UiAutomatorConnector.textHendler(request);
            }
            if (uri.endsWith("/value")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }


            if (uri.endsWith("/name")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/clear")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/selected")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/enabled")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/displayed")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/location")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/location_in_view")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.endsWith("/size")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.contains("/attribute")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.contains("/equals")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }
            if (uri.contains("/css")) {
                return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");
            }

            return new JSONObject("{'status':200,'value':{'cmd':'action','action':'notImplementedYet','params':{}}}");

        }
        if (uri.contains("implicit_wait")) {
            return UiAutomatorConnector.waitHendler(request);
        }
        if (uri.endsWith("/click")) {
            return UiAutomatorConnector.clickHendler(request);
        }

        return new JSONObject("{\"cmd\":\"action\",\"action\":\"notImplementedYet\",\"params\":{}}");
    }

    private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());
    private static JSONObject touchHendler(HttpRequest request) {
        JSONObject uiRequest = null;
        try {
            log.info("\n[DEBUG] touchHendler for uri:" + request.uri());
            //"{\"cmd\":\"action\",\"action\":\"swipe\",\"params\":{\"startX\":500,\"startY\":25,\"endX\":500,\"endY\":800,\"steps\":0}}\n\n";
            uiRequest = new JSONObject("{'errmes':'unknown touch command'}");

            if (request.uri().contains("down")) {
                log.info("\n[DEBUG] down");
                //"{'cmd':'action','action':'touchDown','params':{'x':500,'y':25}}")
                uiRequest = new JSONObject("{'cmd':'action'}");
                JSONObject body = new JSONObject(request.body());
                uiRequest.put("action", "touchDown");
                JSONObject params = new JSONObject();
                params.put("x", body.get("x"));
                params.put("y", body.get("y"));
                uiRequest.put("params", params);
                log.info("\n[DEBUG] down uiRequest:" + uiRequest.toString());

                JSONObject response = new JSONObject();
                JSONObject results = new JSONObject(client.send(uiRequest.toString() + '\n'));
                response.put("x", results);
                log.info("\n[DEBUG] down uiRequest response:" + response.toString());
                return response;
            }

            if (request.uri().contains("move")) {
                log.info("\n[DEBUG] move");
                //"{'cmd':'action','action':'touchMove','params':{'x':500,'y':25}}")
                uiRequest = new JSONObject("{'cmd':'action'}");
                JSONObject body = new JSONObject(request.body());
                uiRequest.put("action", "touchMove");
                JSONObject params = new JSONObject();
                params.put("x", body.getInt("x"));
                params.put("y", body.getInt("y"));
                uiRequest.put("params", params);
                log.info("\n[DEBUG] move uiRequest:" + uiRequest.toString());

                JSONObject response = new JSONObject();
                JSONObject results = new JSONObject(client.send(uiRequest.toString() + '\n'));
                response.put("x", results);
                log.info("\n[DEBUG] move uiRequest response:" + response.toString());
                return response;

            }

            if (request.uri().contains("up")) {
                log.info("\n[DEBUG] up");
                //"{'cmd':'action','action':'touchUp','params':{'x':500,'y':25}}")
                uiRequest = new JSONObject("{'cmd':'action'}");
                JSONObject body = new JSONObject(request.body());
                uiRequest.put("action", "touchUp");
                JSONObject params = new JSONObject();
                params.put("x", body.get("x"));
                params.put("y", body.get("y"));
                uiRequest.put("params", params);
                log.info("\n[DEBUG] up uiRequest:" + uiRequest.toString());

                JSONObject response = new JSONObject();
                JSONObject results = new JSONObject(client.send(uiRequest.toString() + '\n'));
                response.put("x", results);
                log.info("\n[DEBUG] up uiRequest response:" + response.toString());
                return response;

            }

            return uiRequest;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uiRequest;
    }
    private static JSONObject elementHendler(HttpRequest request) {
        JSONObject uiRequest = null;
        try {
            log.info("\n[DEBUG] elementHendler for uri:" + request.uri());
            uiRequest = new JSONObject("{'errmes':'unknown find command'}");

            if (request.uri().endsWith("element")) {
                log.info("\n[DEBUG] find");
                //"{\"cmd\":\"action\",\"action\":\"find\",\"params\":{\"strategy\":\"class name\",\"selector\":\"android.widget.EditText\",\"context\":\"\",\"multiple\":true}}\n\n";
                uiRequest = new JSONObject("{'cmd':'action'}");
                JSONObject body = new JSONObject(request.body());
                uiRequest.put("action", "find");
                JSONObject params = new JSONObject();
                params.put("strategy", body.get("using"));
                params.put("selector", body.get("value"));
                params.put("context", "");
                params.put("context", "");
                params.put("multiple", false);
                uiRequest.put("params", params);
                log.info("\n[DEBUG] find uiRequest:" + uiRequest.toString());

                JSONObject results = new JSONObject(client.send(uiRequest.toString() + '\n'));
                log.info("\n[DEBUG] find uiRequest response:" + results.toString());
                return results;
            }

            return uiRequest;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uiRequest;
    }
    private static JSONObject clickHendler(HttpRequest request) {
        // /wd/hub/session/4e290a0f-e094-8639-63a7-563fa7f0cea3/element/3/click
        // "{"cmd":"action","action":"element:click","params":{"elementId":"1"}}
        JSONObject uiRequest = null;
        try {
            log.info("\n[DEBUG] clickHendler for uri:" + request.uri());
            uiRequest = new JSONObject("{'errmes':'unknown click command'}");

            if (request.uri().endsWith("click")) {
                log.info("\n[DEBUG] click");
                uiRequest = new JSONObject("{'cmd':'action'}");
                JSONObject body = new JSONObject(request.body());
                uiRequest.put("action", "element:click");
                JSONObject params = new JSONObject();
                params.put("elementId", body.get("id").toString());
                uiRequest.put("params", params);
                log.info("\n[DEBUG] click uiRequest:" + uiRequest.toString());

                String fromClient = client.send(uiRequest.toString() + '\n');
                if (fromClient != null) {
                    return new JSONObject(fromClient);
                }
                return new JSONObject();
            }

            return uiRequest;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uiRequest;
    }
    private static JSONObject textHendler(HttpRequest request) {
        // /wd/hub/session/4e290a0f-e094-8639-63a7-563fa7f0cea3/element/3/text
        // {"cmd":"action","action":"element:getText","params":{"elementId":"8"}}
        JSONObject uiRequest = null;
        try {
            log.info("\n[DEBUG] textHendler for uri:" + request.uri());
            uiRequest = new JSONObject("{'errmes':'unknown command text'}");

            log.info("\n[DEBUG] text");
            uiRequest = new JSONObject("{'cmd':'action'}");
            uiRequest.put("action", "element:getText");
            JSONObject params = new JSONObject();

            String list[] = request.uri().split("/");

            params.put("elementId", "" + list[list.length - 2]);
            uiRequest.put("params", params);
            log.info("\n[DEBUG] click uiRequest:" + uiRequest.toString());

            String fromClient = client.send(uiRequest.toString() + '\n');
            if (fromClient != null) {
                return new JSONObject(fromClient);
            }
            return new JSONObject();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uiRequest;
    }
    private static JSONObject waitHendler(HttpRequest request) {
        JSONObject result = null;
        try {
            result = new JSONObject("{'errmes':'unknown find command'}");

            if (request.uri().contains("")) {
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}


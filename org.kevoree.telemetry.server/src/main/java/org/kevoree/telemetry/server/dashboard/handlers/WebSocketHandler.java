package org.kevoree.telemetry.server.dashboard.handlers;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.log.Log;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by gregory.nain on 28/08/2014.
 */
public class WebSocketHandler implements WebSocketConnectionCallback {
    private HashMap<String, WebSocketChannel> connections = new HashMap<String, WebSocketChannel>();

    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                String data = message.getData();
                Log.debug(data);
                JsonObject content = JsonObject.readFrom(data);
                JsonValue val = content.get("topic");
                if(val != null) {
                    String sub = val.asString();
                    if(sub.contains("#")) {
                        sub = sub.substring(0, sub.lastIndexOf("#"));
                    }
                    connections.put(sub, channel);
                    Log.debug("Registered channel on topic:" + val.asString() + "->"+ sub);
                }
            }

            @Override
            protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                super.onClose(webSocketChannel, channel);
                connections.remove(channel);
            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                super.onError(channel, error);
                connections.remove(channel);
            }
        });
        webSocketChannel.resumeReceives();
    }

    public void processMessage(String tpc, String payload) {
        JsonObject obj = new JsonObject();
        obj.add("topic", tpc);
        obj.add("payload", payload);
        String msg = obj.toString();
        for(String subscription : connections.keySet()) {
            if(tpc.startsWith(subscription)) {
                WebSockets.sendText(msg, connections.get(subscription), null);
            }
        }
    }
}

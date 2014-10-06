package org.kevoree.telemetry.server.dashboard.handlers;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import io.undertow.io.DefaultIoCallback;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gregory.nain on 28/08/2014.
 */
public class WebSocketHandler implements WebSocketConnectionCallback {
    private HashMap<String, List<WebSocketChannel>> connections = new HashMap<String, List<WebSocketChannel>>();
    private ExecutorService exec = Executors.newFixedThreadPool(5);

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
                    connections.computeIfAbsent(sub, t -> new ArrayList<>()).add(channel);
                    Log.debug("Registered channel on topic:" + val.asString() + "->"+ sub);
                }
            }

            @Override
            protected void onClose(final WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                super.onClose(webSocketChannel, channel);
                connections.values().forEach(lst -> lst.remove(webSocketChannel));

            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                super.onError(channel, error);
                connections.values().forEach(lst -> lst.remove(channel));
            }
        });
        webSocketChannel.resumeReceives();
    }

    public void processMessage(String tpc, String payload, String path) {
        JsonObject obj = new JsonObject();
        obj.add("topic", tpc);
        obj.add("path", path);
        obj.add("payload", payload);
        final String msg = obj.toString();
        connections.keySet().forEach(subscription -> {
                    if (tpc.startsWith(subscription)) {
                        for (final WebSocketChannel chan : connections.get(subscription)) {
                            exec.execute(() -> {
                                WebSockets.sendText(msg, chan, null);
                            });
                        }
                    }
                }
        );
    }
}

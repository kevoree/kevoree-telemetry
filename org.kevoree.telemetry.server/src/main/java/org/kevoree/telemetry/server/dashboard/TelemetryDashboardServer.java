package org.kevoree.telemetry.server.dashboard;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.dashboard.handlers.DataRequestHandler;
import org.kevoree.telemetry.server.dashboard.handlers.WebSocketHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 *
 * @author gnain
 */
public class TelemetryDashboardServer {

    private Undertow server;
    private WebSocketHandler wsHandler = new WebSocketHandler();

    public TelemetryDashboardServer() {

        PathHandler handler = Handlers.path();
        FileResourceManager mgr = null;
        try {
            mgr = new FileResourceManager(new File(getClass().getClassLoader().getResource("static").toURI()),1000);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpHandler resourceHandler = Handlers.resource(mgr);

        handler.addPrefixPath("/", resourceHandler);
        handler.addPrefixPath("/data", new DataRequestHandler());
        handler.addExactPath("/mqtt",Handlers.websocket(wsHandler));

        server = Undertow.builder()
                .addHttpListener(9967, "localhost")
                .setHandler(handler).build();
    }


    public void processMessage(String topic, String payload, String path) {
        wsHandler.processMessage(topic, payload, path);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

}

package org.kevoree.telemetry.server.dashboard;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.kevoree.log.Log;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.dashboard.handlers.DataRequestHandler;
import org.kevoree.telemetry.server.dashboard.handlers.WebSocketHandler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * @author gnain
 */
public class TelemetryDashboardServer {

    private Undertow server;
    private WebSocketHandler wsHandler = new WebSocketHandler();

    public TelemetryDashboardServer() {

        PathHandler handler = Handlers.path();
        ResourceManager mgr = null;
        try {
            URL me = getClass().getClassLoader().getResource("static");
            if(me == null || me.getProtocol()==null) {
                Log.error("Error while location resources. Aborting. (Protocol NULL in '" + me + "')");
                System.exit(-1);
            }
            if(me.getProtocol().equals("file")) {
                mgr = new FileResourceManager(new File(me.toURI()), 1000);
            } else if(me.getProtocol().equals("jar")) {
                mgr = new ClassPathResourceManager(this.getClass().getClassLoader(),"static/");
            } else {
                Log.error("Error while location resources. Aborting. (Protocol unknown in '" + me + "')");
                System.exit(-1);
            }
        } catch (URISyntaxException e) {
            Log.error("URI Exception. File:" + mgr, e);
        }
        HttpHandler resourceHandler = Handlers.resource(mgr);

        handler.addPrefixPath("/", resourceHandler);
        handler.addPrefixPath("/data", new DataRequestHandler());
        handler.addExactPath("/mqtt",Handlers.websocket(wsHandler));

        server = Undertow.builder()
                .addHttpListener(9967, "0.0.0.0")
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

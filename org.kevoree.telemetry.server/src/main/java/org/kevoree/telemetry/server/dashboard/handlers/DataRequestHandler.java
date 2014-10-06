package org.kevoree.telemetry.server.dashboard.handlers;

import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;

import java.util.HashMap;

/**
 * Created by gregory.nain on 27/08/2014.
 */
public class DataRequestHandler implements HttpHandler {

    private TopicsRequestHandler topicsHandler;
    private MemoryRequestHandler memoryHandler;

    public DataRequestHandler() {
        topicsHandler = new TopicsRequestHandler();
        memoryHandler = new MemoryRequestHandler();
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        //System.out.println("[DataRequestHandler] Requested:" + httpServerExchange.getRequestPath());
        if(httpServerExchange.getRequestPath().startsWith("/data/topics")) {
            topicsHandler.handleRequest(httpServerExchange);
        } else if(httpServerExchange.getRequestPath().startsWith("/data/memory")) {
            memoryHandler.handleRequest(httpServerExchange);
        } else {
            httpServerExchange.setResponseCode(404);
            httpServerExchange.getResponseSender().send("Not found!", IoCallback.END_EXCHANGE);
        }

    }
}

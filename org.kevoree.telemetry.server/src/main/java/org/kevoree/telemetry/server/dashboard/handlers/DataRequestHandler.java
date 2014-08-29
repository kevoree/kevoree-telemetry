package org.kevoree.telemetry.server.dashboard.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;

import java.util.HashMap;

/**
 * Created by gregory.nain on 27/08/2014.
 */
public class DataRequestHandler implements HttpHandler {

    private TelemetryTransactionManager transactionManager;
    private TopicsRequestHandler topicsHandler;

    public DataRequestHandler(TelemetryTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        topicsHandler = new TopicsRequestHandler(transactionManager);
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        //System.out.println("[DataRequestHandler] Requested:" + httpServerExchange.getRequestPath());
        if(httpServerExchange.getRequestPath().startsWith("/data/topics")) {
            topicsHandler.handleRequest(httpServerExchange);
        } else {
            httpServerExchange.setResponseCode(404);
            httpServerExchange.getResponseSender().send("Not found!");
        }

    }
}

package org.kevoree.telemetry.server.topichandlers.node;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.kevoree.log.Log;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.topichandlers.HandlingContext;
import org.kevoree.telemetry.server.topichandlers.TopicHandler;
import org.kevoree.telemetry.server.topichandlers.node.runtime.NodeRuntimeMemoryTopicHandler;
import org.kevoree.telemetry.server.topichandlers.node.runtime.NodeRuntimePropertiesTopicHandler;
import org.kevoree.telemetry.store.LogTicket;
import org.kevoree.telemetry.store.Node;
import org.kevoree.telemetry.store.TelemetryStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public class NodeRuntimeTopicHandler implements TopicHandler {

    public static final String KEY = "runtime";

    private NodeRuntimeTopicHandler() {
    }

    private static NodeRuntimeTopicHandler INSTANCE = new NodeRuntimeTopicHandler();

    public static TopicHandler getInstance() {
        return INSTANCE;
    }

    private static final Map<String, TopicHandler> subHandlers = new HashMap<String, TopicHandler>();

    static {
        subHandlers.put(NodeRuntimeMemoryTopicHandler.KEY, NodeRuntimeMemoryTopicHandler.getInstance());
        subHandlers.put(NodeRuntimePropertiesTopicHandler.KEY, NodeRuntimePropertiesTopicHandler.getInstance());
    }

    @Override
    public String handleMessage(HandlingContext ctx) {
        String chunk = ctx.relativeTopic.poll();
        TopicHandler handler = subHandlers.get(chunk);
        if (handler != null) {
            return handler.handleMessage(ctx);
        } else {
            Log.warn("NodeRuntimeTopicHandler:: No topic handler found for chunk:" + chunk + " in topic:" + ctx.topic + " with payload:" + ctx.payload);
            return null;
        }
    }
}

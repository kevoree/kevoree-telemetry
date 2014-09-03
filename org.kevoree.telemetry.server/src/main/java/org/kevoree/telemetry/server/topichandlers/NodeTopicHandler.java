package org.kevoree.telemetry.server.topichandlers;

import org.kevoree.log.Log;
import org.kevoree.telemetry.server.topichandlers.node.NodeLogTopicHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public class NodeTopicHandler implements TopicHandler {

    public static final String KEY = "nodes";
    private  NodeTopicHandler(){}
    private static final NodeTopicHandler INSTANCE = new NodeTopicHandler();
    private static final Map<String, TopicHandler> subHandlers = new HashMap<String, TopicHandler>();
    public static TopicHandler getInstance() {
        return INSTANCE;
    }

    static {
        subHandlers.put(NodeLogTopicHandler.KEY, NodeLogTopicHandler.getInstance());
    }

    @Override
    public String handleMessage(String topic, String payload, Queue<String> relativeTopic) {
        String nodeName = relativeTopic.poll();
        String chunk = relativeTopic.poll();
        TopicHandler handler = subHandlers.get(chunk);
        if(handler != null) {
            return handler.handleMessage(topic, payload, relativeTopic);
        } else {
            Log.warn("NodeTopicHandler:: No topic handler found for chunk:"+chunk+" in topic:"+topic+" with payload:"+payload);
            return null;
        }

    }

}

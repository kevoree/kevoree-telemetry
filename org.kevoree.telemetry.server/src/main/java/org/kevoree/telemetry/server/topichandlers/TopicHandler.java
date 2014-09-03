package org.kevoree.telemetry.server.topichandlers;

import java.util.Queue;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public interface TopicHandler {
    String handleMessage(String topic, String payload, Queue<String> relativeTopic);
}

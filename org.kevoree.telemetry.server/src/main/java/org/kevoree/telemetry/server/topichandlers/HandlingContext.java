package org.kevoree.telemetry.server.topichandlers;

import java.util.Queue;

/**
 * Created by gregory.nain on 04/09/2014.
 */
public class HandlingContext {

    public String topic;
    public String nodeName;
    public Queue<String> relativeTopic;
    public String payload;


}

package org.kevoree.telemetry.server.topichandlers.node;

import com.eclipsesource.json.JsonObject;
import org.kevoree.log.Log;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.topichandlers.TopicHandler;
import org.kevoree.telemetry.store.LogTicket;
import org.kevoree.telemetry.store.Node;
import org.kevoree.telemetry.store.TelemetryStore;
import org.kevoree.telemetry.store.Ticket;

import java.util.Queue;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public class NodeLogTopicHandler implements TopicHandler {

    public static final String KEY = "log";
    private  NodeLogTopicHandler(){}
    private static NodeLogTopicHandler INSTANCE = new NodeLogTopicHandler();
    public static TopicHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public String handleMessage(String topic, String payload, Queue<String> relativeTopic) {

        JsonObject jsonObject = JsonObject.readFrom(payload);
        String nodeName = jsonObject.get("origin").asString();

        TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
        TelemetryTimeView view = transaction.time(Long.valueOf(jsonObject.get("timestamp").asString()));
        TelemetryStore store = (TelemetryStore) view.lookup("/");
        Node node = store.findNodesByID(nodeName);
        if (node == null) {
            node = view.createNode().withName(nodeName);
            store.addNodes(node);
            Log.debug("Creating Node:" + node.getName());
        }

        LogTicket ticket = node.getLog();
        if (ticket != null) {
            ticket.withMessage(jsonObject.get("message").asString()).withStack(jsonObject.get("stack").asString()).withType(jsonObject.get("type").asString()).withOrigin(jsonObject.get("origin").asString());
        } else {
            ticket = (LogTicket) view.createLogTicket().withMessage(jsonObject.get("message").asString()).withStack(jsonObject.get("stack").asString()).withType(jsonObject.get("type").asString()).withOrigin(jsonObject.get("origin").asString());
            node.setLog(ticket);
            Log.debug("Adding ticket");
        }
        transaction.commit();
        return ticket.path();
    }
}

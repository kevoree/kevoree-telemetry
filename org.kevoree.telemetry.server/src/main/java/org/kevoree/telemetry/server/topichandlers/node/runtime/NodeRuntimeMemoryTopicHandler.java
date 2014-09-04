package org.kevoree.telemetry.server.topichandlers.node.runtime;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.ParseException;
import org.kevoree.log.Log;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.topichandlers.HandlingContext;
import org.kevoree.telemetry.server.topichandlers.TopicHandler;
import org.kevoree.telemetry.store.*;

import java.util.function.Consumer;

/**
 * Created by gregory.nain on 04/09/2014.
 */
public class NodeRuntimeMemoryTopicHandler implements TopicHandler {
    public static final String KEY = "memory";
    private NodeRuntimeMemoryTopicHandler(){}
    private static NodeRuntimeMemoryTopicHandler INSTANCE = new NodeRuntimeMemoryTopicHandler();
    public static TopicHandler getInstance() {
        return INSTANCE;
    }


    @Override
    public String handleMessage(HandlingContext ctx) {
        try {
             JsonObject jsonObject = JsonObject.readFrom(ctx.payload);
            String nodeName = jsonObject.get("origin").asString();
            JsonObject message = jsonObject.get("message").asObject();

            TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
            TelemetryTimeView view = transaction.time(Long.valueOf(jsonObject.get("timestamp").asString()));
            TelemetryStore store = (TelemetryStore) view.lookup("/");
            Node node = store.findNodesByID(nodeName);
            if (node == null) {
                node = view.createNode().withName(nodeName);
                store.addNodes(node);
                Log.debug("Creating Node:" + node.getName());
            }

            MemoryInfoTicket ticket = node.getMemory();
            if(ticket == null) {
                ticket = view.createMemoryInfoTicket();
                node.setMemory(ticket);
            }

            MemoryInfo heapInfos = ticket.getHeapMemory();
            if(heapInfos == null) {
                heapInfos = view.createMemoryInfo();
                ticket.setHeapMemory(heapInfos);
            }
            JsonObject heapInfosJson = message.get("heapMemory").asObject();
            heapInfos.withInit(heapInfosJson.get("init").asLong()).withCommitted(heapInfosJson.get("committed").asLong()).withMax(heapInfosJson.get("max").asLong()).withUsed(heapInfosJson.get("used").asLong());

            MemoryInfo offHeapInfos = ticket.getOffHeapMemory();
            if(offHeapInfos == null) {
                offHeapInfos = view.createMemoryInfo();
                ticket.setOffHeapMemory(offHeapInfos);
            }
            JsonObject offHeapInfosJson = message.get("heapMemory").asObject();
            offHeapInfos.withInit(offHeapInfosJson.get("init").asLong()).withCommitted(offHeapInfosJson.get("committed").asLong()).withMax(offHeapInfosJson.get("max").asLong()).withUsed(offHeapInfosJson.get("used").asLong());

            ticket.setPendingFinalization(message.get("pendingFinalization").asInt());

            transaction.commit();
            return ticket.path();
        } catch(ParseException ex) {
            Log.error("Error when parsing payload:" + ctx.payload, ex);
            return null;
        }
    }
}

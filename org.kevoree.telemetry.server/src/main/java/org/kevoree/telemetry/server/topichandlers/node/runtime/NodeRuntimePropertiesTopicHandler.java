package org.kevoree.telemetry.server.topichandlers.node.runtime;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import org.kevoree.log.Log;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.server.topichandlers.HandlingContext;
import org.kevoree.telemetry.server.topichandlers.TopicHandler;
import org.kevoree.telemetry.store.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by gregory.nain on 04/09/2014.
 */
public class NodeRuntimePropertiesTopicHandler implements TopicHandler {
    public static final String KEY = "properties";
    private NodeRuntimePropertiesTopicHandler(){}
    private static NodeRuntimePropertiesTopicHandler INSTANCE = new NodeRuntimePropertiesTopicHandler();
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
            final TelemetryTimeView view = transaction.time(Long.valueOf(jsonObject.get("timestamp").asString()));
            TelemetryStore store = (TelemetryStore) view.lookup("/");
            Node node = store.findNodesByID(nodeName);
            if (node == null) {
                node = view.createNode().withName(nodeName);
                store.addNodes(node);
                Log.debug("Creating Node:" + node.getName());
            }

            RuntimeInfoTicket ticket = node.getRuntime();
            if(ticket == null) {
                ticket = view.createRuntimeInfoTicket();
                node.setRuntime(ticket);
            }

            ticket.withName(message.get("name").asString())
                    .withBootClasspath(message.get("bootClasspath").asString())
                    .withClassPath(message.get("classpath").asString())
                    .withLibraryPath(message.get("libpath").asString())
                    .withManagementSpecVersion(message.get("mgmtSpecVersion").asString())
                    .withStartTime(message.get("startTime").asLong())
                    .withUpTime(message.get("upTime").asLong());

            VmDetail vmDetail = ticket.getVm();
            if(vmDetail == null) {
                vmDetail = view.createVmDetail();
                ticket.setVm(vmDetail);
            }
            JsonObject jsonVm = message.get("vm").asObject();
            vmDetail.withName(jsonVm.get("name").asString()).withVendor(jsonVm.get("vendor").asString()).withVersion(jsonVm.get("version").asString());

            VmDetail spec = ticket.getSpec();
            if(spec == null) {
                spec = view.createVmDetail();
                ticket.setSpec(spec);
            }
            JsonObject jsonSpec = message.get("spec").asObject();
            spec.withName(jsonSpec.get("name").asString()).withVendor(jsonSpec.get("vendor").asString()).withVersion(jsonSpec.get("version").asString());

            JsonArray inputArgsJson = message.get("inputArguments").asArray();
            for(JsonValue value : inputArgsJson.values()) {
                ticket.getInputArguments().add(value.asString());
            }


            final List<KeyValuePair> systemProps = new ArrayList<KeyValuePair>();
            JsonObject systemPropsJson = message.get("systemProperties").asObject();
            for(String name : systemPropsJson.names()) {
                systemProps.add(view.createKeyValuePair().withKey(name).withValue(systemPropsJson.get(name).asString()));
            }
            ticket.addAllSystemProperties(systemProps);


            transaction.commit();
            return ticket.path();
        } catch(ParseException ex) {
            Log.error("Error when parsing payload:" + ctx.payload, ex);
            return null;
        }
    }
}

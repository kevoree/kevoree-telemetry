package org.kevoree.telemetry.server;

import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.server.ServerAcceptor;
import org.dna.mqtt.moquette.server.netty.NettyAcceptor;
import org.kevoree.log.Log;
import org.kevoree.modeling.datastores.leveldb.LevelDbDataStore;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import org.kevoree.telemetry.server.topichandlers.HandlingContext;
import org.kevoree.telemetry.server.topichandlers.NodeTopicHandler;
import org.kevoree.telemetry.server.topichandlers.TopicHandler;
import org.kevoree.telemetry.store.TelemetryStore;

import java.io.IOException;
import java.util.*;

/**
 * Created by duke on 6/18/14.
 */
public class ExServer {

    private Integer port;
    private static final Map<String, TopicHandler> subHandlers = new HashMap<String, TopicHandler>();
    static {
        subHandlers.put(NodeTopicHandler.KEY, NodeTopicHandler.getInstance());
    }


    public ExServer(Integer port) {
        this.port = port;
    }

    private ServerAcceptor m_acceptor;
    SimpleMessaging messaging;

   public void startServer() throws IOException {

        checkOrCreateStore();

        System.out.println("Started Bridge to localhost:" + (port+1));
        Properties configProps = new Properties();
        configProps.put("host", "0.0.0.0");
        configProps.put("port", this.port.toString());
        configProps.put("password_file","");

        messaging = SimpleMessaging.getInstance();
        messaging.init(configProps);

        m_acceptor = new NettyAcceptor();
        m_acceptor.initialize(messaging, configProps);
        System.out.println("Telemetry Server started");
    }

    public void stopServer() {
        messaging.stop();
        m_acceptor.close();

        TelemetryServerKernel.getTransactionManager().close();
        System.out.println("Telemetry Server stopped");
    }


    public String processMessage(String topic, String payload) {
        Log.trace("[ProcessMessage] Topic:" + topic + " payload:" + payload);
        String result = "";
        String[] topics = topic.split("/");
        Queue<String> relativeTopic = new ArrayDeque<String>();
        relativeTopic.addAll(Arrays.asList(topics));

        HandlingContext ctx = new HandlingContext();
        ctx.topic = topic;
        ctx.relativeTopic = relativeTopic;
        ctx.payload = payload;

        String chunck = relativeTopic.poll();
        TopicHandler handler = subHandlers.get(chunck);
        if(handler != null) {
            return handler.handleMessage(ctx);
        } else {
            Log.warn("ExServer:: No topic handler found for chunk:"+chunck+" in topic:"+topic+" with payload:"+payload);
            return null;
        }
    }


    private void checkOrCreateStore() {
        TelemetryServerKernel.setTransactionManager(new TelemetryTransactionManager(new LevelDbDataStore("TelemetryDB")));
        TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
        TelemetryTimeView view = transaction.time(Long.MIN_VALUE);
        if(view.lookup("/") == null) {
            System.out.println("Creating a root at time:" + Long.MIN_VALUE);
            TelemetryStore store = view.createTelemetryStore();
            view.root(store);
            assert(view.lookup("/") != null);
            transaction.commit();
            assert(view.lookup("/") != null);
            transaction.close();
            TelemetryTransaction transaction2 = TelemetryServerKernel.getTransactionManager().createTransaction();
            TelemetryTimeView view2 = transaction2.time(System.currentTimeMillis());
            assert(view2.lookup("/") != null);
        }
    }
}

package org.kevoree.telemetry.server;

import com.eclipsesource.json.JsonObject;
import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.server.ServerAcceptor;
import org.dna.mqtt.moquette.server.netty.NettyAcceptor;
import org.kevoree.log.Log;
import org.kevoree.modeling.datastores.leveldb.LevelDbDataStore;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import org.kevoree.telemetry.store.TelemetryStore;
import org.kevoree.telemetry.store.Ticket;
import org.kevoree.telemetry.store.Topic;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by duke on 6/18/14.
 */
public class ExServer {

    private TelemetryTransactionManager transactionManager;
   private Integer port;

    public ExServer(Integer port) {
        this.port = port;
    }

    private ServerAcceptor m_acceptor;
    SimpleMessaging messaging;

    public TelemetryTransactionManager getTransactionManager() {
        return transactionManager;
    }

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

        transactionManager.close();
        System.out.println("Telemetry Server stopped");
    }


    public String processMessage(String topic, String payload) {
        Log.debug("[ProcessMessage] Topic:" + topic + " payload:" + payload);

        String[] topics = topic.split("/");
        JsonObject jsonObject = JsonObject.readFrom(payload);

        TelemetryTransaction transaction = transactionManager.createTransaction();
        TelemetryTimeView view = transaction.time(Long.valueOf(jsonObject.get("timestamp").asString()));
        TelemetryStore store = (TelemetryStore)view.lookup("/");
        Topic rootTopic = store.findTopicsByID(topics[0]);
        if(rootTopic == null) {
            rootTopic = view.createTopic().withName(topics[0]);
            store.addTopics(rootTopic);
            Log.debug("Creating root topic:" + rootTopic.getName());
        }
        for(int i = 1; i < topics.length; i++) {
            Topic tmp = rootTopic.findTopicsByID(topics[i]);
            if(tmp == null) {
                tmp = view.createTopic().withName(topics[i]);
                rootTopic.addTopics(tmp);
                Log.debug("Creating sub-topic:" + tmp.getName());
            }
            rootTopic = tmp;
        }
        Ticket ticket = rootTopic.getTicket();
        if(rootTopic.getTicket() != null) {
            ticket.withMessage(jsonObject.get("message").asString()).withStack(jsonObject.get("stack").asString()).withType(jsonObject.get("type").asString()).withOrigin(jsonObject.get("origin").asString());
        } else {
            ticket = view.createTicket().withMessage(jsonObject.get("message").asString()).withStack(jsonObject.get("stack").asString()).withType(jsonObject.get("type").asString()).withOrigin(jsonObject.get("origin").asString());
            rootTopic.setTicket(ticket);
            Log.debug("Adding ticket");
        }
        transaction.commit();
        return ticket.path();
    }


    private void checkOrCreateStore() {
        transactionManager = new TelemetryTransactionManager(new LevelDbDataStore("TelemetryDB"));
        TelemetryTransaction transaction = transactionManager.createTransaction();
        TelemetryTimeView view = transaction.time(Long.MIN_VALUE);
        if(view.lookup("/") == null) {
            System.out.println("Creating a root at time:" + Long.MIN_VALUE);
            TelemetryStore store = view.createTelemetryStore();
            view.root(store);
            assert(view.lookup("/") != null);
            transaction.commit();
            assert(view.lookup("/") != null);
            transaction.close();
            TelemetryTransaction transaction2 = transactionManager.createTransaction();
            TelemetryTimeView view2 = transaction2.time(System.currentTimeMillis());
            assert(view2.lookup("/") != null);
        }
    }
}

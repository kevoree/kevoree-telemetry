package org.kevoree.telemetry.server;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.kevoree.log.Log;
import org.kevoree.telemetry.server.dashboard.TelemetryDashboardServer;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by duke on 8/7/14.
 */
public class App {


    public static Integer port = 9966;

    public static void main(String[] args) throws IOException, URISyntaxException {
        final ExLogger logger = new ExLogger();
        InternalLoggerFactory.setDefaultFactory(new InternalLoggerFactory() {
            @Override
            protected InternalLogger newInstance(String s) {
                return logger;
            }
        });
        Log.set(Log.LEVEL_TRACE);

        final ExServer server = new ExServer(port);
        final MQTT mqtt = new MQTT();
        final TelemetryDashboardServer dashboardServer;

        server.startServer();

        dashboardServer = new TelemetryDashboardServer(server.getTransactionManager());
        dashboardServer.start();

        mqtt.setClientId("master");
        mqtt.setCleanSession(true);
        mqtt.setHost("tcp://localhost:" + port);
        final CallbackConnection connection = mqtt.callbackConnection();
        connection.listener(new Listener() {

            @Override
            public void onConnected() {
                Log.info("MQTT Listener Connected");
            }

            @Override
            public void onDisconnected() {
                Log.info("MQTT Listener Disconnected");
            }

            @Override
            public void onPublish(UTF8Buffer topi, Buffer body, Runnable ack) {
                String topic = topi.utf8().toString();
                String payload = body.utf8().toString();
                server.processMessage(topic, payload);
                dashboardServer.processMessage(topic, payload);
                /*
                String payload = new String(body.utf8().toString());
                System.out.println(topic.utf8());
                System.out.println(payload);

                JsonObject jsonObject = JsonObject.readFrom(payload);
                System.err.println("origin:"+jsonObject.get("origin"));
                */

            }

            @Override
            public void onFailure(Throwable value) {
                Log.error("MQTT Listener failed !", value);
                //value.printStackTrace();
            }
        });
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {

                Topic[] topics = {new Topic("#", QoS.AT_LEAST_ONCE)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        System.out.println("MQTT connected to topic #");
                    }

                    public void onFailure(Throwable value) {
                        Log.error("MQTT server could not connect to #", value);
                        //value.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {
                Log.error("MQTT server could not connect.", value);
            }
        });




        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                dashboardServer.stop();
                server.stopServer();
            }
        }));
    }

}

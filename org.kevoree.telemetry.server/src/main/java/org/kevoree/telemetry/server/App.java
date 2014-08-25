package org.kevoree.telemetry.server;

import com.eclipsesource.json.JsonObject;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.kevoree.log.Log;

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
        Log.set(Log.LEVEL_WARN);

        System.out.println("Kevoree Telemetry Server");

        ExServer server = new ExServer(port);
        server.startServer();

        MQTT mqtt = new MQTT();
        mqtt.setClientId("master");
        mqtt.setCleanSession(true);
        mqtt.setHost("tcp://localhost:" + port);
        final CallbackConnection connection = mqtt.callbackConnection();
        connection.listener(new Listener() {

            @Override
            public void onConnected() {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                String payload = new String(body.utf8().toString());
                System.out.println(topic.utf8());
                System.out.println(payload);

                JsonObject jsonObject = JsonObject.readFrom(payload);
                System.err.println("origin:"+jsonObject.get("origin"));

            }

            @Override
            public void onFailure(Throwable value) {
                value.printStackTrace();
            }
        });
        connection.connect(new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {

                Topic[] topics = {new Topic("nodes/#", QoS.AT_LEAST_ONCE)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {

                    }

                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Throwable value) {

            }
        });

    }

}

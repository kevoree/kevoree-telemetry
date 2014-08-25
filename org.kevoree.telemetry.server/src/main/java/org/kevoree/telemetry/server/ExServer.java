package org.kevoree.telemetry.server;

import org.dna.mqtt.moquette.messaging.spi.impl.SimpleMessaging;
import org.dna.mqtt.moquette.server.ServerAcceptor;
import org.dna.mqtt.moquette.server.netty.NettyAcceptor;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by duke on 6/18/14.
 */
public class ExServer {

    private Integer port;

    public ExServer(Integer port) {
        this.port = port;
    }

    private ServerAcceptor m_acceptor;
    SimpleMessaging messaging;

    public void startServer() throws IOException {

        Properties configProps = new Properties();
        configProps.put("host", "0.0.0.0");
        configProps.put("port", this.port.toString());
        configProps.put("password_file","");

        messaging = SimpleMessaging.getInstance();
        messaging.init(configProps);

        m_acceptor = new NettyAcceptor();
        m_acceptor.initialize(messaging, configProps);
    }

    public void stopServer() {
        messaging.stop();
        m_acceptor.close();
    }

}

package org.kevoree.telemetry.server;

import com.sun.org.apache.xalan.internal.lib.ExsltBase;
import org.fusesource.mqtt.client.MQTT;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import org.kevoree.telemetry.server.dashboard.TelemetryDashboardServer;

/**
 * Created by gregory.nain on 03/09/2014.
 */
public abstract class TelemetryServerKernel {


    private static TelemetryTransactionManager transactionManager;

    public static TelemetryTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public static void setTransactionManager(TelemetryTransactionManager transactionManager) {
        TelemetryServerKernel.transactionManager = transactionManager;
    }

    private static MQTT mqttClient;
    private static TelemetryDashboardServer dashboardServer;


    public static MQTT getMqttClient() {
        return mqttClient;
    }

    public static void setMqttClient(MQTT mqttClient) {
        TelemetryServerKernel.mqttClient = mqttClient;
    }

    public static TelemetryDashboardServer getDashboardServer() {
        return dashboardServer;
    }

    public static void setDashboardServer(TelemetryDashboardServer dashboardServer) {
        TelemetryServerKernel.dashboardServer = dashboardServer;
    }


    private static ExServer exServer;

    public static ExServer getExServer() {
        return exServer;
    }

    public static void setExServer(ExServer exServer) {
        TelemetryServerKernel.exServer = exServer;
    }
}

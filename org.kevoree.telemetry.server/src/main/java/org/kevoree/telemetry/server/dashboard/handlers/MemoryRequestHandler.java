package org.kevoree.telemetry.server.dashboard.handlers;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import io.undertow.io.IoCallback;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.time.TimeWalker;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.server.TelemetryServerKernel;
import org.kevoree.telemetry.store.LogTicket;
import org.kevoree.telemetry.store.MemoryInfo;
import org.kevoree.telemetry.store.Node;
import org.kevoree.telemetry.store.TelemetryStore;


/**
 * Created by gregory.nain on 27/08/2014.
 */
public class MemoryRequestHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        //System.out.println("[TopicsRequestHandler] Requested:" + httpServerExchange.getRequestPath());
        if(httpServerExchange.getRequestMethod() == Methods.POST) {

            FormData formData = httpServerExchange.getAttachment(FormDataParser.FORM_DATA);
            if(formData == null) {
                FormDataParser parser = FormParserFactory.builder().build().createParser(httpServerExchange);
                parser.parse(this);
            } else  {
                String path = formData.get("path").getFirst().getValue();
                String type = formData.get("type").getFirst().getValue();
                TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
                TelemetryTimeView view = transaction.time(System.currentTimeMillis() * 1000);

                Node node  = (Node) view.lookup(path);
                if(node != null) {
                    JsonObject response = new JsonObject();
                    response.add("path",node.path());
                    JsonArray ticketsArray = new JsonArray();
                    MemoryInfo memoryInfo;
                    if(type.equals("heap")) {
                        memoryInfo = node.getMemory().getHeapMemory();
                    } else {
                        memoryInfo = node.getMemory().getOffHeapMemory();
                    }
                    int numTickets = 0;
                    while (memoryInfo != null && numTickets < 100) {
                        JsonObject ticket = new JsonObject();
                        ticket.add("init", memoryInfo.getInit());
                        ticket.add("max", memoryInfo.getMax());
                        ticket.add("used", memoryInfo.getUsed());
                        ticket.add("committed", memoryInfo.getCommitted());
                        ticket.add("timestamp", memoryInfo.getNow());
                        ticketsArray.add(ticket);
                        memoryInfo = memoryInfo.previous();
                        numTickets++;
                    }
                    response.add("tickets",ticketsArray);
                    String sending = response.toString();
                    httpServerExchange.getResponseSender().send(sending, IoCallback.END_EXCHANGE);
                }
            }

        }


    }

}

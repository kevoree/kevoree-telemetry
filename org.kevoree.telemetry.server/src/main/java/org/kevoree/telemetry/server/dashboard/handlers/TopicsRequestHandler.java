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
import org.kevoree.telemetry.store.Node;
import org.kevoree.telemetry.store.TelemetryStore;

import java.util.concurrent.TimeUnit;


/**
 * Created by gregory.nain on 27/08/2014.
 */
public class TopicsRequestHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        //System.out.println("[TopicsRequestHandler] Requested:" + httpServerExchange.getRequestPath());
        if(httpServerExchange.getRequestMethod() == Methods.GET) {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
            TelemetryTimeView view = transaction.time(System.currentTimeMillis() * 1000);
            TelemetryStore store = (TelemetryStore) view.lookup("/");

            JsonArray response = new JsonArray();
            JsonObject rootTopic = new JsonObject();
            rootTopic.add("name", "Nodes");
            rootTopic.add("path", "/nodes[*]");
            collectTopics(store, rootTopic);
            response.add(rootTopic);
            String sending = response.toString();
            //Log.debug(sending);
            httpServerExchange.getResponseSender().send(sending, IoCallback.END_EXCHANGE);
        } else if(httpServerExchange.getRequestMethod() == Methods.POST) {

            FormData formData = httpServerExchange.getAttachment(FormDataParser.FORM_DATA);
            if(formData == null) {
                FormDataParser parser = FormParserFactory.builder().build().createParser(httpServerExchange);
                parser.parse(this);
            } else  {
                String path = formData.get("path").getFirst().getValue();
                TelemetryTransaction transaction = TelemetryServerKernel.getTransactionManager().createTransaction();
                TelemetryTimeView view = transaction.time(System.currentTimeMillis() * 1000);

                Node topic  = (Node) view.lookup(path);
                if(topic != null) {
                    JsonObject response = new JsonObject();
                    response.add("path",topic.path());

                    JsonArray ticketsArray = new JsonArray();
                    LogTicket currentTicket = topic.getLog();
                    int numTickets = 0;
                    while (currentTicket != null && numTickets < 100) {
                        JsonObject ticket = new JsonObject();
                        ticket.add("origin", currentTicket.getOrigin());
                        ticket.add("type", currentTicket.getType());
                        ticket.add("message", currentTicket.getMessage());
                        ticket.add("stack", currentTicket.getStack());
                        ticket.add("timestamp", currentTicket.getNow());
                        ticketsArray.add(ticket);
                        currentTicket = currentTicket.previous();
                        numTickets++;
                    }
                    response.add("tickets",ticketsArray);
                    String sending = response.toString();
                    httpServerExchange.getResponseSender().send(sending, IoCallback.END_EXCHANGE);
                }
            }

        }


    }

    private void collectTopics(TelemetryStore store, JsonObject container) {

            JsonArray subs = new JsonArray();
            for(Node node : store.getNodes()) {
                JsonObject topicJson = new JsonObject();
                topicJson.add("name", node.getName());
                topicJson.add("path", node.path());
                LogTicket ticket = node.getLog();
                if(ticket != null) {
                    final int[] counter = new int[1];
                    long firstTimestamp = ticket.timeTree().first();
                    long lastTimestamp = ticket.timeTree().last();
                    ticket.timeTree().walkRangeAsc(new TimeWalker() {
                        @Override
                        public void walk(long l) {
                            counter[0]++;
                        }
                    },firstTimestamp,lastTimestamp);
                    topicJson.add("nbtickets", counter[0]);
                    topicJson.add("oldest", firstTimestamp);
                    topicJson.add("latest", lastTimestamp);
                }
                subs.add(topicJson);
            }
            container.add("nodes", subs);

    }

}

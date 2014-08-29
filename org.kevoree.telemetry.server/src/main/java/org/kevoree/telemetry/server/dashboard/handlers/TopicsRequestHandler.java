package org.kevoree.telemetry.server.dashboard.handlers;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import io.undertow.io.UndertowInputStream;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import jet.runtime.typeinfo.JetValueParameter;
import org.kevoree.log.Log;
import org.kevoree.modeling.api.time.TimeWalker;
import org.kevoree.telemetry.factory.TelemetryTimeView;
import org.kevoree.telemetry.factory.TelemetryTransaction;
import org.kevoree.telemetry.factory.TelemetryTransactionManager;
import store.TelemetryStore;
import store.Ticket;
import store.Topic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by gregory.nain on 27/08/2014.
 */
public class TopicsRequestHandler implements HttpHandler {

    private TelemetryTransactionManager transactionManager;

    public TopicsRequestHandler(TelemetryTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        //System.out.println("[TopicsRequestHandler] Requested:" + httpServerExchange.getRequestPath());
        if(httpServerExchange.getRequestMethod() == Methods.GET) {
            httpServerExchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
            TelemetryTransaction transaction = transactionManager.createTransaction();
            TelemetryTimeView view = transaction.time(System.nanoTime()/1000);
            TelemetryStore store = (TelemetryStore) view.lookup("/");

            JsonArray response = new JsonArray();
            for(Topic t : store.getTopics()) {
                JsonObject rootTopic = new JsonObject();
                rootTopic.add("name", t.getName());
                rootTopic.add("path", t.path());
                collectTopics(t, rootTopic);
                response.add(rootTopic);
            }
            String sending = response.toString();
            //Log.debug(sending);
            httpServerExchange.getResponseSender().send(sending);
        } else if(httpServerExchange.getRequestMethod() == Methods.POST) {

            FormData formData = httpServerExchange.getAttachment(FormDataParser.FORM_DATA);
            if(formData == null) {
                FormDataParser parser = FormParserFactory.builder().build().createParser(httpServerExchange);
                parser.parse(this);
            } else  {
                String path = formData.get("path").getFirst().getValue();
                TelemetryTransaction transaction = transactionManager.createTransaction();
                TelemetryTimeView view = transaction.time(System.nanoTime()/1000);
                Topic topic  = (Topic) view.lookup(path);
                if(topic != null) {
                    JsonObject response = new JsonObject();
                    response.add("path",topic.path());

                    JsonArray ticketsArray = new JsonArray();
                    Ticket currentTicket = topic.getTicket();
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
                    httpServerExchange.getResponseSender().send(sending);
                }
            }

        }


    }

    private void collectTopics(Topic t, JsonObject container) {
        List<Topic> subTopics = t.getTopics();
        if(subTopics.size()>0) {
            JsonArray subs = new JsonArray();
            for(Topic s :subTopics) {
                JsonObject topicJson = new JsonObject();
                topicJson.add("name", s.getName());
                topicJson.add("path", s.path());
                Ticket ticket = s.getTicket();
                if(ticket != null) {
                    final int[] counter = new int[1];
                    long firstTimestamp = ticket.timeTree().first();
                    long lastTimestamp = ticket.timeTree().last();
                    ticket.timeTree().walkRangeAsc(new TimeWalker() {
                        @Override
                        public void walk(@JetValueParameter(name = "timePoint") long l) {
                            counter[0]++;
                        }
                    },firstTimestamp,lastTimestamp);
                    topicJson.add("nbtickets", counter[0]);
                    topicJson.add("oldest", firstTimestamp);
                    topicJson.add("latest", lastTimestamp);
                }
                collectTopics(s, topicJson);
                subs.add(topicJson);
            }
            container.add("subtopics", subs);
        }
    }

    /*
    private static String getStringFrom(HttpServerExchange exchange) {
        InputStream inputStream = new UndertowInputStream(exchange);
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
    */
}

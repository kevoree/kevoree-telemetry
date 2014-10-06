var MqttHandler = function(){

    var client;
    var connectionInProgress = false;

    var init = function() {
        setTimeout(connect, 0);
    };

    var connect = function() {
        client = new WebSocket("ws://telemetry.sntiotlab.lu:9967/mqtt");
        client.onopen = onOpen;
        client.onmessage = onMessage;
        client.onclose = onClose;
        client.onerror = onError;
        connectionInProgress = false;
    };

    var onOpen = function(evt) {
        console.log("onopen", evt);
        var params = {topic:"#"};
        client.send(JSON.stringify(params));
    };

    var onMessage = function(msg) {
        var parsed = JSON.parse(msg.data);
       // console.log("OnMessage:", msg);
        var active = $('.topicsTree').find('.active');
        if(active.length != 0) {
            //console.log("ActiveNode:" + active.attr('data-topic') + " Topic:" + parsed.topic);
            if(parsed.topic.indexOf(active.attr('data-topic').toLowerCase()) == 0) {
                var parsedPayload = JSON.parse(parsed.payload);
                var activeTab = $('#detailsTabs').find("li.active > a").attr('href');
                if(activeTab == "#console") {
                   // console.log("Passing to Console");
                    ConsoleTabHandler.addTicket(parsed.topic, parsedPayload);
                    TopicsHandler.messageReceived(parsed)
                } else if(activeTab == "#memory") {
                    //console.log("Passing to Memory");
                    MemoryTabHandler.addTicket(parsed.topic, parsedPayload);
                } else if(activeTab == "#cpu") {
                    //ConsoleHandler.addTicket(parsedPayload);
                } else {
                    console.log("Not routed. ActiveTab:" + activeTab);
                }
            } else {
                TopicsHandler.messageReceived(parsed)
            }
        }

    };

    var onClose = function(evt) {
        console.log("onClose", evt);
        if(!connectionInProgress) {
            setTimeout(connect, 5000);
            connectionInProgress = true;
        }
    };

    var onError = function(evt) {
        console.log("onError", evt);
        if(!connectionInProgress) {
            setTimeout(connect, 5000);
            connectionInProgress = true;
        }
    };

    return {
        init : init
    }

}();

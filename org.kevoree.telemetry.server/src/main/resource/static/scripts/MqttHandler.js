var MqttHandler = function(){

    var client;
    var connectionInProgress = false;

    var init = function() {
        setTimeout(connect, 0);
    };

    var connect = function() {
        client = new WebSocket("ws://localhost:9967/mqtt");
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
        var active = $('.topicsTree').find('.active');
        if(active != undefined) {
            if(active.attr('data-topic') == parsed.topic) {
                ConsoleHandler.addTicket(JSON.parse(parsed.payload));
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

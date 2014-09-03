
var ConsoleHandler = new function() {

    var replaceContent = function(newContent) {
        var consoleContent = $('div#consoleContents');
        consoleContent.contents().remove();
        for(var i = newContent.length-1 ; i > 0 ;i--) {
            doAddTicket(consoleContent, newContent[i]);
        }
    };

    var doAddTicket = function(container, ticket) {
        var date = new Date(ticket.timestamp/1000);

        var panel = $('<div class="alert" role="alert"><b>From</b> ' + ticket.origin + ' <b>on</b> ' + date.toLocaleDateString()+ ' at ' + date.toLocaleTimeString() + ' :</u><br/>' + ticket.message + ((ticket.hasOwnProperty('stack') && ticket.stack != "")?'<br/>' + ticket.stack : "") + '</div>');

        switch (ticket.type) {
            case "info" :
                panel.addClass('alert-info');
                break;
            case "warn" :
            case "raw_err" :
                panel.addClass('alert-warning');
                break;
            case "error" :
                panel.addClass('alert-danger');
                break;
            case "debug" :
            case "trace" :
                panel.addClass('alert-info');
                break;
            default:
                panel.addClass('alert-info');
        }
        container.prepend(panel);
    };

    var addTicket = function(ticket) {
       doAddTicket($('div#consoleContents'), ticket);
    };

    var init = function() {
        $("#errorFilter").on("click", function() {
            var selector = 'div.alert-danger';
            var consoleContent = $('div#consoleContents');
            consoleContent.find(':not('+selector+')').hide();
            consoleContent.find(selector).show();
        });
        $("#debugFilter").on("click", function() {
            var selector = 'div.alert-danger, div.alert-warning';
            var consoleContent = $('div#consoleContents');
            consoleContent.find(':not('+selector+')').hide();
            consoleContent.find(selector).show();
        });
        $("#infoFilter").on("click", function() {
            var selector = 'div.alert-danger, div.alert-warning, div.alert-info';
            var consoleContent = $('div#consoleContents');
            consoleContent.find(':not('+selector+')').hide();
            consoleContent.find(selector).show();
        });
        $("#noneFilter").on("click", function() {
            var consoleContent = $('div#consoleContents');
            consoleContent.find('div.alert').show();
        });
    };

    return {
        init :init,
        replaceContent : replaceContent,
        addTicket: addTicket
    }

}();
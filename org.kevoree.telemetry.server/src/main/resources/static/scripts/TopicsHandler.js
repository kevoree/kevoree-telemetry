var TopicsHandler = new function() {

    var initialize = function() {
        $.ajax("/data/topics",{type:"GET"})
            .done(function(msg){
                var parent = $(".topicsTree");
                parent.contents().remove();
                refreshTopicsTree(parent, msg);
                registerTreeClickListeners(parent);
            })
            .error(function(err){
                console.log("Failed to collect topics:", err);
            });
    };

    var refreshTopicsTree = function(parent, treeElement) {
        var ulElem = $("<ul></ul>");
        for(var i in treeElement) {
            var e = treeElement[i];
            var liElem = $("<li></li>");
            var textSpan = $("<span>"+e.name+"</span>");
            textSpan.attr('data-path', e.path);
            var parentSpan = parent.find('span[data-topic]');
            if(parentSpan.length != 0) {
                textSpan.attr('data-topic', parentSpan.attr('data-topic') + "/" + e.name.toLowerCase());
            } else {
                textSpan.attr('data-topic', e.name.toLowerCase());
            }
            textSpan.appendTo(liElem);
            if(e.hasOwnProperty('nbtickets')) {
                textSpan.addClass("detailable");
                $("<span class=\"badge pullright\">" + e.nbtickets + "</span>").appendTo(textSpan);
            }
            if(e.nodes !== 'undefined') {
                refreshTopicsTree(liElem, e.nodes);
            }
            liElem.appendTo(ulElem);
        }
        ulElem.appendTo(parent);
    };

    var registerTreeClickListeners = function(tree) {
        tree.find('li:has(ul)').addClass('parent_li').find(' > span').attr('title', 'Collapse this branch');
        tree.find('li.parent_li > span').on('click', function (e) {
            var children = $(this).parent('li.parent_li').find(' > ul > li');
            if (children.is(":visible")) {
                children.hide('fast');
                $(this).attr('title', 'Expand this branch').find(' > i').addClass('icon-plus-sign').removeClass('icon-minus-sign');
            } else {
                children.show('fast');
                $(this).attr('title', 'Collapse this branch').find(' > i').addClass('icon-minus-sign').removeClass('icon-plus-sign');
            }
            e.stopPropagation();
        });

        tree.find('.detailable').on('click', function(){
            updateDetails(this);
            tree.find('.active').removeClass('active');
            $(this).addClass('active');
        });
    };

    var updateDetails = function(node) {
        var pathV = $(node).attr('data-path');
        $.post("/data/topics",{path:pathV})
            .done(function(msg){
                var parsed = JSON.parse(msg);
                if(parsed.hasOwnProperty('tickets')) {
                    ConsoleHandler.replaceContent(parsed.tickets);
                }
            })
            .error(function(err){
                console.log("Failed to collect details:", err);
            });
    };


    var messageReceived = function(parsed) {
        var topics = parsed.topic.split("/");
        var currentTopic = topics[0];
        if(currentTopic.length == 2) {
            var topicTree = $(".topicsTree");
            var ul = topicTree.find("ul").first();
            if(ul.length == 0) {
                ul = $('<ul></ul>');
                ul.appendTo(topicTree);
            }
            for(var i = 0; i < topics.length; i++) {
                if(i==0) {
                    currentTopic = topics[i];
                } else {
                    currentTopic = currentTopic + "/" + topics[i];
                }
                var li = ul.find('li > span[data-topic="'+currentTopic+'"]').parent("li");
                if(li.length == 0) {
                    li = $("<li></li>");
                    li.appendTo(ul);
                    var textSpan = $("<span>"+topics[i]+"</span>");
                    if(i == topics.length -1) {
                        textSpan.attr('data-path', parsed.path.substring(0,parsed.path.lastIndexOf("/")));
                    }
                    textSpan.attr('data-topic', currentTopic);
                    textSpan.appendTo(li);
                    ul = $('<ul></ul>');
                    ul.appendTo(li);
                    if(i == topics.length-1) {
                        textSpan.addClass("detailable");
                        var span = $("<span class=\"badge pullright\">0</span>");
                        span.appendTo(textSpan);
                        registerTreeClickListeners(topicTree);
                    }
                }
                if(i == topics.length-1) {
                    var badge = li.find("span.badge");
                    badge.text(parseInt(badge.text())+1);
                } else {
                    ul = li.find("ul").first();
                    if(ul.length == 0) {
                        ul = $('<ul></ul>');
                        ul.appendTo(li);
                    }
                }
            }
        } else {
            console.log("Message Received:", parsed)
        }
    };


    return {
        init : initialize,
        messageReceived : messageReceived
    };


}();
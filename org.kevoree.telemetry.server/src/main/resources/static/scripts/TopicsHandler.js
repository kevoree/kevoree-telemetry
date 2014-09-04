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
            var rootTopic = treeElement[i];

            if(rootTopic.name.toLowerCase() == "nodes") {

                var liRootElem = $("<li></li>");
                var textSpanRoot = $("<span>"+rootTopic.name+"</span>");
                textSpanRoot.attr('data-path', rootTopic.path);
                textSpanRoot.attr('data-topic', rootTopic.name.toLowerCase());

                textSpanRoot.appendTo(liRootElem);

                var nodeListElem = $("<ul></ul>");
                nodeListElem.appendTo(liRootElem);

                for(var j in rootTopic.nodes) {
                    var node = rootTopic.nodes[j];
                    var liNodeElem = $("<li></li>");
                    var textSpanNode = $("<span>"+node.name+"</span>");
                    textSpanNode.attr('data-path', node.path);
                    textSpanNode.attr('data-topic', "nodes/" + node.name.toLowerCase());
                    textSpanNode.addClass("detailable");
                    $(" <span class=\"badge pullright\" style=\"margin-left:10px;\">" + node.nbtickets + "</span>").appendTo(textSpanNode);
                    textSpanNode.appendTo(liNodeElem);

                    var shadowChildListElem = $("<ul></ul>");
                    shadowChildListElem.appendTo(liNodeElem);

                    liNodeElem.appendTo(nodeListElem);
                }
                liRootElem.appendTo(ulElem);
            } else {
                console.log("RootTopic unknown:" + rootTopic, treeElement);
            }
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
        var rootTopic = topics[0];

        if(rootTopic == "nodes") {
            var nodeName = topics[1];
            var topicTree = $(".topicsTree");
            var ul = topicTree.find("ul").first();
            if(ul.length == 0) {
                ul = $('<ul></ul>');
                ul.appendTo(topicTree);
            }

            var li = ul.find('li > span[data-topic="'+rootTopic + "/" + nodeName+'"]').parent("li");
            if(li.length == 0) {
                li = $("<li></li>");
                li.appendTo(ul);
                var textSpan = $("<span>"+nodeName+"</span>");
                textSpan.attr('data-path', "/nodes["+nodeName+"]");
                textSpan.attr('data-topic', rootTopic + "/" + nodeName);
                textSpan.appendTo(li);
                ul = $('<ul></ul>');
                ul.appendTo(li);
                textSpan.addClass("detailable");
                var span = $("<span class=\"badge pullright\" style=\"margin-left:10px;\">0</span>");
                span.appendTo(textSpan);
                ul = $('<ul></ul>');
                ul.appendTo(li);
                registerTreeClickListeners(topicTree);
            }
            var badge = li.find("span.badge");
            badge.text(parseInt(badge.text())+1);

        } else {
            console.log("Unknown root topic:" + parsed.topic);
        }

    };


    return {
        init : initialize,
        messageReceived : messageReceived
    };


}();
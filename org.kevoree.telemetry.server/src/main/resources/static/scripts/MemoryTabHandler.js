
var MemoryTabHandler = new function() {

    var gaugeOptions = {
        chart: { type: 'solidgauge' },
        title: null,
        pane: {
            center: ['50%', '50%'],
            size: '100%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },

        tooltip: {
            enabled: false
        },

        // the value axis
        yAxis: {
            stops: [
                [0.1, '#55BF3B'], // green
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#DF5353'] // red
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickPixelInterval: 400,
            tickWidth: 0,
            title: {
                y: -70
            },
            labels: {
                y: 16
            }
        },

        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        }
    };




    var replaceContent = function(newContent) {
        /*
         var consoleContent = $('div#consoleContents');
         consoleContent.contents().remove();
         for(var i = newContent.length-1 ; i > 0 ;i--) {
         doAddTicket(consoleContent, newContent[i]);
         }
         */
    };

    var refreshInstantValues = function(container, ticket) {
        var heapGauge = $('#heapGauge').highcharts();
        if (heapGauge) {
            var point = heapGauge.series[0].points[0];
            var newMax = parseFloat((ticket.message.heapMemory.committed / 1024 / 1024).toFixed(2));
            heapGauge.yAxis[0].update({max:newMax});
            var newVal = parseFloat((ticket.message.heapMemory.used / 1024 / 1024).toFixed(2));
            point.update(newVal);
        }

        var heapHistory = $('#heapHistory').highcharts();
        if (heapHistory) {
            heapHistory.series[0].addPoint([ticket.timestamp / 1000, parseFloat((ticket.message.heapMemory.used / 1024 / 1024).toFixed(2))], true, true)
        }

        var offHeapGauge = $('#offHeapGauge').highcharts();
        if (offHeapGauge) {
            point = offHeapGauge.series[0].points[0];
            var newMax = parseFloat((ticket.message.offHeapMemory.committed / 1024 / 1024).toFixed(2));
            offHeapGauge.yAxis[0].update({max:newMax});
            var newVal = parseFloat((ticket.message.offHeapMemory.used / 1024 / 1024).toFixed(2));
            point.update(newVal);

        }
        var offHeapHistory = $('#offHeapHistory').highcharts();
        if (offHeapHistory) {
            offHeapHistory.series[0].addPoint([ticket.timestamp / 1000, parseFloat((ticket.message.offHeapMemory.used / 1024 / 1024).toFixed(2))], true, true)
        }


    };

    var doAddTicket = function(container, topic, ticket) {

        if(topic.indexOf("/runtime/memory") != -1) {
            var date = new Date(ticket.timestamp/1000);

            if(ticket.type == "MemoryBeanInfo") {
                refreshInstantValues(container.find('#instantMemoryPanel').first(), ticket);
            } else {
                console.log("Ticket Type not recognized", ticket)
            }
        }
    };

    var addTicket = function(topic, ticket) {
        doAddTicket($('div#memoryContents'), topic, ticket);
    };


    var setupHeapLine = function() {
        //setting gauge up
        $('#heapGauge').highcharts(Highcharts.merge(gaugeOptions, {
            yAxis: {
                min: 0,
                max: 200,
                title: {
                    text: 'UsedHeap'
                }
            },

            credits: {
                enabled: false
            },

            series: [{
                name: 'UsedHeap',
                data: [0],
                dataLabels: {
                    format: '<div style="text-align:center"><span style="font-size:25px;color:' +
                        ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y}</span><br/>' +
                        '<span style="font-size:12px;color:silver">MB</span></div>'
                },
                tooltip: {
                    valueSuffix: ' MB'
                }
            }]

        }));

        //setting graph up
        var heapHistory = $('#heapHistory').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events:{
                    load : function() {
                        var data =  [];
                        var series = this.series[0];
                        $.post("/data/memory",{path:$(".active").attr('data-path'), type:"heap"})
                            .done(function(msg){
                                var parsed = JSON.parse(msg);
                                //console.log("Received Parsed", parsed);
                                if(parsed.hasOwnProperty('tickets')) {
                                    var elements = parsed.tickets.reverse();
                                    for(var i in elements) {
                                        var ticket = elements[i];
                                        data.push([ticket.timestamp / 1000, parseFloat((ticket.used / 1024 / 1024).toFixed(2))]);
                                    }
                                    series.setData(data);
                                }
                            })
                            .error(function(err){
                                console.log("Failed to collect details:", err);
                            });
                    }
                }
            },
            title: {
                text: 'Heap Memory Used'
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'MB'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Heap Memory Used'
            }]
        });
    };

    var setupOffHeapLine = function() {
        //setting gauge up
        $('#offHeapGauge').highcharts(Highcharts.merge(gaugeOptions, {
            yAxis: {
                min: 0,
                max: 200,
                title: {
                    text: 'UsedOffHeap'
                }
            },

            credits: {
                enabled: false
            },

            series: [{
                name: 'UsedOffHeap',
                data: [0],
                dataLabels: {
                    format: '<div style="text-align:center"><span style="font-size:25px;color:' +
                        ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">{y}</span><br/>' +
                        '<span style="font-size:12px;color:silver">MB</span></div>'
                },
                tooltip: {
                    valueSuffix: ' MB'
                }
            }]

        }));

        //setting graph up
        $('#offHeapHistory').highcharts({
            chart: {
                type: 'spline',
                animation: Highcharts.svg, // don't animate in old IE
                marginRight: 10,
                events: {
                    load: function () {
                        var data =  [];
                        var series = this.series[0];
                        $.post("/data/memory",{path:$(".active").attr('data-path'), type:"offHeap"})
                            .done(function(msg){
                                var parsed = JSON.parse(msg);
                                if(parsed.hasOwnProperty('tickets')) {
                                    var elements = parsed.tickets.reverse();
                                    for(var i in elements) {
                                        var ticket = elements[i];
                                        data.push([ticket.timestamp / 1000, parseFloat((ticket.used / 1024 / 1024).toFixed(2))]);
                                    }
                                    series.setData(data);
                                }
                            })
                            .error(function(err){
                                console.log("Failed to collect details:", err);
                            });
                    }
                }
            },
            title: {
                text: 'Off-Heap memory used'
            },
            xAxis: {
                type: 'datetime',
                tickPixelInterval: 150
            },
            yAxis: {
                title: {
                    text: 'MB'
                },
                plotLines: [{
                    value: 0,
                    width: 1,
                    color: '#808080'
                }]
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.series.name + '</b><br/>' +
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) + '<br/>' +
                        Highcharts.numberFormat(this.y, 2);
                }
            },
            legend: {
                enabled: false
            },
            exporting: {
                enabled: false
            },
            series: [{
                name: 'Off-Heap Memory Used'
            }]
        });
    };


    var init = function() {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
        setupHeapLine();
        setupOffHeapLine();
    };

    return {
        init :init,
        replaceContent : replaceContent,
        addTicket: addTicket
    }

}();
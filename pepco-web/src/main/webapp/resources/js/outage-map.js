$(document).ready(function(){
    markers=[];
    function createMarkers(data){
        for (i in data.outages){
            var marker=new google.maps.Marker({
                position:new google.maps.LatLng(data.outages[i].lat,data.outages[i].lon),
                title:"Outage",
                map:map
            });
            markers.push(marker);
            var customersAffected=parseInt(data.outages[i].numCustomersOut,10);
            if(customersAffected == 0)customersAffected="1-5";
            var infoString="<dl>";
            infoString=infoString+"<dt>Customers Affected:</dt><dd>"+customersAffected+"</dd>";
            infoString=infoString+"<dt>Estimated Restoration:</dt><dd>"+data.outages[i].estimatedRestoration+"</dd>";
            infoString=infoString+"<dt>Cause:</dt><dd>"+data.outages[i].cause+"</dd>";
            infoString=infoString+"<dt>Status:</dt><dd>"+data.outages[i].status+"</dd>";
            infoString=infoString+"</dl>";
            var infowindow=new google.maps.InfoWindow({
                content:infoString
            });
            //Closure
            var createEvent = function(marker, infowindow){
                google.maps.event.addListener(marker,'click',function(){
                    infowindow.open(map,marker);
                });
            };
            createEvent(marker,infowindow);
        }
    }
    $("#outage-map").before($("<input id='outagesAsof'/>"));
    $("#outagesAsof").datetimepicker();
    $("#ui-datepicker-div").css("font-size",".8em");
    $("#outagesAsof").change(function(){
        
    });
    
    var map = new google.maps.Map(document.getElementById("outage-map"),{
        zoom:8,
        center:new google.maps.LatLng(38.96, -77.03),
        mapTypeId:google.maps.MapTypeId.ROADMAP
    });
    
    if(window.location.toString().substr(window.location.toString().length-1)!="/")
        outagesUrl=window.location.toString()+"/outages.json";
    else
        outagesUrl=window.location.toString()+"outages.json";
    function loadOutages(asof){
        for (i in markers){markers[i].setMap(null);}
        markers=[];
        $.get(outagesUrl,
            {"asof":asof},
            createMarkers,
            "json"
        );
    }
    loadOutages(new Date().toString("yyyyMMdd.HHmmss"));
    $("#outagesAsof").change(loadOutages);
});
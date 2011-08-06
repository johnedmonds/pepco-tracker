//Copied from http://www.mredkj.com/javascript/numberFormat.html
function addCommas(nStr) {
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}
function makeChart(dataset) {
	$.plot($("#reliability-chart-placeholder"), dataset, {
		series : {
			stack : true,
			lines : {
				show : true,
				fill : true
			}
		},
		xaxis : {
			show : true,
			mode : "time",
			timeformat : "%y-%m-%d",
		},
		yaxis : {
			show : true,
			tickFormatter : addCommas
		}
	});
	$("#reliability-chart-placeholder").before($('<div id="yAxisLabel" class="axisLabel" style="width: 100px; height: 200px; float:left;position:relative;"><p style="position:absolute;top:25%;">Customers Affected</p></div>'));
	$(".tickLabels").append("<div class=\"tickLabel axisLabel\" id=\"xAxisLabel\">Time</div>");
}

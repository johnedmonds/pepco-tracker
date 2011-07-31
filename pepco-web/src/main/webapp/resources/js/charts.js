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
	$(".tickLabels").append("<div class=\"tickLabel xAxisLabel\">Time</div>");
}

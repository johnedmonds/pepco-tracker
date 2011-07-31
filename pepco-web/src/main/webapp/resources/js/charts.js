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
			mode : "time",
			timeformat : "%y-%m-%d"
		}
	});
	$(".tickLabels").append("<div class=\"tickLabel xAxisLabel\">Time</div>");
}

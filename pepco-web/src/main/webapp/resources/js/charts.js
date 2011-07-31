function makeChart(dataset) {
	$.plot($("#reliability-chart"), dataset, {
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
}


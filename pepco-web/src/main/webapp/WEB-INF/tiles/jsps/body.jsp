<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<script type="text/javascript"
	src="<c:url value="/resources/js/excanvas.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/resources/js/jquery.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/resources/js/jquery.flot.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/resources/js/jquery.flot.stack.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/resources/js/charts.js"/>"></script>
<script type="text/javascript">
<!--
	$(document).ready(
			function() {
				// Don't need the image since people with javascript can use our flot
				// charts.
				$("#reliability-chart-img").remove();
				$.getJSON('<c:url value="/summary-data"/>', null, function(
						dataset, textStatus) {
					makeChart(dataset);
				});
			});
//-->
</script>
<div id="outageSummary">
	<h1>Outage Summary</h1>
	<p>
		As of
		<fmt:formatDate value="${summary.whenGenerated}"
			pattern="yyyy-MM-dd h:mm a" />
		there are: <em class="summaryCount"> <fmt:formatNumber
				value="${summary.totalOutages}" />
		</em> total outages in Pepco's service area affecting <em
			class="summaryCount"> <fmt:formatNumber
				value="${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}" />
		</em> customers. Of those
		<fmt:formatNumber
			value="${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}" />
		customers, <em class="summaryCount"> <fmt:formatNumber
				value="${summary.dcAffectedCustomers}" />
		</em> are in DC, <em class="summaryCount"> <fmt:formatNumber
				value="${summary.pgAffectedCustomers}" />
		</em> are in Prince George County, and <em class="summaryCount"> <fmt:formatNumber
				value="${summary.montAffectedCustomers}" />
		</em> are in Montgomery County.
	</p>

</div>
<div id="reliability-chart" style="width: 1000px; height: 200px;">
	<h2>Customer Outages Over Time By Area</h2>
	<noscript>
		<div>
			<img id="reliability-chart-img" alt="Pepco Reliability Statistics"
				src="<c:url value="/resources/img/generated/reliability.png"/>">
		</div>
	</noscript>
	<div id="reliability-chart-placeholder"
		style="width: 900px; height: 200px; float: left;"></div>
</div>
<a href="<c:url value="/summary.csv"/>" id="summary-csv-link">Chart Source Data (csv)</a>
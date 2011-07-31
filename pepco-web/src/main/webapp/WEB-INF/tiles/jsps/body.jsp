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
	As of
	<fmt:formatDate value="${summary.whenGenerated}"
		pattern="yyyy-MM-dd h:mm a" />
	there are: <br />
	<p>
		<emph class="summaryCount"> <fmt:formatNumber
			value="${summary.totalOutages}" /></emph>
		total outages in Pepco's service area affecting
		<emph class="summaryCount"> <fmt:formatNumber
			value="${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}" /></emph>
		customers. Of those
		<fmt:formatNumber
			value="${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}" />
		customers,
		<emph class="summaryCount"> <fmt:formatNumber
			value="${summary.dcAffectedCustomers}" /></emph>
		are in DC,
		<emph class="summaryCount"> <fmt:formatNumber
			value="${summary.pgAffectedCustomers}" /></emph>
		are in Prince George County , and
		<emph class="summaryCount"> <fmt:formatNumber
			value="${summary.montAffectedCustomers}" /></emph>
		are in Montgomery County.
	</p>

</div>
<div id="reliability-chart">
	<h2>Customer Outages Over Time By Area</h2>
	<noscript>
		<img id="reliability-chart-img" alt="Pepco Reliability Statistics"
			src="<c:url value="/resources/img/generated/reliability.png"/>">
	</noscript>
	<div id="reliability-chart-placeholder"
		style="width: 1000px; height: 200px;"></div>
</div>
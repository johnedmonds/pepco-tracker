<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
	<table id="outageSummaryTable">
		<tr>
			<td>Total Outages</td>
			<td><c:out value="${summary.totalOutages}" /></td>
		</tr>
		<tr>
			<td>Total Affected Customers</td>
			<td>${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}<span
				class="summaryTotal"> /
					${summary.dcTotalCustomers+summary.pgTotalCustomers+summary.montTotalCustomers}</span>
			</td>
		</tr>
		<tr>
			<td>DC Customers Affected Customers</td>
			<td>${summary.dcAffectedCustomers}<span class="summaryTotal">
					/ ${summary.dcTotalCustomers}</span></td>
		</tr>
		<tr>
			<td>Prince George Affected Customers</td>
			<td>${summary.pgAffectedCustomers}<span class="summaryTotal">
					/ ${summary.pgTotalCustomers}</span></td>
		</tr>
		<tr>
			<td>Montgomery Affected Customers</td>
			<td>${summary.montAffectedCustomers}<span class="summaryTotal">
					/ ${summary.montTotalCustomers}</span></td>
		</tr>
	</table>
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
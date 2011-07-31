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
	<span id="asOf">As of <fmt:formatDate value="${summary.whenGenerated}" pattern="yyyy-MM-dd hh:mm:ss a"/></span>
	<table id="outageSummaryTable">
		<tr>
			<td><emph class="summaryCount"> <fmt:formatNumber
					value="${summary.totalOutages}" /></emph></td>
			<td>Total outages in Pepco's service area.</td>
		</tr>
		<tr>
			<td><emph class="summaryCount"> <fmt:formatNumber
					value="${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}" /></emph>
			</td>
			<td>Pepco customers that are affected out of <fmt:formatNumber
					value="${summary.dcTotalCustomers+summary.pgTotalCustomers+summary.montTotalCustomers}" />
				total customers.</span>
			</td>
		</tr>
		<tr>
			<td><div style="height: 20px;"></div></td>
			<td></td>
		</tr>
		<tr>
			<td><emph class="summaryCount"> <fmt:formatNumber
					value="${summary.dcAffectedCustomers}" /></emph></td>
			<td>DC Customers affected out of <fmt:formatNumber
					value="${summary.dcTotalCustomers}" /> total DC customers.</td>
		</tr>
		<tr>
			<td><emph class="summaryCount"> <fmt:formatNumber
					value="${summary.pgAffectedCustomers}" /></emph></td>
			<td>Prince George customers affected out of <fmt:formatNumber
					value="${summary.pgTotalCustomers}" /> total Prince George
				customers.</td>
		</tr>
		<tr>
			<td><emph class="summaryCount">
				<fmt:formatNumber value="${summary.montAffectedCustomers}" /></emph></td>
			<td>Montgomery customers affected out of <fmt:formatNumber
					value="${summary.montTotalCustomers}" /> total Montgomery
				customers.</td>
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
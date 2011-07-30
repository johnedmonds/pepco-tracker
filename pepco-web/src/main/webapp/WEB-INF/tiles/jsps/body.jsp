<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<h1>Outage Summary</h1>
<div id="outageSummary">
	<table>
		<tr>
			<td>Total Outages</td>
			<td><c:out value="${summary.totalOutages}" />
			</td>
		</tr>
		<tr>
			<td>Total Affected Customers</td>
			<td>${summary.dcAffectedCustomers+summary.pgAffectedCustomers+summary.montAffectedCustomers}<span
				class="summaryTotal"> / ${summary.dcTotalCustomers+summary.pgTotalCustomers+summary.montTotalCustomers}</span>
			</td>
		</tr>
		<tr>
			<td>DC Customers Affected Customers</td>
			<td>${summary.dcAffectedCustomers}<span class="summaryTotal"> / ${summary.dcTotalCustomers}</span>
			</td>
		</tr>
		<tr>
			<td>Prince George Affected Customers</td>
			<td>${summary.pgAffectedCustomers}<span class="summaryTotal"> / ${summary.pgTotalCustomers}</span>
			</td>
		</tr>
		<tr>
			<td>Montgomery Affected Customers</td>
			<td>${summary.montAffectedCustomers}<span class="summaryTotal"> / ${summary.montTotalCustomers}</span>
			</td>
		</tr>
	</table>
	<img alt="Pepco Reliability Statistics"
		src="<c:url value="/resources/img/generated/reliability.png"/>">
</div>

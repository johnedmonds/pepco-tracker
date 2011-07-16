<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<h1>Outage Summary</h1>
<dl>
	<dt>Total Outages</dt>
	<dd>${totalOutages }</dd>
	<dt>Affected Customers</dt>
	<dd>
		<dl>
			<dt>DC</dt>
			<dd>${dcCust}</dd>
			<dt>Prince George</dt>
			<dd>${pgCust}</dd>
			<dt>Montgomery</dt>
			<dd>${pgCust}</dd>
			<dt>Total</dt>
			<dd>${dcCust+pgCust+montCust}</dd>
		</dl>
	</dd>
</dl>
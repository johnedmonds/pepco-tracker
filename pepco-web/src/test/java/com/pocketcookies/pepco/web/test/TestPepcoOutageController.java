package com.pocketcookies.pepco.web.test;

import java.sql.Timestamp;

import junit.framework.TestCase;

import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.web.PepcoOutageController.OutageRevisionWrapper;

public class TestPepcoOutageController extends TestCase {
	public void testCreateOutageRevisionWrapper() {
		assertEquals(new OutageRevisionWrapper(4, 5, 6, 7, 1, 2, "cause",
				"Pending"), new OutageRevisionWrapper(new OutageRevision(1,
				new Timestamp(2), new Timestamp(3), new Outage(4, 5,
						new Timestamp(6), new Timestamp(7)), null, "cause",
				CrewStatus.PENDING)));
		assertEquals(new OutageRevisionWrapper(1, 2, 3, -1, 4, -1, "cause",
				"Pending"), new OutageRevisionWrapper(new OutageRevision(4,
				null, null, new Outage(1, 2, new Timestamp(3), null), null,
				"cause", CrewStatus.PENDING)));
		assertEquals(new OutageRevisionWrapper(4, 5, 6, 7, 1, 2, "cause",
				"Assigned"), new OutageRevisionWrapper(new OutageRevision(1,
				new Timestamp(2), new Timestamp(3), new Outage(4, 5,
						new Timestamp(6), new Timestamp(7)), null, "cause",
				CrewStatus.ASSIGNED)));
		assertEquals(new OutageRevisionWrapper(4, 5, 6, 7, 1, 2, "cause",
				"En Route"), new OutageRevisionWrapper(new OutageRevision(1,
				new Timestamp(2), new Timestamp(3), new Outage(4, 5,
						new Timestamp(6), new Timestamp(7)), null, "cause",
				CrewStatus.EN_ROUTE)));
		assertEquals(new OutageRevisionWrapper(4, 5, 6, 7, 1, 2, "cause",
				"On Site"), new OutageRevisionWrapper(new OutageRevision(1,
				new Timestamp(2), new Timestamp(3), new Outage(4, 5,
						new Timestamp(6), new Timestamp(7)), null, "cause",
				CrewStatus.ON_SITE)));
	}
}

package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;

public class OutageRevisionCombinerTest {
	@Test
	public void testCombineCluster() {
		final Outage o1 = new Outage(1, 1, new Timestamp(1), new Timestamp(2));
		final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
		final OutageClusterRevision r1 = new OutageClusterRevision(1,
				new Timestamp(3), o1, run, 1, 2, 2);
		final OutageClusterRevision r2 = new OutageClusterRevision(1,
				new Timestamp(3), o1, run, 1, 1, 1);
		final OutageClusterRevision r3 = new OutageClusterRevision(1,
				new Timestamp(3), o1, run, 1, 3, 3);
		final OutageRevisionCombiner combiner = new OutageRevisionCombiner();
		combiner.addAll(ImmutableList.<AbstractOutageRevision> of(r1, r2, r3));
		final OutageClusterRevision combinedRevision = (OutageClusterRevision) Iterables
				.getOnlyElement(combiner.getCombinedRevisions());
		assertEquals(1, combinedRevision.getFirstSeenZoomLevel());
		assertEquals(3, combinedRevision.getLastSeenZoomLevel());
	}

	@Test
	public void testCombineOutage() {
		final Outage o1 = new Outage(1, 1, new Timestamp(1), new Timestamp(2));
		final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
		final OutageRevision r1 = new OutageRevision(1, new Timestamp(3), o1,
				run, "test", CrewStatus.PENDING, 2);
		final OutageRevision r2 = new OutageRevision(1, new Timestamp(3), o1,
				run, "test", CrewStatus.PENDING, 1);
		final OutageRevision r3 = new OutageRevision(1, new Timestamp(3), o1,
				run, "test", CrewStatus.PENDING, 3);
		final OutageRevisionCombiner combiner = new OutageRevisionCombiner();
		combiner.addAll(ImmutableList.<AbstractOutageRevision> of(r1, r2, r3));
		final OutageRevision combinedRevision = (OutageRevision) Iterables
				.getOnlyElement(combiner.getCombinedRevisions());
		assertEquals(1, combinedRevision.getFirstSeenZoomLevel());
	}
}

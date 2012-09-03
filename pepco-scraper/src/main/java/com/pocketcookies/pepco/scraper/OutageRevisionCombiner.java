package com.pocketcookies.pepco.scraper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableSet;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageClusterRevision;

/**
 * Given outage revisions, combines them (basically using the zoom level). This
 * is usually used to combine outages as we are loading them.
 * 
 * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
 */
public class OutageRevisionCombiner {
	private static class AbstractOutageRevisionWrapper {
		private final AbstractOutageRevision revision;

		public AbstractOutageRevisionWrapper(
				final AbstractOutageRevision revision) {
			this.revision = revision;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder()
					.append(revision.getNumCustomersAffected())
					.append(revision.getEstimatedRestoration())
					.append(revision.getOutage()).toHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof AbstractOutageRevisionWrapper) {
				AbstractOutageRevision otherRevision = ((AbstractOutageRevisionWrapper) o).revision;
				return new EqualsBuilder()
						.append(revision.getNumCustomersAffected(),
								otherRevision.getNumCustomersAffected())
						.append(revision.getEstimatedRestoration(),
								otherRevision.getEstimatedRestoration())
						.append(revision.getOutage(), otherRevision.getOutage())
						.isEquals();
			}
			return false;
		}
	}

	private Map<AbstractOutageRevisionWrapper, AbstractOutageRevision> revisions = new HashMap<OutageRevisionCombiner.AbstractOutageRevisionWrapper, AbstractOutageRevision>();

	public void add(AbstractOutageRevision revision) {
		AbstractOutageRevisionWrapper wrapper = new AbstractOutageRevisionWrapper(
				revision);
		if (revisions.containsKey(wrapper)) {
			AbstractOutageRevision existingRevision = revisions.get(wrapper);
			existingRevision.setFirstSeenZoomLevel(Math.min(
					existingRevision.getFirstSeenZoomLevel(),
					revision.getFirstSeenZoomLevel()));
			// Merge in the changes from the given revision.
			if (existingRevision instanceof OutageClusterRevision) {
				assert revision instanceof OutageClusterRevision;
				OutageClusterRevision existingClusterRevision = (OutageClusterRevision) existingRevision;
				existingClusterRevision.setLastSeenZoomLevel(Math.max(
						existingClusterRevision.getLastSeenZoomLevel(),
						((OutageClusterRevision) revision)
								.getLastSeenZoomLevel()));
			}
		} else {
			revisions.put(wrapper, revision);
		}
	}

	public void addAll(Iterable<AbstractOutageRevision> revisions) {
		for (AbstractOutageRevision revision : revisions) {
			add(revision);
		}
	}

	public Set<AbstractOutageRevision> getCombinedRevisions() {
		return ImmutableSet.copyOf(revisions.values());
	}
}

package com.pocketcookies.pepco.model.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.AbstractOutage;

public class OutageDAO {
	private final SessionFactory sessionFactory;

	public OutageDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public AbstractOutage getActiveOutage(final double lat, final double lon,
			final Class<? extends AbstractOutage> outageType) {

		final String outageClass = outageType == null ? AbstractOutage.class
				.getName() : outageType.getName();
		final List<AbstractOutage> outages;
		outages = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from "
								+ outageClass
								+ " where lat=:lat and lon=:lon and observedEnd=null")
				.setDouble("lat", lat).setDouble("lon", lon).list();
		if (outages.isEmpty())
			return null;
		return outages.get(0);
	}
}

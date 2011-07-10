package com.pocketcookies.pepco.model.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.Outage;

public class OutageDAO {
	private final SessionFactory sessionFactory;

	public OutageDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public Outage getActiveOutage(final double lat, final double lon) {

		final List<Outage> outages;
		outages = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Outage where lat=:lat and lon=:lon and observedEnd=null")
				.setDouble("lat", lat).setDouble("lon", lon).list();
		if (outages.isEmpty())
			return null;
		return outages.get(0);
	}
}

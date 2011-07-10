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

		final List<AbstractOutage> outages;
		if (outageType == null)
			outages = this.sessionFactory
					.getCurrentSession()
					.createQuery(
							"from AbstractOutage where lat=:lat and lon=:lon and observedEnd=null")
					.setDouble("lat", lat).setDouble("lon", lon).list();
		else
			outages = this.sessionFactory
					.getCurrentSession()
					.createQuery(
							"from AbstractOutage where lat=:lat and lon=:lon and observedEnd=null and outageType=:outageType")
					.setDouble("lat", lat).setDouble("lon", lon)
					.setString("outageType", outageType.getName()).list();
		if (outages.isEmpty())
			return null;
		return outages.get(0);
	}
}

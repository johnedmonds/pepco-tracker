package com.pocketcookies.pepco.model.dao;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.Summary;

public class SummaryDAO {

	private SessionFactory sessionFactory;

	protected SummaryDAO() {
	}

	public SummaryDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	// Used by spring.
	@SuppressWarnings("unused")
	private void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void saveSummary(final Summary summary) {
		this.sessionFactory.getCurrentSession().save(summary);
	}

}

package com.pocketcookies.pepco.model.dao;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pocketcookies.pepco.model.ParserRun;

@Service
public class ParserRunDao {
    private final SessionFactory sessionFactory;

    @Inject
    public ParserRunDao(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Used by Spring.  Do not use this constructor!
     */
    protected ParserRunDao() {
        sessionFactory = null;
    }

    @Transactional
    public void saveParserRun(ParserRun run){
        sessionFactory.getCurrentSession().save(run);
    }
}

package com.pocketcookies.pepco.model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 *
 * @author jack
 */
public class SessionFactoryLoader {

    public static SessionFactory loadSessionFactory() {
        return new AnnotationConfiguration()
                .configure("hibernate-mappings.cfg.xml")
                .configure("hibernate.ds.cfg.xml")
                .buildSessionFactory();
    }
}

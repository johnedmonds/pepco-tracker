package com.pocketcookies.pepco.scraper;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringScraperConfiguration {
    @Bean
    public StormCenterLoader createStormCenterLoader() {
        return new StormCenterLoader(new DefaultHttpClient(
                new PoolingClientConnectionManager()));
    }
}

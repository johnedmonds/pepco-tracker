package com.pocketcookies.pepco.scraper;

import com.pocketcookies.pepco.model.ParserRun;

public interface Scraper {
    public void scrape(ParserRun run) throws Exception;
}

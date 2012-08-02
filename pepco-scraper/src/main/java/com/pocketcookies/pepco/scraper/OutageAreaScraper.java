package com.pocketcookies.pepco.scraper;

import java.io.IOException;

import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pocketcookies.pepco.model.OutageArea;
import com.pocketcookies.pepco.model.OutageAreaRevision;
import com.pocketcookies.pepco.model.ParserRun;
import com.pocketcookies.pepco.model.dao.OutageAreaDAO;

@Service
public class OutageAreaScraper implements Scraper {
    private static final String THEMATIC_SUFFIX = "thematic/current/thematic_areas.xml";

    private final OutageAreaDAO dao;
    private final StormCenterLoader stormCenterLoader;

    @Inject
    public OutageAreaScraper(final OutageAreaDAO dao,
            final StormCenterLoader stormCenterLoader) {
        this.dao = dao;
        this.stormCenterLoader = stormCenterLoader;
    }

    /**
     * Parses a single thematic area into an OutageAreaRevision.
     */
    static OutageAreaRevision parseOutageArea(final Node area,
            final ParserRun run) {
        final String descriptionHtml = PepcoUtil.getTextFromOnlyElement(area,
                "description");
        final String sCustomersOut = ((TextNode) Jsoup
                .parseBodyFragment(descriptionHtml)
                .select(":containsOwn(Customers Affected)").first()
                .nextSibling()).text().trim();
        final int customersOut = sCustomersOut.equals("Less than 5") ? 0
                : Integer.parseInt(sCustomersOut);
        return new OutageAreaRevision(new OutageArea(
                PepcoUtil.getTextFromOnlyElement(area, "title")), customersOut,
                run);
    }

    private void scrape(NodeList areas, ParserRun run) {
        for (int i = 0; i < areas.getLength(); i++) {
            dao.updateArea(parseOutageArea(areas.item(i), run));
        }
    }

    @Override
    public void scrape(ParserRun run) throws IOException {
        scrape(stormCenterLoader.loadXMLRequest(
                PepcoScraper.DATA_HTML_PREFIX + THEMATIC_SUFFIX)
                .getElementsByTagName("item"), run);
    }
}

package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.sql.Timestamp;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pocketcookies.pepco.model.ParserRun;
import com.pocketcookies.pepco.model.Summary;
import com.pocketcookies.pepco.model.dao.SummaryDAO;

public class SummaryScraper implements Scraper {
    private static final String SUMMARY_SUFFIX = "data.xml";
    private final SummaryDAO dao;
    private final StormCenterLoader stormCenterLoader;

    public SummaryScraper(final SummaryDAO dao,
            final StormCenterLoader stormCenterLoader) {
        this.dao = dao;
        this.stormCenterLoader = stormCenterLoader;
    }

    private Summary parseSummary(final Document doc, final ParserRun run) {
        final NodeList areas = doc.getElementsByTagName("area");
        final Node dc = areas.item(0);
        final Node pg = areas.item(1);
        final Node mont = areas.item(2);
        return new Summary(Integer.parseInt(PepcoUtil.getTextFromOnlyElement(
                doc, "total_outages")), Integer.parseInt(PepcoUtil
                .getTextFromOnlyElement(dc, "custs_out")),
                Integer.parseInt(PepcoUtil.getTextFromOnlyElement(dc,
                        "total_custs")), Integer.parseInt(PepcoUtil
                        .getTextFromOnlyElement(pg, "custs_out")),
                Integer.parseInt(PepcoUtil.getTextFromOnlyElement(pg,
                        "total_custs")), Integer.parseInt(PepcoUtil
                        .getTextFromOnlyElement(mont, "custs_out")),
                Integer.parseInt(PepcoUtil.getTextFromOnlyElement(mont,
                        "total_custs")), new Timestamp(PepcoUtil
                        .parsePepcoDateTime(
                                PepcoUtil.getTextFromOnlyElement(doc,
                                        "date_generated")).getMillis()), run);
    }

    @Override
    public void scrape(final ParserRun run) throws IOException {
        dao.saveSummary(parseSummary(
                stormCenterLoader.loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
                        + SUMMARY_SUFFIX), run));
    }
}

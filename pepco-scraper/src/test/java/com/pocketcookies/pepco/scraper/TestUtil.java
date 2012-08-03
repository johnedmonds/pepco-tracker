package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class TestUtil {
    static Document loadXml(InputStream stream) throws SAXException, IOException,
            ParserConfigurationException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(stream);
    }
}

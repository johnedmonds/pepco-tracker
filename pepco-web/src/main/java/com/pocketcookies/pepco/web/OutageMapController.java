package com.pocketcookies.pepco.web;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.dao.OutageDAO;
import java.sql.Timestamp;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Controller for the outage-maps page.
 * @author jack
 */
@Controller
@RequestMapping(value = "/outage-map")
public class OutageMapController {

    final OutageDAO outageDao;
    final Logger logger = Logger.getLogger(OutageMapController.class);

    @Autowired
    public OutageMapController(
            @Qualifier("outageDao") final OutageDAO outageDao) {
        this.outageDao = outageDao;
    }

    @RequestMapping(value = "")
    public ModelAndView index() {
        return new ModelAndView("pepco.outage.map");
    }

    @RequestMapping(value = "outages-{dateTime}.kmz")
    public void outageKml(final HttpServletResponse response, @PathVariable(value = "dateTime") @DateTimeFormat(pattern = "yyyymmdd.HHmmss") final Date dateTime) throws TransformerConfigurationException {
        response.setContentType("application/vnd.google-earth.kmz");

        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final Element kml = doc.createElement("kml");
            doc.appendChild(kml);
            for (final AbstractOutageRevision revision : this.outageDao.getOutagesAsOf(new Timestamp(dateTime.getTime()), AbstractOutageRevision.class)) {
                final Element placemark = doc.createElement("Placemark");

                final Element name = doc.createElement("name");
                if (revision instanceof OutageRevision) {
                    name.setTextContent("Outage");
                } else {
                    name.setTextContent("Outage Cluster");
                }

                final Element description = doc.createElement("description");
                description.setTextContent("An outage");

                final Element point = doc.createElement("Point");
                final Element coordinates = doc.createElement("coordinates");
                coordinates.setTextContent(revision.getOutage().getLon() + "," + revision.getOutage().getLat());

                point.appendChild(coordinates);
                placemark.appendChild(point);
                placemark.appendChild(description);
                placemark.appendChild(name);
                kml.appendChild(placemark);
            }
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(response.getOutputStream()));
        } catch (Exception e) {
            logger.error(e);
        }
    }
}

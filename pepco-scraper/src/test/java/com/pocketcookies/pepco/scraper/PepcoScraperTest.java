package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

public class PepcoScraperTest {
    @Test
    public void testYearChangeParseDateTime() {
      final DateTime jan1=new DateTime(2012, 1, 1, 0, 0);
      final DateTime dec31=new DateTime(2011, 12, 31, 0, 0);
      final DateTime jan2=new DateTime(2012, 1, 2, 0, 0);
      assertEquals(jan1.getYear(), PepcoUtil.parsePepcoDateTime("Jan 1, 12:00 AM", jan1).getYear());
      assertEquals(jan1.getYear() - 1, PepcoUtil.parsePepcoDateTime("Dec 31, 12:00 AM", jan1).getYear());
      assertEquals(jan2.getYear(), PepcoUtil.parsePepcoDateTime("Dec 31, 12:00 AM", jan2).getYear());
      assertEquals(dec31.getYear(), PepcoUtil.parsePepcoDateTime("Dec 31, 12:00 AM", dec31).getYear());
      assertEquals(dec31.getYear(), PepcoUtil.parsePepcoDateTime("Jan 1, 12:00 AM", dec31).getYear());
    }
}

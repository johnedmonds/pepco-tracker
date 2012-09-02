/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import junit.framework.TestCase;

/**
 * Tests for {@link OutageRevision}.
 *
 * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
 */
public class TestOutageRevision extends TestCase {

	private static final int DEFAULT_FIRST_SEEN_ZOOM_LEVEL=1;
	private static final int DEFAULT_LAST_SEEN_ZOOM_LEVEL=1;
	
    /**
     * Tests compareTo
     */
    public void testCompareTo() {
	assertEquals(0,
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL)));
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(0), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL)) > 0);
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(0), new Timestamp(1)), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL, DEFAULT_LAST_SEEN_ZOOM_LEVEL)) < 0);
    }
}

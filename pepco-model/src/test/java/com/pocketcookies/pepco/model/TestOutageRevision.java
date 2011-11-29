/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import junit.framework.TestCase;

/**
 *
 * @author m1jae02
 */
public class TestOutageRevision extends TestCase {

    /**
     * Tests compareTo
     */
    public void testCompareTo() {
	assertEquals(0,
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1)));
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(0)), 1).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1)) > 0);
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).compareTo(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(0)), 1)) < 0);
    }
}

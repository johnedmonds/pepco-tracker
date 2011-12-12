package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import junit.framework.TestCase;

/**
 *
 */
public class TestParserRun extends TestCase {

    public void testEquality() {
	//Regular test for equals.
	assertEquals(
		new ParserRun(new Timestamp(1), new Timestamp(1)),
		new ParserRun(new Timestamp(1), new Timestamp(1)));

	//Test that equals ignores asof.
	assertEquals(
		new ParserRun(new Timestamp(1), new Timestamp(1)),
		new ParserRun(new Timestamp(1), new Timestamp(2)));
	//Make sure equals checks the runtime.
	assertFalse(
		new ParserRun(new Timestamp(1), new Timestamp(1)).equals(
		new ParserRun(new Timestamp(2), new Timestamp(1))));
    }

    public void testComparison() {
	assertEquals(0,
		new ParserRun(new Timestamp(1), new Timestamp(1)).compareTo(
		new ParserRun(new Timestamp(1), new Timestamp(2))));
	assertTrue(
		new ParserRun(new Timestamp(1), new Timestamp(1)).compareTo(
		new ParserRun(new Timestamp(2), new Timestamp(1))) < 0);
	assertTrue(
		new ParserRun(new Timestamp(2), new Timestamp(1)).compareTo(
		new ParserRun(new Timestamp(1), new Timestamp(1))) > 0);
    }
}

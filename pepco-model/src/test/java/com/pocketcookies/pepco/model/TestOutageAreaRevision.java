package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

public class TestOutageAreaRevision extends TestCase {
	public void testEqualsIgnoreTime() {
		assertTrue(new OutageAreaRevision(null, 1, new ParserRun(new Timestamp(
				1))).equalsIgnoreTime(new OutageAreaRevision(null, 1,
				new ParserRun(new Timestamp(1)))));
		assertTrue(new OutageAreaRevision(null, 1, new ParserRun(new Timestamp(
				1))).equalsIgnoreTime(new OutageAreaRevision(null, 1,
				new ParserRun(new Timestamp(2)))));
		assertFalse(new OutageAreaRevision(null, 2, new ParserRun(
				new Timestamp(1))).equalsIgnoreTime(new OutageAreaRevision(
				null, 1, new ParserRun(new Timestamp(1)))));
	}
}

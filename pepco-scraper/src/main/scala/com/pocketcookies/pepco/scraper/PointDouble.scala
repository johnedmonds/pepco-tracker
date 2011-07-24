package com.pocketcookies.pepco.scraper

class PointDouble(val lat: Double, val lon: Double) {
  def this(sLatLon: List[String]) = this(java.lang.Double.parseDouble(sLatLon(0)), java.lang.Double.parseDouble(sLatLon(1)))
}
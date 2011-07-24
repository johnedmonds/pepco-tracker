package com.pocketcookies.pepco.scraper
import scala.collection.mutable.LinkedList

object PepcoUtil {
  /**
   * Taken from
   * http://www.pepco.com/home/emergency/maps/stormcenter/scripts/ifactor
   * /stormcenter.js
   *
   * @param lat
   * @param lon
   * @param zoom
   * @return
   */
  def getSpatialIndicesForPoint(lat: Double, lon: Double,
    zoom: Int): List[String] = {
    var minLat: Double = -85.05112878;
    var maxLat: Double = 85.05112878;
    var minLong: Double = -180;
    var maxLong: Double = 180;
    var indexName: String = "";
    var A: Int = 0;
    var curZoom: Int = 0;
    var D: Int = 0;
    var indexNames: List[String] = List[String]();
    var zoomLevels: Array[String] = Array("0", "1", "2", "3");
    for (curZoom <- 0 until zoom) {
      A = 0;
      if (lat < ((maxLat + minLat) / 2)) {
        A = A + 2;
        maxLat = (maxLat + minLat) / 2;
      } else {
        minLat = (maxLat + minLat) / 2;
      }
      if (lon > ((maxLong + minLong) / 2)) {
        A = A + 1;
        minLong = (maxLong + minLong) / 2;
      } else {
        maxLong = (maxLong + minLong) / 2;
      }
      indexName = indexName + A;
    }
    if (indexName.length() > 1) {
      indexName = indexName.substring(0, indexName.length() - 2);
      for (curZoom <- 0 until zoomLevels.length) {
        for (D <- 0 until zoomLevels.length) {
          indexNames = (indexName + zoomLevels(curZoom) + zoomLevels(D)) :: indexNames;
        }
      }
    } else {
      indexNames = zoomLevels toList
    }
    return indexNames;
  }
}
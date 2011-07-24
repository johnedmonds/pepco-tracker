package com.pocketcookies.pepco.scraper

class Polygon(vertices: List[PointDouble]) {
  def this(s: String) = this(List.fromString(s, ' ').grouped(2).map(a => new PointDouble(a)).toList)
  def getCenter(): PointDouble = {
    val temp: PointDouble = vertices.reduceLeft((a: PointDouble, b: PointDouble) => new PointDouble(a.lat + b.lat, a.lon + b.lon))
    new PointDouble(temp.lat / vertices.size, temp.lon / vertices.size)
  }
}
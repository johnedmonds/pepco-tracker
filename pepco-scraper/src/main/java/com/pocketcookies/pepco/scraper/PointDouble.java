package com.pocketcookies.pepco.scraper;

public class PointDouble {
    public final double lat;
    public final double lon;

    public PointDouble(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public PointDouble(String latLng) {
        this(latLng.split(" "));
    }

    private PointDouble(String[] latLng) {
        this(Double.parseDouble(latLng[0]), Double.parseDouble(latLng[1]));
        assert latLng.length == 2;
    }
}

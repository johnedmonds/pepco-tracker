package com.pocketcookies.pepco.scraper;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class Polygon {
    private final ImmutableList<PointDouble> points;

    public Polygon(String s) {
        final String[] sPoints = s.split(" ");
        assert sPoints.length % 2 == 0;
        final ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 0; i < sPoints.length / 2; i++) {
            builder.add(sPoints[i] + sPoints[i + 1]);
        }
        points = ImmutableList.copyOf(Iterables.transform(builder.build(),
                new Function<String, PointDouble>() {
                    public PointDouble apply(String s) {
                        return new PointDouble(s);
                    }
                }));
    }

    public PointDouble getCenter() {
        double sumLat = 0, sumLng = 0;
        for (PointDouble pd : points) {
            sumLat += pd.lat;
            sumLng += pd.lon;
        }
        return new PointDouble(sumLat / points.size(), sumLng / points.size());
    }
}

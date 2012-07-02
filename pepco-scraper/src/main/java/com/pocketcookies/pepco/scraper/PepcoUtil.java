package com.pocketcookies.pepco.scraper;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class PepcoUtil {
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
    public List<String> getSpatialIndicesForPoint(double lat, double lon,
            int zoom) {
        Double minLat = -85.05112878;
        Double maxLat = 85.05112878;
        Double minLong = -180.0;
        Double maxLong = 180.0;
        String indexName = "";
        int A = 0;
        int curZoom = 0;
        int D = 0;
        List<String> indexNames = new LinkedList<String>();
        String[] zoomLevels = new String[] { "0", "1", "2", "3" };
        for (; curZoom < zoom; curZoom++) {
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
            for (curZoom = 0; curZoom < zoomLevels.length; curZoom++) {
                for (D = 0; D < zoomLevels.length; D++) {
                    indexNames.add(indexName + zoomLevels[curZoom]
                            + zoomLevels[D]);
                }
            }
        } else {
            indexNames = ImmutableList.<String> copyOf(zoomLevels);
        }
        return indexNames;
    }
}
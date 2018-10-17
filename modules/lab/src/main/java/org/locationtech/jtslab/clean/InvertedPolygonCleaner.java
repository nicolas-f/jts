/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtslab.clean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.dissolve.LineDissolver;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;

/**
 * Cleans an invalid polygon which contains  
 * inverted holes and exverted regions.  
 * The input geometry is assumed to be correctly noded.
 * <p>
 * This kind of polygon may be produced by rasterization processes.
 * 
 * @author mbdavis
 *
 */
public class InvertedPolygonCleaner {
  
  public static Geometry clean(Polygon inputPoly) {
    // Extract lines, noded at common vertices
    Geometry nodedLines = LineDissolver.dissolve(inputPoly);

    // Polygonize the noded line arrangement
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedLines);
    Collection<Polygon> polygons = polygonizer.getPolygons();
    
    // Discard any polygons which lie outside the original polygon
    // (IndexedPointInAreaLocator is tolerant of inverted/exverted polygonal input)
    IndexedPointInAreaLocator locater = new IndexedPointInAreaLocator(inputPoly);
    List resultPolys = new ArrayList();
    for (Polygon poly : polygons) {
      if (Location.EXTERIOR != locater.locate(poly.getInteriorPoint().getCoordinate())) {
        resultPolys.add(poly);
      }
    }
    return inputPoly.getFactory().buildGeometry(resultPolys);
  }
  
}

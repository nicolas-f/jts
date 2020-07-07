/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;

public class OverlayPolygonLine {

  private Geometry polyGeom;
  private Coordinate[] polyCoords;

  public OverlayPolygonLine(Geometry polyGeom) {
    this.polyGeom = polyGeom;
    this.polyCoords = polyGeom.getCoordinates();
  }
  
  public Geometry getResult(Geometry lineGeom) {
    return compute(lineGeom);
  }

  private Geometry compute(Geometry lineGeom) {
    node(lineGeom);
    return null;
  }

  private void node(Geometry lineGeom) {
    Coordinate[] pts = lineGeom.getCoordinates();
    NodedSegmentString lineSS = new NodedSegmentString(pts, null);
    
    SegmentString polySS = new BasicSegmentString(polyCoords, null);
    
    for (int i = 1; i < lineSS.size(); i++ ) {
      for (int j = 1; j < polySS.size(); j++) {
        computeIntersection(lineSS, i-1, i, polySS, j-1, j);
      }
    }
    
  }

  private void computeIntersection(NodedSegmentString liness, int i, int i2, SegmentString polySS, int j, int j2) {
    
  }


  
  
}

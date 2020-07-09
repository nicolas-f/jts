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

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentNode;
import org.locationtech.jts.noding.SegmentString;

public class OverlayPolygonLine {

  private Geometry polyGeom;
  private Coordinate[] polyCoords;
  private LineIntersector li = new RobustLineIntersector();
  private Map<SegmentNode, LineEdge> nodeMap = new HashMap<SegmentNode, LineEdge>();

  public OverlayPolygonLine(Geometry polyGeom) {
    this.polyGeom = polyGeom;
    this.polyCoords = polyGeom.getCoordinates();
  }
  
  public Geometry getResult(Geometry lineGeom) {
    // TODO: remove repeated points from line
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
    
    for (int i = 0; i < lineSS.size() - 1; i++ ) {
      for (int j = 0; j < polySS.size() - 1; j++) {
        processIntersections(lineSS, i, polySS, j);
      }
    }
    
  }

  public void processIntersections(
      SegmentString lineSS,  int segIndex0,
      SegmentString polySS,  int segIndex1
      ) {
    Coordinate p00 = lineSS.getCoordinates()[segIndex0];
    Coordinate p01 = lineSS.getCoordinates()[segIndex0 + 1];
    Coordinate p10 = polySS.getCoordinates()[segIndex1];
    Coordinate p11 = polySS.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p00, p01, p10, p11);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    /**
     * Process single point intersections.
     */
    // TODO: handle two-point (collinear) intersections 
    if (li.hasIntersection() && li.getIntersectionNum() == 1) {
        
        Coordinate intPt = li.getIntersection(0);
        
        SegmentNode segNode = ((NodedSegmentString) lineSS).addIntersectionNode(intPt, segIndex0);
        
        LineEdge nodeEdge = nodeMap.get(segNode);
        
        if (nodeEdge == null) {
          nodeEdge = createNode(lineSS, segIndex0, p00, p01, intPt);
          nodeMap.put(segNode, nodeEdge);
        }
    }

  }

  /**
   * Creates a node in the line edge graph.
   * The node is created with both line edges originating at the node
   * (except for end nodes, which have only one edge).
   * 
   * @param lineSS
   * @param segIndex0
   * @param intPt
   * @return
   */
  private LineEdge createNode(SegmentString lineSS, int segIndex, 
      Coordinate p0, Coordinate p1, Coordinate intPt) {
    
    LineEdge e = null;
    
    
  }

  
  
}

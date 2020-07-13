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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentNode;
import org.locationtech.jts.noding.SegmentNodeList;
import org.locationtech.jts.noding.SegmentString;

public class OverlayPolygonLine {

  private Geometry polyGeom;
  private Coordinate[] polyCoords;
  private LineIntersector li = new RobustLineIntersector();
  private Map<SegmentNode, PolygonLineNode> nodeMap = new HashMap<SegmentNode, PolygonLineNode>();

  public OverlayPolygonLine(Geometry polyGeom) {
    this.polyGeom = polyGeom;
    this.polyCoords = polyGeom.getCoordinates();
  }
  
  public Geometry getResult(Geometry lineGeom) {
    // TODO: remove repeated points from line
    return compute(lineGeom);
  }

  private Geometry compute(Geometry lineGeom) {
    NodedSegmentString lineSS = node(lineGeom);
    
    merge(nodeMap.values());
    
    SegmentNodeList segNodeList = lineSS.getNodeList();
    List splitEdges = new ArrayList();
    segNodeList.addSplitEdges(splitEdges);
    
    
    return null;
  }

  private void merge(Collection<PolygonLineNode> nodes) {
    for (PolygonLineNode node : nodes) {
      node.merge();
    }
  }

  private NodedSegmentString node(Geometry lineGeom) {
    Coordinate[] pts = lineGeom.getCoordinates();
    NodedSegmentString lineSS = new NodedSegmentString(pts, null);
    
    SegmentString polySS = new BasicSegmentString(polyCoords, null);
    
    for (int i = 0; i < lineSS.size() - 1; i++ ) {
      for (int j = 0; j < polySS.size() - 1; j++) {
        processIntersections(lineSS, i, polySS, j);
      }
    }
    return lineSS;
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
    if (li.hasIntersection() && li.getIntersectionNum() == 1) {
        
        Coordinate intPt = li.getIntersection(0);
        
        SegmentNode segNode = ((NodedSegmentString) lineSS).addIntersectionNode(intPt, segIndex0);
        
        PolygonLineNode node = nodeMap.get(segNode);
        
        if (node == null) {
          node = createNode(lineSS, segIndex0, p00, p01, intPt);
          nodeMap.put(segNode, node);
        }
        node.addPolygonEdge(p10, false);
        node.addPolygonEdge(p11, true);
    }
    // TODO: handle two-point (collinear) intersections 

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
  private PolygonLineNode createNode(SegmentString lineSS, int segIndex, 
      Coordinate segp0, Coordinate segp1, Coordinate intPt) {
    
    PolygonLineNode node = new PolygonLineNode(intPt);
    if (! intPt.equals2D(segp0)) {
      node.addLineEdge(segp0, false);
    }
    if (! intPt.equals2D(segp1)) {
      node.addLineEdge(segp1, true);
    }
    return node;
    
  }

  
  
}

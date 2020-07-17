/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.clip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.noding.BasicSegmentString;
import org.locationtech.jts.noding.MCIndexSegmentSetMutualIntersector;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentIntersector;
import org.locationtech.jts.noding.SegmentNode;
import org.locationtech.jts.noding.SegmentString;

class AreaLineNoder {

  private List<SegmentString> areaEdges = new ArrayList<SegmentString>();
  private MCIndexSegmentSetMutualIntersector intersector;
  
  public AreaLineNoder(Geometry geom) {
    add(geom);
    intersector = new MCIndexSegmentSetMutualIntersector(areaEdges);
  }
  
  private void add(Geometry geom) {
    for (int i = 0; i < geom.getNumGeometries(); i++) {
      addPolygon((Polygon) geom.getGeometryN(i));
    }
  }
  
  private void addPolygon(Polygon poly) {
    addPolygonRing(poly.getExteriorRing(), false);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      addPolygonRing(poly.getInteriorRingN(i), true);
    }
  }
  
  private void addPolygonRing(LinearRing ring, boolean isHole) {
    /**
     * Compute the orientation of the ring, to
     * allow assigning side interior/exterior labels correctly.
     * JTS canonical orientation is that shells are CW, holes are CCW.
     * 
     * It is important to compute orientation on the original ring,
     * since topology collapse can make the orientation computation give the wrong answer.
     */
    boolean isCCW = Orientation.isCCW( ring.getCoordinateSequence() );
    /**
     * Compute whether ring is in canonical orientation or not.
     * Canonical orientation for the overlay process is
     * Shells : CW, Holes: CCW
     */
    boolean isInteriorRight = true;
    if (! isHole)
      isInteriorRight = ! isCCW;
    else {
      isInteriorRight = isCCW;
    }
    Coordinate[] pts = ring.getCoordinates();
    SegmentString ringSS = new BasicSegmentString(pts, isInteriorRight);
    areaEdges.add(ringSS);
  }

  public NodedSegmentString nodeSLOW(Geometry lineGeom, Map<SegmentNode, AreaLineNode> nodeMap) {
    Coordinate[] pts = lineGeom.getCoordinates();
    NodedSegmentString lineSS = new NodedSegmentString(pts, null);
    AreaLineSegmentIntersector si = new AreaLineSegmentIntersector(nodeMap);

    for (int i = 0; i < lineSS.size() - 1; i++ ) {
      for (SegmentString ss : areaEdges) {
        for (int j = 0; j < ss.size() - 1; j++) {
          si.processIntersections(lineSS, i, ss, j);
        }
      }
    }
    return lineSS;
  }
  
  public NodedSegmentString node(Geometry lineGeom, Map<SegmentNode, AreaLineNode> nodeMap) {
    Coordinate[] pts = lineGeom.getCoordinates();
    NodedSegmentString lineSS = new NodedSegmentString(pts, null);
    List<NodedSegmentString> lineList = new ArrayList<NodedSegmentString>();
    lineList.add(lineSS);
    
    AreaLineSegmentIntersector segInt = new AreaLineSegmentIntersector(nodeMap);

    intersector.process(lineList, segInt);
    return lineSS;
  }

  
}

class AreaLineSegmentIntersector implements SegmentIntersector {
  
  private LineIntersector li = new RobustLineIntersector();
  private Map<SegmentNode, AreaLineNode> nodeMap;

  public AreaLineSegmentIntersector(Map<SegmentNode, AreaLineNode> nodeMap) {
    this.nodeMap = nodeMap;
  }

  @Override
  public boolean isDone() {
    return false;
  }
  
  public void processIntersections(
      SegmentString lineSS,  int segIndex0,
      SegmentString polySS,  int segIndex1
      ) {
    boolean isInteriorRight = (boolean) polySS.getData();
    
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

      AreaLineNode node = nodeMap.get(segNode); 
      if (node == null) {
        node = createNode(lineSS, segIndex0, p00, p01, intPt);
        nodeMap.put(segNode, node);
      }
        
      /**
       * Don't add endpoint intersections at that node
       */
      if (! intPt.equals2D(p10)) {
        node.addEdgePolygon(p10, ! isInteriorRight);
      }
      if (! intPt.equals2D(p11)) {
        node.addEdgePolygon(p11, isInteriorRight);
      }
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
  private static AreaLineNode createNode(SegmentString lineSS, int lineSegIndex, 
      Coordinate segp0, Coordinate segp1, Coordinate intPt) {
    
    AreaLineNode node = new AreaLineNode(intPt);
    if (! intPt.equals2D(segp0)) {
      node.addEdgeLine(segp0, false);
    }
    if (! intPt.equals2D(segp1)) {
      node.addEdgeLine(segp1, true);
    }
    return node;
  }


  
}
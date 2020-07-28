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
    
    Coordinate p0Line = lineSS.getCoordinates()[segIndex0];
    Coordinate p1Line = lineSS.getCoordinates()[segIndex0 + 1];
    Coordinate p0Poly = polySS.getCoordinates()[segIndex1];
    Coordinate p1Poly = polySS.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p0Line, p1Line, p0Poly, p1Poly);
//if (li.hasIntersection() && li.isProper()) Debug.println(li);

    /**
     * Process single point intersections.
     */
    if (li.hasIntersection() && li.getIntersectionNum() == 1) {

      Coordinate intPt = li.getIntersection(0);
        
      SegmentNode segNode = ((NodedSegmentString) lineSS).addIntersectionNode(intPt, segIndex0);

      AreaLineNode node = nodeMap.get(segNode); 
      if (node == null) {
        node = new AreaLineNode(intPt);
        nodeMap.put(segNode, node);
      }
      
      /**
       * If the intersection occurs at a polygon segment endpoint 
       * AND/OR an interior line segment endpoint,
       * it will be processed twice (once for each incident segments).
       * This would result in duplicated edges being added.
       * To avoid this, only process these endpoints once.
       */
      boolean isPolyEndpoint = intPt.equals2D(p1Poly);
      if (! isPolyEndpoint) {
        addLineEdges(node, p0Line, p1Line, intPt);
      }
      
      boolean isLineInteriorSegment = segIndex0 < lineSS.size() - 2;
      boolean isLineEndpoint = intPt.equals2D(p1Line);
      boolean isLineInteriorEndpoint = isLineEndpoint && isLineInteriorSegment;
      if (! isLineInteriorEndpoint) {
        /**
         * Don't add zero-length polygon edges
         * (i.e. where intersection is at an endpoint)
         */
        if (! intPt.equals2D(p0Poly)) {
          node.addEdgePolygon(p0Poly, ! isInteriorRight);
        }
        if (! intPt.equals2D(p1Poly)) {
          node.addEdgePolygon(p1Poly, isInteriorRight);
        }
      }
    }
    // TODO: handle two-point (collinear) intersections 

  }

  /**
   * Add the edges for an intersected line segment to
   * the intersection node. 
   * 
   * @param node
   * @param p0Line
   * @param p1Line
   * @param intPt
   */
  private static void addLineEdges(AreaLineNode node, 
      Coordinate p0Line, Coordinate p1Line, Coordinate intPt) {
    /**
     * Don't add zero-length polygon edges
     * (which occur when intersection is at an endpoint)
     */    
    if (! intPt.equals2D(p0Line)) {
      node.addEdgeLine(p0Line, false);
    }
    if (! intPt.equals2D(p1Line)) {
      node.addEdgeLine(p1Line, true);
    }
  }


  
}
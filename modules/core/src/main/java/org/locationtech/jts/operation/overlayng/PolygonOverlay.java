/*
 * Copyright (c) 2019 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.topology.Position;

public class PolygonOverlay {
  
  public static Geometry overlay(Geometry geom, PrecisionModel pm) {
    PolygonOverlay overlay = new PolygonOverlay(geom, pm);
    return overlay.getResult();
  }
  
  private PrecisionModel pm;
  private OverlayGraph graph;
  private Geometry resultGeom;
  private Geometry inputGeom;
  private GeometryFactory geomFact;

  public PolygonOverlay(Geometry geom, PrecisionModel pm) {
    this.pm = pm;
    geomFact = geom.getFactory();
    inputGeom = geom;
  } 
  
  public Geometry getResult() {
    computeOverlay();
    return resultGeom;
  }
  
  private void computeOverlay() {
    
    //--- Noding phase
    List<Edge> edges = nodeAndMerge();
    
    //--- Topology building phase
    graph = new OverlayGraph( edges );

    markResultAreaEdges( graph.getEdges() );
    
    createResult();
  }

  private void createResult() {
    //--- Build polygons
    List<OverlayEdge> resultAreaEdges = graph.getResultAreaEdges();
    PolygonBuilder polyBuilder = new PolygonBuilder(resultAreaEdges, geomFact, false);
    List<Polygon> resultPolyList = extractPolygons(polyBuilder);
    
    resultGeom = geomFact.createGeometryCollection(
        GeometryFactory.toGeometryArray(resultPolyList));
  }

  private List<Polygon> extractPolygons(PolygonBuilder polyBuilder) {
    List<Polygon> polygons = new ArrayList<Polygon>();
    List<OverlayEdgeRing> shells = polyBuilder.getShellRings();
    for (OverlayEdgeRing shell : shells) {
      if (isInResult(shell)) {
        polygons.add( shell.toPolygon( geomFact ));
      }
    }
    return polygons;
  }

  /**
   * Tests if this shell is in the result, 
   * by checking if any edge lies in the interior of an input polygon.
   * 
   * @param shell the shell ring to test
   * @return true if this shell is in the result
   */
  private boolean isInResult(OverlayEdgeRing shell) {
    OverlayEdge startEdge = shell.getEdge();
    OverlayEdge edge = startEdge;
    do {
      if (isInterior(edge))
        return true;
      edge = edge.nextResult();
    } while (edge != startEdge); 
    return false;
  }

  private static boolean isInterior(OverlayEdge edge) {
    return edge.getLocation(0, Position.RIGHT) == Location.INTERIOR;
  }

  private List<Edge> nodeAndMerge() {
    /**
     * Node the edges, using whatever noder is being used
     */
    OverlayNoder ovNoder = new OverlayNoder(pm);
    
    //if (noder != null) ovNoder.setNoder(noder);
    
    ovNoder.add(inputGeom, 0);
    Collection<SegmentString> nodedLines = ovNoder.node();
    
    /**
     * Merge the noded edges to eliminate duplicates.
     * Labels will be combined.
     */
    // nodedSegStrings are no longer needed, and will be GCed
    List<Edge> edges = OverlayNG.createEdges(nodedLines);
    List<Edge> mergedEdges = EdgeMerger.merge(edges);
    
    return mergedEdges;
  }

  /**
   * Marks result edges.
   * In polygon overlay, every edge appears 
   * in the output, except for edges which collapse to lines.
   * @param edges 
   */
  private void markResultAreaEdges(Collection<OverlayEdge> edges) {
    for (OverlayEdge edge : edges) {
      edge.markInResultArea();
    }
  }
  
  /**
   * Marks result edges.
   * In polygon overlay, every edge appears 
   * in the output, except for edges which collapse to lines.
   * This allows the following simple strategy:
   * <ul>
   * <li>Mark all rings which have at least one interior edge as in the result
   * <li>Unmark any edge sequences which terminate in a degree-1 node
   * </ul>
   * 
   */
  private void xmarkResultAreaEdges() {
    for (OverlayEdge edge : graph.getEdges()) {
      // skip if already marked
      if (edge.isInResultArea())
        continue;
      
      if ( isInterior(edge)) {
        markRingInResult(edge);     
      }
    }
  }

  private void markRingInResult(OverlayEdge startEdge) {
    OverlayEdge edge = startEdge;
    do {
      // perhaps - assert: edge is not marked?
      edge.markInResultArea();
      // get next edge CW at dest, which is next CW ring edge
      OverlayEdge edgeNextInRing = edge.symOE().oNextOE();
      edge = edgeNextInRing;
    } while (edge != startEdge); 
  }
}

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
package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Position;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.Debug;

public class AreaLineNode {

  private Coordinate orig;
  private List<AreaLineEdge> edges = new ArrayList<AreaLineEdge>();

  public AreaLineNode(Coordinate orig) {
    this.orig = orig;
  }

  public void addEdgeLine(Coordinate dest, boolean isForward) {
    edges.add(new AreaLineEdge(orig, dest, true, isForward));
  }

  public void addEdgePolygon(Coordinate dest, boolean isForward) {
    edges.add(new AreaLineEdge(orig, dest, false, isForward));
  }

  public int degree() {
    return edges.size();
  }
 
  public AreaLineEdge getEdge(int i) {
    return edges.get(i);
  }

  /**
   * Determines whether the line edge starting/ending
   * at this node lies in the interior of the polygon.
   * 
   * @param isForward true if the forward direction line edge is to be checked, falso for 
   * the reverse direction edge 
   * @return true if the directed line edge is in the polygon interior
   */
  public boolean isInterior(boolean isForward) {
    //--- Find directed line edge
    int lineIndex = findLineIndex(isForward);
    // Assert lineIndex >= 0
    AreaLineEdge lineEdge = edges.get(lineIndex);
    return lineEdge.isInterior();
  }

  private int next(int index) {
    // Assert: index >= 0 && index < edges.size()
    int next = index += 1;
    if (next >= edges.size())
      next = 0;
    return next;
  }

  private int findLineIndex(boolean isForward) {
    for (int i = 0; i < edges.size(); i++) {
      AreaLineEdge edge = edges.get(i);
      if (edge.isLine(isForward))
        return i;
    }
    return -1;
  }

  public void mergeAndLabel() {
    edges.sort(null);
    // TODO: merge coincident Area edges
    propagateAreaLocations();
  }
  
  /**
   * Scans around node CCW, propagating the side labels
   * for the Area geometry to all non-boundary edges.
   */
  public void propagateAreaLocations() {
    if (degree() == 1) return;
    
    int edgeIndex = findPropagationStartEdge();
     
    // no boundary edge found, so nothing to propagate
    if ( edgeIndex < 0 )
      return;
    AreaLineEdge eStart = getEdge(edgeIndex);
    
    // initialize currLoc to location of L side
    int currLoc = eStart.getAreaLocation(Position.LEFT);
    edgeIndex = next(edgeIndex);
    AreaLineEdge e = getEdge(edgeIndex);

    //Debug.println("\npropagateSideLabels " + " : " + eStart);
    //Debug.print("BEFORE: " + this);
    
    do {
      OverlayLabel label = e.getLabel();
      if ( ! label.isBoundary(AreaLineEdge.INDEX_AREA) ) {
      /**
       * If this is not a Boundary edge for this input area, 
       * its location is now known relative to this input area
       */
        e.setAreaLocation(currLoc);
      }
      else {
        Assert.isTrue(label.hasSides(AreaLineEdge.INDEX_AREA));
        /**
         *  This is a boundary edge for the input area geom.
         *  Update the current location from its labels.
         *  Also check for topological consistency.
         */
        int locRight = e.getAreaLocation(Position.RIGHT);
        if (locRight != currLoc) {
          /*
          Debug.println("side location conflict: index= " + geomIndex + " R loc " 
        + Location.toLocationSymbol(locRight) + " <>  curr loc " + Location.toLocationSymbol(currLoc) 
        + " for " + e);
        //*/
          throw new TopologyException("side location conflict", e.getCoordinate());
        }
        int locLeft = e.getAreaLocation( Position.LEFT);
        if (locLeft == Location.NONE) {
          Assert.shouldNeverReachHere("found single null side at " + e);
        }
        currLoc = locLeft;
      }
      edgeIndex = next(edgeIndex);
      e = getEdge(edgeIndex);
    } while (e != eStart);
    //Debug.print("AFTER: " + this);
  }

  /**
   * Finds the index for a boundary edge for the area, if one exists.
   * 
   * @return a boundary edge index, or -1 if no boundary edge exists
   */
  private int findPropagationStartEdge() {
    for (int i = 0; i < edges.size(); i++) {
      OverlayLabel label = getEdge(i).getLabel();
      if (label.isBoundary(AreaLineEdge.INDEX_AREA)) {
        Assert.isTrue(label.hasSides(AreaLineEdge.INDEX_AREA));
        return i;
      }
    }
    return -1;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (AreaLineEdge e : edges) {
      sb.append(e + "\n");
    }
    return sb.toString();
  }
}

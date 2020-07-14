package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Position;

public class AreaLineNode {

  private Coordinate orig;
  private List<AreaLineEdge> edges = new ArrayList<AreaLineEdge>();

  public AreaLineNode(Coordinate orig) {
    this.orig = orig;
  }

  public void addLineEdge(Coordinate dest, boolean isForward) {
    edges.add(new AreaLineEdge(orig, dest, true, isForward));
  }

  public void addPolygonEdge(Coordinate dest, boolean isForward) {
    edges.add(new AreaLineEdge(orig, dest, false, isForward));
  }

  public void merge() {
    edges.sort(null);
    // TODO: merge coincident edges
    propagateAreaLabels();
  }

  public void propagateAreaLabels() {
    //--- Find area edge
    int currIndex = findAreaBoundaryIndex();
    // Assert lineIndex >= 0
    AreaLineEdge currEdge = edges.get(currIndex);
    int currLoc = currEdge.getAreaLocation(Position.RIGHT);
  }
  


  /**
   * Determines whether the line edge starting/ending
   * at this node lies in the interior of the polygon.
   * @param isForward
   * @return true if the directed line edge is in the polygon interior
   */
  public boolean isInterior(boolean isForward) {
    //--- Find directed line edge
    int lineIndex = findLineIndex(isForward);
    // Assert lineIndex >= 0
    AreaLineEdge lineEdge = edges.get(lineIndex);
    

    int iedge = next(lineIndex);
    // TODO: check if edge is coincident with line edge ==> INTERIOR
    
    /*
     * Search CCW for a significant (non-collapsed) polygon edge
     * The interior condition is indicated by the R side of that edge 
     */
    while ( iedge != lineIndex ) {
      // TODO: handle collapses
      // TODO: handle  
      
      
      iedge = next(iedge);
    }
    return false;
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

  private int findAreaBoundaryIndex() {
    for (int i = 0; i < edges.size(); i++) {
      AreaLineEdge edge = edges.get(i);
      if (edge.isAreaBoundary())
        return i;
    }
    return -1;

  }

}

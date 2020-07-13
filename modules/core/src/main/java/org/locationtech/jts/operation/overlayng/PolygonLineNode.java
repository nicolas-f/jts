package org.locationtech.jts.operation.overlayng;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

public class PolygonLineNode {

  private Coordinate orig;
  private List<PolygonLineEdge> edges = new ArrayList<PolygonLineEdge>();

  public PolygonLineNode(Coordinate orig) {
    this.orig = orig;
  }

  public void addLineEdge(Coordinate dest, boolean isForward) {
    edges.add(new PolygonLineEdge(dest, true, isForward));
  }

  public void addPolygonEdge(Coordinate dest, boolean isForward) {
    edges.add(new PolygonLineEdge(dest, false, isForward));
  }

  public void merge() {
    // TODO: merge coincident edges
    
  }

}

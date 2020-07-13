package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;

public class PolygonLineEdge  {

  private boolean isLine;
  private boolean isForward;
  private Coordinate dest;

  public PolygonLineEdge(Coordinate dest, boolean isLine, boolean isForward) {
    this.dest = dest;
    this.isLine = isLine;
    this.isForward = isForward;
  }


}

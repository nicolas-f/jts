package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

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
    
  }


  
  
}

package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.overlayng.AreaLineClipper;

public class OverlayClippingFunctions {

  public static Geometry areaLineIntersection(Geometry area, Geometry line) {
    return AreaLineClipper.clip(area, line);
  }
}

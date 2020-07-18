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
package org.locationtech.jtstest.function;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.clip.AreaLineClipper;

public class OverlayClippingFunctions {

  public static Geometry areaLineIntersection(Geometry area, Geometry line) {
    return AreaLineClipper.clip(area, line);
  }
  
  private static Geometry cacheKey = null;
  private static AreaLineClipper cacheClipper = null;
  
  public static Geometry areaLineIntersectionCached(Geometry area, Geometry line) {
    if (area != cacheKey) {
      cacheKey = area;
      cacheClipper = new AreaLineClipper(area);
    }
    return cacheClipper.getResult(line);
  }

}

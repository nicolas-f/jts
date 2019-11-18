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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.PrecisionModel;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class PolygonOverlayTest extends GeometryTestCase
{
  public static void main(String args[]) {
    TestRunner.run(PolygonOverlayTest.class);
  }
  
  public PolygonOverlayTest(String name) {
    super(name);
  }

  public void testCrossingSquares( ) {
    checkPolygonOverlay("GEOMETRYCOLLECTION (POLYGON ((50 150, 150 150, 150 50, 50 50, 50 150)), POLYGON ((200 200, 200 100, 100 100, 100 200, 200 200)))",
        1, 
        "GEOMETRYCOLLECTION (POLYGON ((50 50, 50 150, 100 150, 100 100, 150 100, 150 50, 50 50)), POLYGON ((100 100, 100 150, 150 150, 150 100, 100 100)), POLYGON ((100 150, 100 200, 200 200, 200 100, 150 100, 150 150, 100 150)))");
  }

  public void testNestedSquares( ) {
    checkPolygonOverlay("GEOMETRYCOLLECTION (POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)), POLYGON ((50 250, 250 250, 250 50, 50 50, 50 250)))",
        1, 
        "GEOMETRYCOLLECTION (POLYGON ((50 250, 250 250, 250 50, 50 50, 50 250), (100 200, 100 100, 200 100, 200 200, 100 200)), POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200)) )");
  }

  private void checkPolygonOverlay(String wkt, double scaleFactor, String wktExpected) {
    Geometry geom = read(wkt);
    Geometry expected = read(wktExpected);
    PrecisionModel pm = new PrecisionModel(scaleFactor);
    Geometry result = PolygonOverlay.overlay(geom, pm);
    checkEqual(expected, result);
  }
}

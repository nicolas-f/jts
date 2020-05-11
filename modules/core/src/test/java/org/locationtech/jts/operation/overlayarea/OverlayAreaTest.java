/*
 * Copyright (c) 2020 Martin Davis
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.overlayarea;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class OverlayAreaTest extends GeometryTestCase {

  public static void main(String args[]) {
    TestRunner.run(OverlayAreaTest.class);
  }
  
  public OverlayAreaTest(String name) {
    super(name);
  }

  public void testRectangleOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((250 250, 250 150, 150 150, 150 250, 250 250))");
  }

  public void testRectangleTriangleOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))",
        "POLYGON ((300 200, 150 150, 300 100, 300 200))");
  }

  public void testSawOverlap() {
    checkIntersectionArea(
        "POLYGON ((100 300, 305 299, 150 200, 300 150, 150 100, 300 50, 100 50, 100 300))",
        "POLYGON ((400 350, 150 250, 350 200, 200 150, 350 100, 180 50, 400 50, 400 350))");
  }

  private void checkIntersectionArea(String wktA, String wktB) {
    Geometry a = read(wktA);
    Geometry b = read(wktB);
    
    OverlayArea ova = new OverlayArea(a);
    double ovIntArea = ova.intersectionArea(b);
    
    double intAreaFull = a.intersection(b).getArea();
    
    assertEquals(ovIntArea, intAreaFull, 0.0001);
  }
}

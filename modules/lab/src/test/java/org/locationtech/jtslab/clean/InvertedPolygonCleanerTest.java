/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtslab.clean;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Polygonal;

import test.jts.GeometryTestCase;

public class InvertedPolygonCleanerTest extends GeometryTestCase {

  public InvertedPolygonCleanerTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(InvertedPolygonCleanerTest.class);
  }

  public void testInverted() {
    checkValidPolygon("POLYGON ((100 300, 100 100, 400 100, 400 400, 200 400, 200 300, 300 300, 300 200, 200 200, 200 300, 100 300))");
  } 
  
  public void testExverted() {
    checkValidPolygon("POLYGON ((100 100, 100 300, 200 200, 300 300, 300 100, 200 200, 100 100))");
  } 
  
  public void testInvertedAndExverted() {
    checkValidPolygon("POLYGON ((100 300, 100 100, 250 100, 400 100, 400 120, 250 120, 250 250, 270 140, 320 160, 250 250, 400 120, 400 250, 400 300, 350 300, 350 350, 400 350, 400 400, 350 400, 350 350, 300 350, 300 400, 150 400, 150 300, 200 300, 200 250, 230 250, 230 220, 200 220, 200 250, 150 250, 150 300, 100 300))");
  }  

  private void checkValidPolygon(String inputWKT) {
    Geometry input = read(inputWKT);
    
    Geometry actual = InvertedPolygonCleaner.clean((Polygon) input);
    assertTrue(actual.isValid());
    assertTrue(actual instanceof Polygonal);
    assertTrue(actual.getBoundary().equalsTopo(input.getBoundary()));
    // TODO: add more checks to ensure output covers input ?
  }



}

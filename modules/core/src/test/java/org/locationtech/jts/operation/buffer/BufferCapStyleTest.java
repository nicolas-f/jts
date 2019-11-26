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
package org.locationtech.jts.operation.buffer;

import org.locationtech.jts.geom.Geometry;

import test.jts.GeometryTestCase;

public class BufferCapStyleTest extends GeometryTestCase {

  public BufferCapStyleTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(BufferCapStyleTest.class);
  }
  public void testRoundFlatBuffer() {
    BufferParameters param = new BufferParameters();
    param.setStartCapStyle(BufferParameters.CAP_ROUND);
    param.setEndCapStyle(BufferParameters.CAP_FLAT);
    checkBuffer("LINESTRING (0 0, 10 0)", 
        10,
        param,
        "POLYGON ((10 10, 10 -10, 0 -10, -1.9509032201612866 -9.807852804032303, -3.8268343236509033 -9.238795325112864, -5.555702330196022 -8.314696123025453, -7.071067811865477 -7.071067811865475, -8.314696123025454 -5.55570233019602, -9.238795325112868 -3.8268343236508966, -9.807852804032304 -1.9509032201612837, -10 0.0000000000000012, -9.807852804032304 1.9509032201612861, -9.238795325112868 3.826834323650899, -8.314696123025453 5.555702330196022, -7.071067811865475 7.0710678118654755, -5.55570233019602 8.314696123025453, -3.8268343236508926 9.23879532511287, -1.9509032201612755 9.807852804032306, 0 10, 10 10))"
        );
  }

  public void testRoundFlatBuffer2() {
    BufferParameters param = new BufferParameters();
    param.setStartCapStyle(BufferParameters.CAP_ROUND);
    param.setEndCapStyle(BufferParameters.CAP_FLAT);
    checkBuffer("LINESTRING (0 0, 10 10)", 
        10,
        param,
        ""
        );
  }

  public void testFlatPointBuffer() {
    BufferParameters param = new BufferParameters();
    param.setStartCapStyle(BufferParameters.CAP_FLAT);
    param.setEndCapStyle(BufferParameters.CAP_POINT);
    checkBuffer("LINESTRING (0 0, 20 0)", 
        10,
        param,
        "POLYGON ((20 10, 30 0, 20 -10, 0 -10, 0 10, 20 10))"
        );
  }

  public void testLongPointBuffer() {
    BufferParameters param = new BufferParameters();
    param.setStartCapStyle(BufferParameters.CAP_FLAT);
    param.setEndCapStyle(BufferParameters.CAP_POINT);
    param.setEndCapFactor( 3 );
    checkBuffer("LINESTRING (0 0, 20 0)", 
        10,
        param,
        "POLYGON ((20 10, 50 0, 20 -10, 0 -10, 0 10, 20 10))"
        );
  }


  private void checkBuffer(String wkt, double distance, 
      BufferParameters params,
      String wktExpected) {
    Geometry geom = read(wkt);
    Geometry result = BufferOp.bufferOp(geom, distance, params);
    System.out.println(result);
    checkBuffer(result, wktExpected);
  }

  private void checkBuffer(Geometry actual, String wktExpected) {
    Geometry expected = read(wktExpected);
    checkEqual(expected, actual);
  }
}

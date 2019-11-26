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
        ""
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
        ""
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

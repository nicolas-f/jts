package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.geom.Geometry;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class AreaLineClipperTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(AreaLineClipperTest.class);
  }
  
  public AreaLineClipperTest(String name) {
    super(name);
  }
  
  public void testBoxLine( ) {
    checkClip(
        "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
        "LINESTRING (15 15, 25 15)",
        "LINESTRING (15 15, 20 15)");
  }

  private void checkClip(String wktArea, String wktLine, String wktExpected) {
    Geometry area = read(wktArea);
    Geometry line = read(wktLine);
    Geometry expected = read(wktExpected);
    Geometry result = AreaLineClipper.clip(area, line);
    checkEqual(expected, result);
  }
}

package org.locationtech.jts.operation.clip;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.clip.AreaLineClipper;

import junit.textui.TestRunner;
import test.jts.GeometryTestCase;

public class AreaLineClipperTest extends GeometryTestCase {
  public static void main(String args[]) {
    TestRunner.run(AreaLineClipperTest.class);
  }
  
  public AreaLineClipperTest(String name) {
    super(name);
  }
  
  public void testBoxCrossingLine( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (15 15, 25 15)",
      "LINESTRING (15 15, 20 15)");
  }

  public void testBoxCoversProperLine( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (15 15, 19 19)",
      "LINESTRING (15 15, 19 19)");
  }

  public void testBoxCoversLineTouchingAtStart( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (10 15, 15 15)",
      "LINESTRING (10 15, 15 15)");
  }

  public void testBoxCoversLineTouchingAtEnd( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (15 15, 15 20)",
      "LINESTRING (15 15, 15 20)");
  }

  public void testBoxCoversLinewithFlatSpike( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (15 15, 16 16, 16 15, 16 16, 19 19)",
      "LINESTRING (15 15, 16 16, 16 15, 16 16, 19 19)");
  }

  public void testBoxCoversLinewithFlatSpikeAndRepeatedPoint( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (15 15, 16 16, 16 15, 16 16, 16 16, 19 19)",
      "LINESTRING (15 15, 16 16, 16 15, 16 16, 16 16, 19 19)");
  }

  public void testULine( ) {
    checkClip(
      "POLYGON ((10 20, 12 20, 12 12, 18 12, 18 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 15, 25 15)",
      "MULTILINESTRING ((10 15, 12 15), (18 15, 20 15))");
  }

  public void testBoxVertexWithDiagonalLineEntering( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 5, 15 15)",
      "LINESTRING (10 10, 15 15)");
  }
  
  public void testBoxVertexWithDiagonalLineEnteringAtVertex( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 5, 10 10, 15 15)",
      "LINESTRING (10 10, 15 15)");
  }

  public void testBoxVertexWithDiagonalLineCrossing( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 5, 25 25)",
      "LINESTRING (10 10, 20 20)");
  }

  public void testBoxVertexWithDiagonalLineCrossingAtVertex( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 5, 20 20, 25 25)",
      "LINESTRING (10 10, 20 20)");
  }

  public void testBoxVertexWithDiagonalLineCrossingAtVertices( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (5 5, 10 10, 20 20, 25 25)",
      "LINESTRING (10 10, 20 20)");
  }

  public void testBoxTouchingInsideLineInside( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "LINESTRING (12 14, 15 20, 18 14)",
      "MULTILINESTRING ((12 14, 15 20), (15 20, 18 14))");
  }
  
  public void testBoxWith2HolesLineCrossesAll( ) {
    checkClip(
      "POLYGON ((10 40, 80 40, 80 10, 10 10, 10 40), (20 30, 40 30, 40 20, 20 20, 20 30), (50 30, 70 30, 70 20, 50 20, 50 30))",
      "LINESTRING (0 25, 90 25)",
      "MULTILINESTRING ((10 25, 20 25), (40 25, 50 25), (70 25, 80 25))");
  }

  public void testBoxesLineCrossesAll( ) {
    checkClip(
      "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20)), ((30 20, 40 20, 40 10, 30 10, 30 20)), ((50 20, 60 20, 60 10, 50 10, 50 20)))",
      "LINESTRING (0 15, 70 15)",
      "MULTILINESTRING ((10 15, 20 15), (30 15, 40 15), (50 15, 60 15))");
  }

  public void testBoxesWithHolesLineCrossesAll( ) {
    checkClip(
      "MULTIPOLYGON (((10 20, 20 20, 20 10, 10 10, 10 20), (12 18, 18 18, 18 12, 12 12, 12 18)), ((30 20, 40 20, 40 10, 30 10, 30 20), (32 15, 38 18, 38 12, 32 15)), ((50 20, 60 20, 60 10, 50 10, 50 20), (52 18, 52 12, 58 18, 52 18)))",
      "LINESTRING (0 15, 70 15)",
      "MULTILINESTRING ((10 15, 12 15), (18 15, 20 15), (30 15, 32 15), (38 15, 40 15), (50 15, 52 15), (55 15, 60 15))");
  }

  public void testBoxCrossingMultiLine( ) {
    checkClip(
      "POLYGON ((10 20, 20 20, 20 10, 10 10, 10 20))",
      "MULTILINESTRING ((5 15, 15 15), (15 25, 15 15), (25 15, 15 15), (15 5, 15 15))",
      "MULTILINESTRING ((10 15, 15 15), (15 20, 15 15), (20 15, 15 15), (15 10, 15 15))");
  }
  
  
  private void checkClip(String wktArea, String wktLine, String wktExpected) {
    Geometry area = read(wktArea);
    Geometry line = read(wktLine);
    Geometry expected = read(wktExpected);
    Geometry result = AreaLineClipper.clip(area, line);
    checkEqual(expected, result);
  }
}

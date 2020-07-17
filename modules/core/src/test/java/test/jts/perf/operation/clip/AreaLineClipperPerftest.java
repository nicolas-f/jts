package test.jts.perf.operation.clip;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.SineStarFactory;
import org.locationtech.jts.operation.clip.AreaLineClipper;
import org.locationtech.jts.operation.overlayng.OverlayNG;

import test.jts.perf.PerformanceTestCase;
import test.jts.perf.PerformanceTestRunner;

public class AreaLineClipperPerftest extends PerformanceTestCase {
  
  private static final int N_ITER = 100;

  static double ORG_X = 100;
  static double ORG_Y = ORG_X;
  static double SIZE = 2 * ORG_X;
  static int N_ARMS = 20;
  static double ARM_RATIO = 0.3;
  
  static int NUM_CASES = 100;
  
  public static void main(String args[]) {
    PerformanceTestRunner.run(AreaLineClipperPerftest.class);
  }

  private Geometry areaGeom;

  private Geometry[] lines;

  private AreaLineClipper clipper;

  private GeometryFactory geomFactory = new GeometryFactory();
  
  public AreaLineClipperPerftest(String name) {
    super(name);
    setRunSize(new int[] { 100, 1000, 2000, 10000, 100000 });
    //setRunSize(new int[] { 200000 });
    setRunIterations(N_ITER);
  }

  public void setUp()
  {
    System.out.println("AreaLineClipper perf test");
    System.out.println("SineStar: origin: ("
        + ORG_X + ", " + ORG_Y + ")  size: " + SIZE
        + "  # arms: " + N_ARMS + "  arm ratio: " + ARM_RATIO);   
    System.out.println("# Iterations: " + N_ITER);
    System.out.println("# lines: " + NUM_CASES);
  }
  
  public void startRun(int npts)
  {
    areaGeom = SineStarFactory.create(new Coordinate(ORG_X, ORG_Y), SIZE, npts, N_ARMS, ARM_RATIO);
    // force clipper to be rebuilt
    clipper = null;
    
    lines =  createTestLines(NUM_CASES, areaGeom.getEnvelopeInternal());

    System.out.printf("\n-------  Running with Area: # pts = %d  -  # lines = %d  -  # Iter = %d\n",
        npts, NUM_CASES, N_ITER);
  }
  
  private Geometry[] createTestLines(int nLines, Envelope env) {
    double yInc = env.getHeight() / (nLines + 1);
    Geometry[] geoms = new Geometry[ NUM_CASES ];
    for (int i = 0; i < nLines; i++) {
      double y = env.getMinY() + i * yInc;
      double x0 = env.getMinX();
      double x1 = env.getMaxX();
      
      Coordinate[] pts = new Coordinate[] { new Coordinate(x0, y), new Coordinate(x1, y) };
      LineString line = geomFactory.createLineString(pts);
      geoms[i] = line;
    }
    return geoms;
  }
  
  public void runClip()
  {
    if (clipper == null) {
      clipper = new AreaLineClipper(areaGeom);
    }
    for (Geometry line : lines) {
      clipper.getResult(line);
    }
  } 
  
  public void runIntersection()
  {
    for (Geometry line : lines) {
      areaGeom.intersection(line);
    }
  }  

  public void xrunIntersectionNG()
  {
    for (Geometry line : lines) {
      OverlayNG.overlay(areaGeom, line, OverlayNG.INTERSECTION);
    }
  }  

}

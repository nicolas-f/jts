package org.locationtech.jts.operation.overlayng;

import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Quadrant;

public class AreaLineEdge implements Comparable {

  public static int INDEX_AREA = 0;
  public static int INDEX_LINE = 1;
  
  private Coordinate orig;
  private Coordinate dest;
  private boolean isForward;
  private OverlayLabel label;
  

  public AreaLineEdge(Coordinate orig, Coordinate dest, boolean isLine, boolean isForward) {
    this.orig = orig;
    this.dest = dest;
    
    this.isForward = isForward;
    if (isLine) {
      label = new OverlayLabel(INDEX_LINE);
    }
    else {
      int locLeft = Location.EXTERIOR;
      int locRight = Location.INTERIOR;
      if (! isForward) {
        locLeft = Location.EXTERIOR;
        locRight = Location.EXTERIOR;     
      }
      label = new OverlayLabel(INDEX_AREA, locLeft, locRight, false);
    }
  }

  public boolean isLine(boolean isForward) {
    return isLine() && isForward == this.isForward;
  }

  public boolean isLine() {
    return label.isLine(INDEX_LINE);
  }

  public boolean isAreaBoundary() {
    return label.isBoundary(INDEX_AREA);
  }

  public int getAreaLocation(int position) {
    return label.getLocation(INDEX_AREA, position, isForward);
  }
  
  /**
   * The X component of the direction vector.
   * 
   * @return the X component of the direction vector
   */
  private double directionX() { return dest.getX() - orig.getX(); }
  
  /**
   * The Y component of the direction vector.
   * 
   * @return the Y component of the direction vector
   */
  private double directionY() { return dest.getY() - orig.getY(); }
  
  @Override
  public int compareTo(Object o) {
    AreaLineEdge e = (AreaLineEdge) o;
    int comp = compareAngularDirection(e);
    return comp;
  }

  /**
   * Implements the total order relation:
   * <p>
   *    The angle of edge a is greater than the angle of edge b,
   *    where the angle of an edge is the angle made by 
   *    the first segment of the edge with the positive x-axis
   * <p>
   * When applied to a list of edges originating at the same point,
   * this produces a CCW ordering of the edges around the point.
   * <p>
   * Using the obvious algorithm of computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff error.
   * A robust algorithm is:
   * <ul>
   * <li>First, compare the quadrants the edge vectors lie in.  
   * If the quadrants are different, 
   * it is trivial to determine which edge has a greater angle.
   * 
   * <li>if the vectors lie in the same quadrant, the 
   * {@link Orientation#index(Coordinate, Coordinate, Coordinate)} function
   * can be used to determine the relative orientation of the vectors.
   * </ul>
   */
  public int compareAngularDirection(AreaLineEdge e)
  {
    double dx = directionX();
    double dy = directionY();
    double dx2 = e.directionX();
    double dy2 = e.directionY();
    
    // same vector
    if (dx == dx2 && dy == dy2)
      return 0;
    
    int quadrant = Quadrant.quadrant(dx, dy);
    int quadrant2 = Quadrant.quadrant(dx2, dy2);

    /**
     * If the direction vectors are in different quadrants, 
     * that determines the ordering
     */
    if (quadrant > quadrant2) return 1;
    if (quadrant < quadrant2) return -1;
    
    //--- vectors are in the same quadrant
    // Check relative orientation of direction vectors
    // this is > e if it is CCW of e
    Coordinate dir1 = dest;
    Coordinate dir2 = e.dest;
    return Orientation.index(e.orig, dir2, dir1);
  }



}

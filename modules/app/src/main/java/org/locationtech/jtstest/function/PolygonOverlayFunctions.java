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

package org.locationtech.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.LinearComponentExtracter;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.noding.snapround.GeometryNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;


public class PolygonOverlayFunctions 
{

  public static Geometry overlaySnapRounded(Geometry g1, Geometry g2, double precisionTol)
  {
    PrecisionModel pm = new PrecisionModel(precisionTol);
    GeometryFactory geomFact = g1.getFactory();
    
    List lines = LinearComponentExtracter.getLines(g1);
    // add second input's linework, if any
    if (g2 != null)
      LinearComponentExtracter.getLines(g2, lines);
    List nodedLinework = new GeometryNoder(pm).node(lines);
    // union the noded linework to remove duplicates
    Geometry nodedDedupedLinework = geomFact.buildGeometry(nodedLinework).union();
    
    // polygonize the result
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add(nodedDedupedLinework);
    Collection polys = polygonizer.getPolygons();
    
    // convert to collection for return
    Polygon[] polyArray = GeometryFactory.toPolygonArray(polys);
    return geomFact.createGeometryCollection(polyArray);
  }

  /**
   * Computes an overlay of a single polygon set, with parentage for the resultants.
   * Hole polygons are removed.
   * 
   * @param input the input polygons
   * @param precisionTol the precision tolerance to use for snap-rounding
   * @return the resultant polygons, with parentage
   */
  public static Geometry overlaySnapRoundedWithParentage(Geometry input, double precisionTol)
  {
  	Geometry resultants = overlaySnapRounded(input, null, precisionTol);
  	return parentage(input, resultants);
  }
  
  /**
   * Computes parentage for a set of overlay resultant polygons.
   * Parent ids are provided as a blank-separated string in the resultant user data. 
   * Hole resultants are eliminated.
   * 
   * @param input
   * @param resultants
   * @return GeometryCollection of resultants
   */
  public static Geometry parentage(Geometry input, Geometry resultants) {
	  STRtree polyIndex = new STRtree();
	  // load input polygons into index
	  for (int i = 0; i < input.getNumGeometries(); i++) {
		  Geometry g = input.getGeometryN(i);
		  if (g instanceof Polygon) {
			  ParentPoly pp = new ParentPoly(i, (Polygon) g);
			  polyIndex.insert(g.getEnvelopeInternal(), pp);
		  }
	  }
	  List<Polygon> resultantValid = computeParentage(resultants, polyIndex);
	  return input.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(resultantValid));
  }

	private static List<Polygon> computeParentage(Geometry resultants, STRtree polyIndex) {
		List<Polygon> resultantValid = new ArrayList<Polygon>();
		for (int i = 0; i < resultants.getNumGeometries(); i++) {
			Polygon r = (Polygon) resultants.getGeometryN(i);

			Point rpt = r.getInteriorPoint();

			String tag = computeParentageTag(rpt, polyIndex);
			if (tag != null) {
				r.setUserData(tag);
				resultantValid.add(r);
			}
		}
		return resultantValid;
	}

	private static String computeParentageTag(Point rpt, STRtree polyIndex) {
		String tag = null;
		List<ParentPoly> candidates = polyIndex.query(rpt.getEnvelopeInternal());
		for (ParentPoly pp : candidates) {
			if (pp.contains(rpt)) {
				if (tag == null) {
					tag = Integer.toString(pp.index);
				}
				else {
					tag += " " + pp.index;
				}
			}
		}
		return tag;
	}
  
	private static class ParentPoly {
		int index;
		Polygon poly;
		IndexedPointInAreaLocator loc;

		public ParentPoly(int index, Polygon poly) {
			this.index = index;
			this.poly = poly;
			loc = new IndexedPointInAreaLocator(poly);
		}

		public boolean contains(Point rpt) {
			return Location.INTERIOR == loc.locate(rpt.getCoordinate());
		}
	}
}

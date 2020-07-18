/*
 * Copyright (c) 2020 Martin Davis.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jts.operation.clip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentNode;
import org.locationtech.jts.noding.SegmentNodeList;
import org.locationtech.jts.noding.SegmentString;

/**
 * Clips a linear geometry to an area geometry in a performant way.
 * In order to provide faster performance,  
 * some of the full overlay output semantics are not provided:
 * <ul>
 * <li>Output lines are not merged
 * <li>Output lines may contain coincident linework
 * </ul>
 * 
 * @author mdavis
 *
 */
public class AreaLineClipper {

  public static Geometry clip(Geometry area, Geometry line) {
    AreaLineClipper clipper = new AreaLineClipper(area);
    return clipper.getResult(line);
  }
  
  private Geometry polyGeom;
  private GeometryFactory geomFactory;
  private AreaLineNoder noder;

  public AreaLineClipper(Geometry polyGeom) {
    this.polyGeom = polyGeom;
    //this.polyCoords = polyGeom.getCoordinates();
    this.geomFactory = polyGeom.getFactory();
    noder = new AreaLineNoder(polyGeom);
  }
  
  public Geometry getResult(Geometry lineGeom) {
    if (lineGeom.getEnvelopeInternal().disjoint(polyGeom.getEnvelopeInternal())) {
      return createEmptyLine();
    }
    // TODO: Handle MultiLineString
    // TODO: remove repeated points from line?
    
    List<LineString> resultLines = new ArrayList<LineString>();
    for (int i = 0; i < lineGeom.getNumGeometries(); i++) {
      addResultLines(lineGeom.getGeometryN(i), resultLines);
    }
    return buildResult(resultLines);
  }

  private Geometry createEmptyLine() {
    return geomFactory.createEmpty(1);
  }

  private void addResultLines(Geometry lineGeom, List<LineString> resultLines) {
    Map<SegmentNode, AreaLineNode> nodeMap = new HashMap<SegmentNode, AreaLineNode>();

    NodedSegmentString lineSS = noder.node(lineGeom, nodeMap);
    Collection<AreaLineNode> nodes = nodeMap.values();
    mergeAndLabel(nodes);
    
    addResultLines(lineSS, nodeMap, resultLines);
  }

  private List<LineString> addResultLines(NodedSegmentString lineSS, Map<SegmentNode, AreaLineNode> nodeMap, List<LineString> resultLines) {
    SegmentNodeList segNodeList = lineSS.getNodeList();
    
    /**
     * If no nodes found just return original line
     */
    if (nodeMap.size() == 0) {
      // TODO: check whether outside
      // Line must be fully inside area
      resultLines.add(createLine(lineSS));
      return resultLines;
    }
    
    List<SegmentString> nodedEdges = new ArrayList<SegmentString>();
    segNodeList.addSplitEdges(nodedEdges);
    
    /*
    if (nodedEdges.size() == 1) {
      resultLines.add(createLine(nodedEdges.get(0)));
      return resultLines;
    }
    */
    
    Iterator it = segNodeList.iterator();
    SegmentNode snStart = (SegmentNode) it.next();
    int i = 0;
    while (it.hasNext()) {
      SegmentNode snEnd = (SegmentNode) it.next();
      SegmentString ss = nodedEdges.get(i);
      
      AreaLineNode lineNodeStart = nodeMap.get(snStart);
      AreaLineNode lineNodeEnd = nodeMap.get(snEnd);
      AreaLineNode topoNode = lineNodeEnd;
      boolean isForward = false;
      if (topoNode == null) {
        topoNode = lineNodeStart;
        isForward = true;
      }
      
      //TODO: check that start and end nodes give identical result tests (if both are available)
        
      boolean isInResult = topoNode.isInterior(isForward);
      if (isInResult) {
        resultLines.add(createLine(ss));
      }
      
      snStart = snEnd;
      i++;
    }
    return resultLines;
  }

  private Geometry buildResult(List<LineString> resultLines) {
    return geomFactory.buildGeometry(resultLines);
  }

  private LineString createLine(SegmentString ss) {
    Coordinate[] pts = ss.getCoordinates();
    return geomFactory.createLineString(pts);
  }

  private void mergeAndLabel(Collection<AreaLineNode> nodes) {
    for (AreaLineNode node : nodes) {
      node.mergeAndLabel();
    }
  }
  

  
}

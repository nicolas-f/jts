package org.locationtech.jts.index.rtree;

import java.util.List;

public interface Node {
  
  boolean hasChildren();
  
  int size();

  boolean isEmpty();

  List<Node> getChildren();
}

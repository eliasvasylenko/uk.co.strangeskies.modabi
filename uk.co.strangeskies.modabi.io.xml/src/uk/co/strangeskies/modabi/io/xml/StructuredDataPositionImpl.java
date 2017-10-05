package uk.co.strangeskies.modabi.io.xml;

import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.modabi.io.structured.StructuredDataPosition;

public class StructuredDataPositionImpl implements StructuredDataPosition {
  private boolean done;
  private final List<Integer> indices;

  public StructuredDataPositionImpl() {
    indices = Collections.emptyList();
  }

  @Override
  public int getDepth() {
    return indices.size();
  }

  @Override
  public int getIndex(int depth) {
    return depth == indices.size() ? (done ? 1 : 0) : indices.get(indices.size() - 1 - depth);
  }

  public void push() {

  }

  public void pop() {
    indices.remove(indices.size() - 1);
    if (!indices.isEmpty()) {
      indices.add(indices.remove(indices.size() - 1) + 1);
    } else {
      done = true;
    }
  }
}

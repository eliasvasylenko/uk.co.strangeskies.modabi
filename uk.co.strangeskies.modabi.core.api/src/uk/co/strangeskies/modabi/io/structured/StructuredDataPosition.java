package uk.co.strangeskies.modabi.io.structured;

public interface StructuredDataPosition {
  int getDepth();

  int getIndex(int depth);

  default int getIndex() {
    return getIndex(getDepth());
  }
}

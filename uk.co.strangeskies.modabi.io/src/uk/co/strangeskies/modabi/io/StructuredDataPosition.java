package uk.co.strangeskies.modabi.io;

public interface StructuredDataPosition {
  int getDepth();

  int getIndex(int depth);

  default int getIndex() {
    return getIndex(getDepth());
  }
}

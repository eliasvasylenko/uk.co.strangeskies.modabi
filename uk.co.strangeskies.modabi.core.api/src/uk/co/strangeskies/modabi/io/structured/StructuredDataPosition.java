package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.modabi.QualifiedName;

public interface StructuredDataPosition {
  int getDepth();

  int getIndex(int depth);

  QualifiedName getName(int depth);
}

package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface Model<T> {
  QualifiedName name();

  boolean export();

  TypeToken<T> dataType();

  Node rootNode();

  Model<? super T> baseModel();
}

package uk.co.strangeskies.modabi.schema;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface Model<T> extends Node<T> {
  QualifiedName name();

  TypeToken<T> dataType();

  /**
   * @return the set of all <em>direct</em> base models, i.e. excluding those
   *         which are transitively implied via other more specific base models
   */
  Stream<Model<?>> baseModel();

  Schema schema();

  boolean export();
}

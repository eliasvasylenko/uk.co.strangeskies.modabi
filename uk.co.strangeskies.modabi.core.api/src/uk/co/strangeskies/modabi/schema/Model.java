package uk.co.strangeskies.modabi.schema;

import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface Model<T> {
  QualifiedName name();

  boolean export();

  TypeToken<T> dataType();

  Node<T> rootNode();

  /**
   * 
   * TODO base models are checked to make sure they come from either:
   * 
   * - the same in-progress batch of Schema, as determined by some internal
   * mechanism.
   * 
   * - one of the schemata in the dependencies of the schema being built.
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * @return the set of all <em>direct</em> base models, i.e. excluding those
   *         which are transitively implied via other more specific base models
   */
  Stream<Model<?>> baseModels();
}

package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.parsing.Parser;

public interface Model<T> {
  QualifiedName name();

  boolean anonymous();

  /**
   * A partial model cannot be instantiated as it is considered incomplete. It
   * must be used as the base of another model. A partial model may describe
   * children which are not instantiable themselves, i.e. children which specify a
   * partial model and are not {@link Child#extensible() extensible}.
   * 
   * @return true if the model is partial
   */
  boolean partial();

  Permission permission();

  TypeToken<T> type();

  Model<? super T> baseModel();

  Parser<?> parser();

  Stream<Child<?>> children();

  Stream<Child<?>> descendents(List<QualifiedName> names);

  default Stream<Child<?>> descendents(QualifiedName... names) {
    return descendents(asList(names));
  }

  Child<?> child(QualifiedName name);

  Optional<?> providedValue();
}

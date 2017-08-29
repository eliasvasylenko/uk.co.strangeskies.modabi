package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.ModelFactory;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface SchemaBuilderDecorator extends SchemaBuilder {
  SchemaBuilder getComponent();

  void setComponent(SchemaBuilder component);

  default void withComponent(Function<SchemaBuilder, SchemaBuilder> action) {
    setComponent(action.apply(getComponent()));
  }

  @Override
  default Schema create() {
    return getComponent().create();
  }

  @Override
  default SchemaBuilder qualifiedName(QualifiedName name) {
    withComponent(c -> c.qualifiedName(name));
    return this;
  }

  @Override
  default SchemaBuilder imports(Collection<? extends Class<?>> imports) {
    withComponent(c -> c.imports(imports));
    return this;
  }

  @Override
  default SchemaBuilder dependencies(Collection<? extends Schema> dependencies) {
    withComponent(c -> c.dependencies(dependencies));
    return this;
  }

  @Override
  default ModelBuilder addModel() {
    return getComponent().addModel();
  }

  @Override
  default SchemaBuilder addModel(
      String name,
      Function<ModelBuilder, ModelFactory<?>> configuration) {
    withComponent(c -> c.addModel(name, configuration));
    return this;
  }

  @Override
  default <T> Model<T> generateModel(TypeToken<T> type) {
    return getComponent().generateModel(type);
  }
}

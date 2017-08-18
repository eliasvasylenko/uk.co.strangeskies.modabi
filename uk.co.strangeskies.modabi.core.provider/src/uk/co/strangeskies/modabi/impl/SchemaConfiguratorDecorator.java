package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.ModelFactory;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface SchemaConfiguratorDecorator extends SchemaConfigurator {
  SchemaConfigurator getComponent();

  @Override
  default Schema create() {
    return getComponent().create();
  }

  @Override
  default SchemaConfigurator qualifiedName(QualifiedName name) {
    getComponent().qualifiedName(name);
    return this;
  }

  @Override
  default SchemaConfigurator imports(Collection<? extends Class<?>> imports) {
    getComponent().imports(imports);
    return this;
  }

  @Override
  default SchemaConfigurator dependencies(Collection<? extends Schema> dependencies) {
    getComponent().dependencies(dependencies);
    return this;
  }

  @Override
  default ModelConfigurator addModel() {
    return getComponent().addModel();
  }

  @Override
  default SchemaConfigurator addModel(
      String name,
      Function<ModelConfigurator, ModelFactory<?>> configuration) {
    getComponent().addModel(name, configuration);
    return this;
  }

  @Override
  default <T> Model<T> generateModel(TypeToken<T> type) {
    return getComponent().generateModel(type);
  }
}

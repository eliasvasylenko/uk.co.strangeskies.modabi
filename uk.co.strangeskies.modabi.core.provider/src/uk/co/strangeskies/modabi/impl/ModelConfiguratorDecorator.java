package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfiguratorDecorator extends ModelConfigurator {
  ModelConfigurator getComponent();

  @Override
  default ModelConfigurator name(QualifiedName name) {
    getComponent().name(name);
    return this;
  }

  @Override
  default Optional<QualifiedName> getName() {
    return getComponent().getName();
  }

  @Override
  default ModelConfigurator export(boolean export) {
    getComponent().export(export);
    return this;
  }

  @Override
  default Optional<Boolean> getExport() {
    return getComponent().getExport();
  }

  @Override
  default <V> SchemaNodeConfigurator<V, Model<V>> type(TypeToken<V> dataType) {
    return getComponent().type(dataType);
  }

  @Override
  default <U> SchemaNodeConfigurator<U, Model<U>> baseModel(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return getComponent().baseModel(type, baseModel);
  }

  @Override
  default Stream<Model<?>> getBaseModel() {
    return getComponent().getBaseModel();
  }
}

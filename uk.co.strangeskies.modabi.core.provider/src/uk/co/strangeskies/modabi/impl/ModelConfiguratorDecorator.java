package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfiguratorDecorator extends ModelBuilder {
  ModelBuilder getComponent();

  @Override
  default ModelBuilder name(QualifiedName name) {
    getComponent().name(name);
    return this;
  }

  @Override
  default Optional<QualifiedName> getName() {
    return getComponent().getName();
  }

  @Override
  default ModelBuilder export(boolean export) {
    getComponent().export(export);
    return this;
  }

  @Override
  default Optional<Boolean> getExport() {
    return getComponent().getExport();
  }

  @Override
  default <V> NodeBuilder<V, Model<V>> baseType(TypeToken<V> dataType) {
    return getComponent().baseType(dataType);
  }

  @Override
  default <U> NodeBuilder<U, Model<U>> baseModel(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return getComponent().baseModel(type, baseModel);
  }

  @Override
  default Stream<? extends Model<?>> getBaseModel() {
    return getComponent().getBaseModel();
  }

  @Override
  default Optional<TypeToken<?>> getBaseType() {
    return getComponent().getBaseType();
  }
}

package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelBuilder {
  Optional<QualifiedName> getName();

  ModelBuilder name(QualifiedName name);

  default ModelBuilder name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ModelBuilder export(boolean export);

  Optional<Boolean> getExport();

  default <U> NodeBuilder<U, Model<U>> baseModel(Model<U> baseModel) {
    return baseModel(baseModel.dataType(), baseModel);
  }

  default <U> NodeBuilder<U, Model<U>> baseModel(Class<U> type, Model<? super U> baseModel) {
    return baseModel(forClass(type), baseModel);
  }

  default <U> NodeBuilder<U, Model<U>> baseModel(TypeToken<U> type, Model<? super U> baseModel) {
    return baseModel(type, asList(baseModel));
  }

  default <U> NodeBuilder<U, Model<U>> baseModel(
      Class<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return baseModel(forClass(type), baseModel);
  }

  <U> NodeBuilder<U, Model<U>> baseModel(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel);

  Stream<? extends Model<?>> getBaseModel();

  Optional<TypeToken<?>> getBaseType();

  default <U> NodeBuilder<U, Model<U>> baseType(Class<U> type) {
    return baseType(forClass(type));
  }

  <U> NodeBuilder<U, Model<U>> baseType(TypeToken<U> type);
}

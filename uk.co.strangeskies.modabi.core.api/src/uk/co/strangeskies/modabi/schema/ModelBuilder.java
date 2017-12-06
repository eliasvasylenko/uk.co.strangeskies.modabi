package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelBuilder<T> {
  ModelBuilder<T> name(QualifiedName name);

  Optional<QualifiedName> getName();

  ModelBuilder<T> export(boolean export);

  Optional<Boolean> getExport();

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(Model<U> baseModel) {
    return rootNode(baseModel.dataType(), baseModel);
  }

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(Class<U> type, Model<? super U> baseModel) {
    return rootNode(forClass(type), baseModel);
  }

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(TypeToken<U> type, Model<? super U> baseModel) {
    return rootNode(type, asList(baseModel));
  }

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(
      Class<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return rootNode(forClass(type), baseModel);
  }

  <U> NodeBuilder<ModelBuilder<U>> rootNode(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel);

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(Class<U> type) {
    return rootNode(forClass(type));
  }

  <U> NodeBuilder<ModelBuilder<U>> rootNode(TypeToken<U> type);

  Optional<? extends NodeBuilder<?>> getRootNode();

  Stream<Model<? super T>> getBaseModel();

  Optional<TypeToken<T>> getDataType();

  default SchemaBuilder endModel() {
    return endModel(m -> {});
  }

  SchemaBuilder endModel(Consumer<Model<T>> completion);
}

package uk.co.strangeskies.modabi.schema.meta;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Optional;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.Model;
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

  <U> NodeBuilder<ModelBuilder<U>> rootNode(TypeToken<U> type, Model<? super U> baseModel);

  default <U> NodeBuilder<ModelBuilder<U>> rootNode(Class<U> type) {
    return rootNode(forClass(type));
  }

  <U> NodeBuilder<ModelBuilder<U>> rootNode(TypeToken<U> type);

  Optional<? extends NodeBuilder<?>> getRootNode();

  Optional<Model<? super T>> getBaseModel();

  Optional<TypeToken<T>> getDataType();

  default SchemaBuilder endModel() {
    return endModel(m -> {});
  }

  SchemaBuilder endModel(Consumer<Model<T>> completion);
}

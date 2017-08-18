package uk.co.strangeskies.modabi.schema;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModelConfigurator {
  Optional<QualifiedName> getName();

  ModelConfigurator name(QualifiedName name);

  default ModelConfigurator name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ModelConfigurator export(boolean export);

  Optional<Boolean> getExport();

  default <U> SchemaNodeConfigurator<U, Model<U>> baseModel(Model<U> baseModel) {
    return baseModel(baseModel.dataType(), baseModel);
  }

  default <U> SchemaNodeConfigurator<U, Model<U>> baseModel(
      Class<U> type,
      Model<? super U> baseModel) {
    return baseModel(forClass(type), baseModel);
  }

  default <U> SchemaNodeConfigurator<U, Model<U>> baseModel(
      TypeToken<U> type,
      Model<? super U> baseModel) {
    return baseModel(type, asList(baseModel));
  }

  default <U> SchemaNodeConfigurator<U, Model<U>> baseModel(
      Class<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return baseModel(forClass(type), baseModel);
  }

  <U> SchemaNodeConfigurator<U, Model<U>> baseModel(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel);

  Stream<Model<?>> getBaseModel();

  default <U> SchemaNodeConfigurator<U, Model<U>> type(Class<U> dataType) {
    return type(forClass(dataType));
  }

  <U> SchemaNodeConfigurator<U, Model<U>> type(TypeToken<U> dataType);
}

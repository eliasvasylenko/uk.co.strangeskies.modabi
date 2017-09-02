package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelBuilderImpl<T> implements ModelBuilder<T> {
  private final SchemaBuilderImpl schema;
  private QualifiedName name;
  private boolean export;

  private Set<Model<? super T>> baseModel;
  private TypeToken<T> baseType;

  public ModelBuilderImpl(SchemaBuilderImpl schema) {
    this.schema = schema;
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public ModelBuilder<T> name(QualifiedName name) {
    this.name = name;
    return this;
  }

  @Override
  public ModelBuilder<T> export(boolean export) {
    this.export = export;
    return this;
  }

  @Override
  public Optional<Boolean> getExport() {
    return Optional.ofNullable(export);
  }

  protected <U> NodeBuilder<U, ModelBuilder<U>> baseModelImpl(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    @SuppressWarnings("unchecked")
    ModelBuilderImpl<U> modelBuilder = (ModelBuilderImpl<U>) this;
    modelBuilder.baseType = type;
    modelBuilder.baseModel = new HashSet<>(baseModel);
    return new NodeBuilderImpl<>(modelBuilder);
  }

  @Override
  public <U> NodeBuilder<U, ModelBuilder<U>> rootNode(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return baseModelImpl(requireNonNull(type), requireNonNull(baseModel));
  }

  @Override
  public Stream<Model<? super T>> getBaseModel() {
    return baseModel.stream();
  }

  @Override
  public <U> NodeBuilder<U, ModelBuilder<U>> rootNode(TypeToken<U> type) {
    return baseModelImpl(requireNonNull(type), emptySet());
  }

  public NodeImpl<T> getRootNode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<TypeToken<T>> getBaseType() {
    return Optional.ofNullable(baseType);
  }

  public <U> OverrideBuilder<U> overrideModelChildren(
      Function<Model<T>, ? extends U> node,
      Function<ModelBuilder<?>, Optional<? extends U>> builder) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SchemaBuilder endModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SchemaBuilder endModel(Consumer<Model<T>> completion) {
    // TODO Auto-generated method stub
    return null;
  }
}

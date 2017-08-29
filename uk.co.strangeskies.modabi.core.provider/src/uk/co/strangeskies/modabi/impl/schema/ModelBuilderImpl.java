package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelBuilderImpl<T, E> extends NodeBuilderImpl<T, E> implements ModelBuilder {
  private final Schema schema;
  private QualifiedName name;
  private boolean export;

  private Set<Model<? super T>> baseModel;
  private TypeToken<T> baseType;

  public ModelBuilderImpl(Schema schema) {
    super(null);
    this.schema = schema;
  }

  public Schema getSchema() {
    return schema;
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public ModelBuilder name(QualifiedName name) {
    this.name = name;
    return this;
  }

  @Override
  public ModelBuilder export(boolean export) {
    this.export = export;
    return this;
  }

  @Override
  public Optional<Boolean> getExport() {
    return Optional.ofNullable(export);
  }

  protected <U> NodeBuilder<U, Model<U>> baseModelImpl(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    @SuppressWarnings("unchecked")
    ModelBuilderImpl<U, Model<U>> nodeBuilder = (ModelBuilderImpl<U, Model<U>>) this;
    nodeBuilder.baseType = type;
    nodeBuilder.baseModel = new HashSet<>(baseModel);
    return nodeBuilder;
  }

  @Override
  public <U> NodeBuilder<U, Model<U>> baseModel(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return baseModelImpl(requireNonNull(type), requireNonNull(baseModel));
  }

  @Override
  public Stream<Model<? super T>> getBaseModel() {
    return baseModel.stream();
  }

  @Override
  public <U> NodeBuilder<U, Model<U>> baseType(TypeToken<U> type) {
    return baseModelImpl(requireNonNull(type), emptySet());
  }

  @Override
  public Optional<TypeToken<?>> getBaseType() {
    return Optional.ofNullable(baseType);
  }

  public <U> OverrideBuilder<U> overrideModelChildren(
      Function<Model<T>, ? extends U> node,
      Function<ModelBuilder, Optional<? extends U>> builder) {
    // TODO Auto-generated method stub
    return null;
  }
}

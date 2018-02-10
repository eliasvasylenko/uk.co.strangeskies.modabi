package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.impl.NodeBuilderImpl.OverriddenNode;
import uk.co.strangeskies.modabi.schema.impl.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.meta.ModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.NodeBuilder;
import uk.co.strangeskies.modabi.schema.meta.SchemaBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ModelBuilderImpl<T> implements ModelBuilder<T> {
  private final SchemaBuilderImpl schema;
  private final QualifiedName name;
  private final Boolean export;

  private final Set<Model<? super T>> baseModel;
  private final TypeToken<T> dataType;
  private final OverriddenNode rootNode;

  public ModelBuilderImpl(SchemaBuilderImpl schema) {
    this.schema = schema;
    name = null;
    export = null;
    baseModel = emptySet();
    dataType = null;
    rootNode = null;
  }

  public ModelBuilderImpl(
      SchemaBuilderImpl schema,
      QualifiedName name,
      Boolean export,
      Set<Model<? super T>> baseModels,
      TypeToken<T> dataType,
      OverriddenNode rootNode) {
    this.schema = schema;
    this.name = name;
    this.export = export;
    this.baseModel = baseModels;
    this.dataType = dataType;
    this.rootNode = rootNode;
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public ModelBuilder<T> name(QualifiedName name) {
    return new ModelBuilderImpl<>(schema, name, export, baseModel, dataType, rootNode);
  }

  @Override
  public ModelBuilder<T> export(boolean export) {
    return new ModelBuilderImpl<>(schema, name, export, baseModel, dataType, rootNode);
  }

  @Override
  public Optional<Boolean> getExport() {
    return Optional.ofNullable(export);
  }

  protected <U> NodeBuilder<ModelBuilder<U>> rootNodeImpl(
      TypeToken<U> dataType,
      Collection<? extends Model<? super U>> baseModel) {
    return new NodeBuilderImpl<>(new NodeBuilderContext<ModelBuilder<U>>() {
      @Override
      public FunctionalExpressionCompiler expressionCompiler() {
        return schema.getExpressionCompiler();
      }

      @Override
      public Optional<Namespace> namespace() {
        return getName().map(QualifiedName::getNamespace);
      }

      @Override
      public Stream<Node> overrideNode() {
        return baseModel.stream().map(Model::rootNode);
      }

      @Override
      public ModelBuilder<U> endNode(NodeBuilderImpl<?> rootNode) {
        return new ModelBuilderImpl<>(
            schema,
            name,
            export,
            new HashSet<>(baseModel),
            dataType,
            new OverriddenNode(rootNode));
      }
    });
  }

  @Override
  public <U> NodeBuilder<ModelBuilder<U>> rootNode(
      TypeToken<U> type,
      Collection<? extends Model<? super U>> baseModel) {
    return rootNodeImpl(requireNonNull(type), requireNonNull(baseModel));
  }

  @Override
  public Stream<Model<? super T>> getBaseModel() {
    return baseModel.stream();
  }

  @Override
  public <U> NodeBuilder<ModelBuilder<U>> rootNode(TypeToken<U> type) {
    return rootNodeImpl(requireNonNull(type), emptySet());
  }

  @Override
  public Optional<NodeBuilderImpl<?>> getRootNode() {
    return Optional.ofNullable(rootNode).map(n -> n.builder);
  }

  protected Optional<NodeImpl> getRootNodeImpl() {
    return Optional.ofNullable(rootNode).map(n -> n.node);
  }

  @Override
  public Optional<TypeToken<T>> getDataType() {
    return Optional.ofNullable(dataType);
  }

  public <U> OverrideBuilder<U> overrideModelChildren(
      Function<Model<? super T>, ? extends U> node,
      Function<ModelBuilder<T>, Optional<? extends U>> builder) {
    return new OverrideBuilder<>(
        getBaseModel().map(node).collect(Collectors.toList()),
        builder.apply(this),
        () -> "unnamed property");
  }

  @Override
  public SchemaBuilder endModel(Consumer<Model<T>> completion) {
    ModelImpl<T> model = new ModelImpl<>(this);
    completion.accept(model);
    return schema.endModel(model);
  }
}

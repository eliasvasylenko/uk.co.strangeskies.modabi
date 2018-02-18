package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.impl.ChildBindingPointBuilderImpl.Child;
import uk.co.strangeskies.modabi.schema.impl.utilities.ChildBindingPointBuilderContext;
import uk.co.strangeskies.modabi.schema.impl.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.meta.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.meta.NodeBuilder;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

public class NodeBuilderImpl<E> implements NodeBuilder<E> {
  private final NodeBuilderContext<E> context;

  private final Boolean concrete;
  private final Object provided;

  private final Expression inputInitialization;
  private final Expression outputInitialization;

  private final List<ChildBindingPoint<?>> overriddenChildren;
  private final List<Child> children;

  public NodeBuilderImpl(NodeBuilderContext<E> context) {
    this.context = context;

    this.concrete = null;
    this.provided = null;

    this.inputInitialization = null;
    this.outputInitialization = null;

    this.overriddenChildren = context
        .overrideNode()
        .map(Node::children)
        .map(c -> c.collect(toList()))
        .orElse(emptyList());
    this.children = emptyList();
  }

  public NodeBuilderImpl(
      NodeBuilderContext<E> context,
      Boolean concrete,
      Object provided,
      Expression inputExpression,
      Expression outputExpression,
      List<ChildBindingPoint<?>> overriddenChildren,
      List<Child> children) {
    this.context = context;

    this.concrete = concrete;
    this.provided = provided;

    this.inputInitialization = inputExpression;
    this.outputInitialization = outputExpression;

    this.overriddenChildren = overriddenChildren;
    this.children = children;
  }

  @Override
  public Expression getInputInitialization() {
    return inputInitialization;
  }

  @Override
  public NodeBuilder<E> inputInitialization(Expression inputInitialization) {
    return new NodeBuilderImpl<>(
        context,
        concrete,
        provided,
        inputInitialization,
        outputInitialization,
        overriddenChildren,
        children);
  }

  @Override
  public Expression getOutputInitialization() {
    return outputInitialization;
  }

  @Override
  public NodeBuilder<E> outputInitialization(Expression outputInitialization) {
    return new NodeBuilderImpl<>(
        context,
        concrete,
        provided,
        inputInitialization,
        outputInitialization,
        overriddenChildren,
        children);
  }

  @Override
  public ChildBindingPointBuilder<NodeBuilder<E>> addChildBindingPoint() {
    return new ChildBindingPointBuilderImpl<>(
        new ChildBindingPointBuilderContext<NodeBuilder<E>>() {
          @Override
          public Optional<Namespace> namespace() {
            return context.namespace();
          }

          @Override
          public FunctionalExpressionCompiler expressionCompiler() {
            return context.expressionCompiler();
          }

          @Override
          public Optional<ChildBindingPoint<?>> overrideChild(QualifiedName id) {
            return overriddenChildren.stream().filter(c -> c.name().equals(id)).findFirst();
          }

          @Override
          public TypeToken<?> outputSourceType() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public TypeToken<?> inputTargetType() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public Imports imports() {
            return context.imports();
          }

          @Override
          public BoundSet boundSet() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public NodeBuilder<E> addChildResult(ChildBindingPointBuilderImpl<?> child) {
            List<Child> children = new ArrayList<>(NodeBuilderImpl.this.children.size() + 1);
            children.addAll(NodeBuilderImpl.this.children);
            children.add(new Child(child));
            return new NodeBuilderImpl<>(
                context,
                concrete,
                provided,
                inputInitialization,
                outputInitialization,
                overriddenChildren,
                children);
          }
        });
  }

  /*
   * 
   * 
   * 
   * TODO where did I land on this? do we need to keep the builder or can it just
   * be the child binding point itself?
   * 
   * We need to keep the builder if we want to be able to bind out a schema
   * 
   * Options:
   * 
   * 1) don't keep the builder here and don't allow us to bind out a schema or
   * schemabuilder
   * 
   * 2) do keep the builder here but discard it from the final Schema objects so
   * we can bind out from the schemabuilder but not the schema
   * 
   * 3) do keep the builder here and in the final schema objects so we can bind
   * out from the schemabuilder and schema
   * 
   * I think option 2) strikes the best balance. We don't need all binding
   * processes to be two way! Some may only support input, some may only support
   * output! This isn't necessarily something to hide from or a design failure!
   * 
   * We don't really need to have output binding for a schema object since they're
   * generally bound in from files and they're immutable.
   * 
   * It may be useful to have a graphical schema editor at some point, which means
   * some sort of output binding is necessary ... But this can function by having
   * output binding available on a schemabuilder not the schema itself.
   * 
   * 
   * 
   * 
   * So, INTERNALLY the NodeBuilder and ChildBindingPointBuilder will probably
   * wish to instantiate their respective Node and ChildBindingPoint objects when
   * the endNode() and endChild() methods are invoked respectively. But this isn't
   * technically a requirement as these classes will only be exposed by the API
   * once SchemaBuilder.create() is invoked. !!!!!! that may not be strictly true
   * because of SchemaBuilder endModel(Consumer<Model<T>> completion);...
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */
  @Override
  public List<ChildBindingPointBuilderImpl<?>> getChildBindingPoints() {
    return children.stream().map(c -> c.builder).collect(toList());
  }

  public List<ChildBindingPointImpl<?>> getChildBindingPointsImpl() {
    return children.stream().map(c -> c.child).collect(toList());
  }

  public <U> OverrideBuilder<U> overrideChildren(
      Function<Node, ? extends U> node,
      Function<NodeBuilder<?>, Optional<? extends U>> builder) {
    return new OverrideBuilder<>(
        () -> "unnamed property",
        context.overrideNode().map(node),
        builder.apply(this));
  }

  @Override
  public NodeBuilder<E> concrete(boolean concrete) {
    return new NodeBuilderImpl<>(
        context,
        concrete,
        provided,
        inputInitialization,
        outputInitialization,
        overriddenChildren,
        children);
  }

  @Override
  public Optional<Boolean> getConcrete() {
    return Optional.ofNullable(concrete);
  }

  @Override
  public E endNode() {
    return context.endNode(this);
  }

  @Override
  public NodeBuilder<E> provideValue(TypedObject<?> provided) {
    return new NodeBuilderImpl<>(
        context,
        concrete,
        provided,
        inputInitialization,
        outputInitialization,
        overriddenChildren,
        children);
  }

  @Override
  public Optional<?> getProvidedValue() {
    return Optional.ofNullable(provided);
  }

  static class OverriddenNode {
    public final NodeImpl node;
    public final NodeBuilderImpl<?> builder;

    public OverriddenNode(NodeBuilderImpl<?> builder) {
      this.node = new NodeImpl(builder);
      this.builder = builder;
    }
  }
}

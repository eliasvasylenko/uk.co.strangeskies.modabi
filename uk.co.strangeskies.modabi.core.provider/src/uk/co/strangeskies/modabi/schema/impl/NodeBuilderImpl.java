package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
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

  private final List<ChildBindingPoint<?>> inheritedChildren;
  private final List<Child> children;

  public NodeBuilderImpl(NodeBuilderContext<E> context) {
    this.context = context;

    this.concrete = null;
    this.provided = null;

    this.inheritedChildren = context
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
      List<ChildBindingPoint<?>> overriddenChildren,
      List<Child> children) {
    this.context = context;

    this.concrete = concrete;
    this.provided = provided;

    this.inheritedChildren = overriddenChildren;
    this.children = children;
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
          public Stream<ChildBindingPoint<?>> inheritedChildren() {
            return inheritedChildren.stream();
          }

          @Override
          public Optional<ChildBindingPoint<?>> previousChild() {
            return children.isEmpty()
                ? Optional.empty()
                : Optional.of(children.get(children.size() - 1).child);
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
            return new NodeBuilderImpl<>(context, concrete, provided, inheritedChildren, children);
          }

          @Override
          public TypeToken<?> parentType() {
            return context.getType();
          }
        });
  }

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
    return new NodeBuilderImpl<>(context, concrete, provided, inheritedChildren, children);
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
    return new NodeBuilderImpl<>(context, concrete, provided, inheritedChildren, children);
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

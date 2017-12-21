package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.streamOptional;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointBuilderContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.impl.NodeBuilderImpl.OverriddenNode;
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointBuilderImpl<E extends NodeBuilder<?>>
    implements ChildBindingPointBuilder<E> {
  private static final Expression NO_IO = v -> {};

  private final ChildBindingPointBuilderContext<E> context;

  private final QualifiedName name;
  private final Model<?> model;
  private final TypeToken<?> type;

  private final Expression input;
  private final Expression output;

  private final BindingConditionPrototype bindingCondition;
  private final Boolean ordered;

  private final OverriddenNode overriddenNode;

  public ChildBindingPointBuilderImpl(ChildBindingPointBuilderContext<E> context) {
    this.context = context;
    this.name = null;
    this.model = null;
    this.type = null;
    this.input = null;
    this.output = null;
    this.bindingCondition = null;
    this.ordered = null;
    this.overriddenNode = null;
  }

  public ChildBindingPointBuilderImpl(
      ChildBindingPointBuilderContext<E> context,
      QualifiedName name,
      Model<?> model,
      TypeToken<?> type,
      Expression inputExpression,
      Expression outputExpression,
      BindingConditionPrototype bindingCondition,
      Boolean ordered,
      OverriddenNode overriddenNode) {
    this.context = context;
    this.name = name;
    this.model = model;
    this.type = type;
    this.input = inputExpression;
    this.output = outputExpression;
    this.bindingCondition = bindingCondition;
    this.ordered = ordered;
    this.overriddenNode = overriddenNode;
  }

  protected Stream<ChildBindingPoint<?>> getOverriddenBindingPoints() {
    return getName().map(context::overrideChild).orElse(Stream.empty());
  }

  public <U> OverrideBuilder<U> overrideChildren(
      Function<? super ChildBindingPoint<?>, ? extends U> overriddenValues,
      Function<? super ChildBindingPointBuilder<E>, Optional<? extends U>> overridingValue) {
    return new OverrideBuilder<>(
        getOverriddenBindingPoints().map(overriddenValues::apply).collect(toList()),
        overridingValue.apply(this),
        () -> Methods.findMethod(ChildBindingPoint.class, overriddenValues::apply).getName());
  }

  @Override
  public ChildBindingPointBuilder<E> input(Expression input) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public ChildBindingPointBuilder<E> noInput() {
    return input(NO_IO);
  }

  @Override
  public Expression getInput() {
    return hasNoInput() ? null : input;
  }

  @Override
  public boolean hasNoInput() {
    return input == NO_IO;
  }

  @Override
  public ChildBindingPointBuilder<E> output(Expression output) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public ChildBindingPointBuilder<E> noOutput() {
    return output(NO_IO);
  }

  @Override
  public Expression getOutput() {
    return hasNoOutput() ? null : output;
  }

  protected FunctionalExpressionCompiler getExpressionCompiler() {
    return context.expressionCompiler();
  }

  @Override
  public boolean hasNoOutput() {
    return output == NO_IO;
  }

  @Override
  public final ChildBindingPointBuilder<E> name(String name) {
    return name(name, context.namespace().get());
  }

  @Override
  public ChildBindingPointBuilder<E> ordered(boolean ordered) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public Optional<Boolean> getOrdered() {
    return ofNullable(ordered);
  }

  @Override
  public ChildBindingPointBuilder<E> bindingCondition(BindingConditionPrototype bindingCondition) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public Optional<BindingConditionPrototype> getBindingCondition() {
    return ofNullable(bindingCondition);
  }

  @Override
  public E endChild() {
    return context.addChildResult(this);
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public ChildBindingPointBuilder<E> name(QualifiedName name) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public Optional<Model<?>> getModel() {
    return Optional.ofNullable(model);
  }

  @Override
  public Optional<TypeToken<?>> getType() {
    return Optional.ofNullable(type);
  }

  @Override
  public Optional<NodeBuilderImpl<?>> getNodeOverride() {
    return Optional.ofNullable(overriddenNode).map(n -> n.builder);
  }

  protected Optional<NodeImpl> getNodeOverrideImpl() {
    return Optional.ofNullable(overriddenNode).map(n -> n.node);
  }

  @Override
  public <U> ChildBindingPointBuilder<E> model(Model<? super U> model, TypeToken<U> type) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public <U> ChildBindingPointBuilder<E> type(TypeToken<U> type) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public <U> NodeBuilder<ChildBindingPointBuilder<E>> overrideNode() {
    return new NodeBuilderImpl<>(new NodeBuilderContext<ChildBindingPointBuilder<E>>() {
      @Override
      public FunctionalExpressionCompiler expressionCompiler() {
        return context.expressionCompiler();
      }

      @Override
      public Optional<Namespace> namespace() {
        return getName().map(QualifiedName::getNamespace);
      }

      @Override
      public Stream<Node> overrideNode() {
        return Stream
            .concat(
                streamOptional(getModel().map(Model::rootNode)),
                getOverriddenBindingPoints().map(ChildBindingPoint::override));
      }

      @Override
      public ChildBindingPointBuilder<E> endNode(NodeBuilderImpl<?> nodeBuilder) {
        return new ChildBindingPointBuilderImpl<>(
            context,
            name,
            model,
            type,
            input,
            output,
            bindingCondition,
            ordered,
            new OverriddenNode(nodeBuilder));
      }
    });
  }

  static class Child {
    public final ChildBindingPointImpl<?> child;
    public final ChildBindingPointBuilderImpl<?> builder;

    public Child(ChildBindingPointBuilderImpl<?> builder) {
      this.child = new ChildBindingPointImpl<>(builder);
      this.builder = builder;
    }
  }
}

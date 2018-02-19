package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Optional.ofNullable;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.impl.NodeBuilderImpl.OverriddenNode;
import uk.co.strangeskies.modabi.schema.impl.utilities.ChildBindingPointBuilderContext;
import uk.co.strangeskies.modabi.schema.impl.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.meta.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.meta.NodeBuilder;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointBuilderImpl<E extends NodeBuilder<?>>
    implements ChildBindingPointBuilder<E> {
  private static final Expression NO_IO = v -> {};

  private final ChildBindingPointBuilderContext<E> context;

  private final QualifiedName name;

  private final Boolean extensible;
  private final Model<?> model;
  private final TypeToken<?> type;

  private final TypeToken<?> inputTarget;
  private final TypeToken<?> outputSource;
  private final Expression input;
  private final Expression output;

  private final BindingConditionPrototype bindingCondition;
  private final Boolean ordered;

  private final OverriddenNode overriddenNode;

  public ChildBindingPointBuilderImpl(ChildBindingPointBuilderContext<E> context) {
    this.context = context;
    this.name = null;

    this.extensible = null;
    this.model = null;
    this.type = null;

    this.inputTarget = context
        .previousChild()
        .<TypeToken<?>>map(c -> c.inputExpression().getTypeAfter())
        .orElseGet(() -> forClass(void.class));
    this.outputSource = context
        .previousChild()
        .<TypeToken<?>>map(c -> c.inputExpression().getTypeAfter())
        .orElseGet(context::parentType);

    this.input = null;
    this.output = null;

    this.bindingCondition = null;
    this.ordered = null;
    this.overriddenNode = null;
  }

  public ChildBindingPointBuilderImpl(
      ChildBindingPointBuilderContext<E> context,
      QualifiedName name,
      Boolean extensible,
      Model<?> model,
      TypeToken<?> type,
      TypeToken<?> inputTarget,
      TypeToken<?> outputSource,
      Expression inputExpression,
      Expression outputExpression,
      BindingConditionPrototype bindingCondition,
      Boolean ordered,
      OverriddenNode overriddenNode) {
    this.context = context;
    this.name = name;
    this.extensible = extensible;
    this.model = model;
    this.type = type;
    this.inputTarget = inputTarget;
    this.outputSource = outputSource;
    this.input = inputExpression;
    this.output = outputExpression;
    this.bindingCondition = bindingCondition;
    this.ordered = ordered;
    this.overriddenNode = overriddenNode;
  }

  public TypeToken<?> getSourceType() {
    return outputSource;
  }

  public TypeToken<?> getTargetType() {
    return inputTarget;
  }

  protected Optional<ChildBindingPoint<?>> getOverriddenBindingPoint() {
    return getName().map(context::overrideChild).orElse(Optional.empty());
  }

  public <U> OverrideBuilder<U> overrideChildren(
      Function<? super ChildBindingPoint<?>, ? extends U> overriddenValues,
      Function<? super ChildBindingPointBuilder<E>, Optional<? extends U>> overridingValue) {
    return new OverrideBuilder<>(
        () -> Methods.findMethod(ChildBindingPoint.class, overriddenValues::apply).getName(),
        getOverriddenBindingPoint().map(overriddenValues::apply),
        overridingValue.apply(this));
  }

  @Override
  public ChildBindingPointBuilder<E> input(Expression input) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
  public Optional<Expression> getInput() {
    return hasNoInput() ? Optional.empty() : Optional.ofNullable(input);
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
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
  public Optional<Expression> getOutput() {
    return hasNoOutput() ? Optional.empty() : Optional.ofNullable(output);
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
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
        input,
        output,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public Optional<Boolean> getExtensible() {
    return Optional.ofNullable(extensible);
  }

  @Override
  public ChildBindingPointBuilder<E> extensible(boolean extensible) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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

  public Optional<NodeImpl> getNodeOverrideImpl() {
    return Optional.ofNullable(overriddenNode).map(n -> n.node);
  }

  @Override
  public <U> ChildBindingPointBuilder<E> model(Model<? super U> model, TypeToken<U> type) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
        extensible,
        model,
        type,
        inputTarget,
        outputSource,
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
      public Imports imports() {
        return context.imports();
      }

      @Override
      public Optional<Namespace> namespace() {
        return getName().map(QualifiedName::getNamespace);
      }

      @Override
      public Optional<Node> overrideNode() {
        return Stream
            .of(
                getModel().map(Model::rootNode),
                getOverriddenBindingPoint().map(ChildBindingPoint::override))
            .flatMap(StreamUtilities::streamOptional)
            .findFirst();
      }

      @Override
      public ChildBindingPointBuilder<E> endNode(NodeBuilderImpl<?> nodeBuilder) {
        return new ChildBindingPointBuilderImpl<>(
            context,
            name,
            extensible,
            model,
            type,
            inputTarget,
            outputSource,
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

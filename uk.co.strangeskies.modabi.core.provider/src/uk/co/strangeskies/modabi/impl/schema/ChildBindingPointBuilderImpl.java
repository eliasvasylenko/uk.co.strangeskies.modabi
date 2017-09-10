package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.streamOptional;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointBuilderContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.InputBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.OutputBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointBuilderImpl<E extends NodeBuilder<?>>
    implements ChildBindingPointBuilder<E> {
  private final ChildBindingPointBuilderContext context;

  private final QualifiedName name;
  private final Model<?> model;
  private final TypeToken<?> type;

  private final ValueExpression inputExpression;
  private final ValueExpression outputExpression;

  private final BindingConditionPrototype bindingCondition;
  private final Boolean ordered;

  private final NodeImpl overriddenNode;

  public ChildBindingPointBuilderImpl(ChildBindingPointBuilderContext context) {
    this.context = context;
    this.name = null;
    this.model = null;
    this.type = null;
    this.inputExpression = null;
    this.outputExpression = null;
    this.bindingCondition = null;
    this.ordered = null;
    this.overriddenNode = null;
  }

  public ChildBindingPointBuilderImpl(
      ChildBindingPointBuilderContext context,
      QualifiedName name,
      Model<?> model,
      TypeToken<?> type,
      ValueExpression inputExpression,
      ValueExpression outputExpression,
      BindingConditionPrototype bindingCondition,
      Boolean ordered,
      NodeImpl overriddenNode) {
    this.context = context;
    this.name = name;
    this.model = model;
    this.type = type;
    this.inputExpression = inputExpression;
    this.outputExpression = outputExpression;
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
  public InputBuilder<E> input() {
    return new InputBuilderImpl<>(
        inputExpression -> new ChildBindingPointBuilderImpl<>(
            context,
            name,
            model,
            type,
            inputExpression,
            outputExpression,
            bindingCondition,
            ordered,
            overriddenNode));
  }

  @Override
  public ValueExpression getInput() {
    return null;
  }

  @Override
  public OutputBuilder<E> output() {
    return new OutputBuilderImpl<>(
        outputExpression -> new ChildBindingPointBuilderImpl<>(
            context,
            name,
            model,
            type,
            inputExpression,
            outputExpression,
            bindingCondition,
            ordered,
            overriddenNode));
  }

  @Override
  public ValueExpression getOutput() {
    // TODO Auto-generated method stub
    return null;
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
        inputExpression,
        outputExpression,
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
        inputExpression,
        outputExpression,
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<QualifiedName> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public Optional<Node> getNode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChildBindingPointBuilder<E> name(QualifiedName name) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        inputExpression,
        outputExpression,
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
  public Optional<Node> getNodeOverride() {
    return Optional.ofNullable(overriddenNode);
  }

  @Override
  public <U> ChildBindingPointBuilder<E> model(Model<? super U> baseModel, TypeToken<U> type) {
    return new ChildBindingPointBuilderImpl<>(
        context,
        name,
        model,
        type,
        inputExpression,
        outputExpression,
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
        inputExpression,
        outputExpression,
        bindingCondition,
        ordered,
        overriddenNode);
  }

  @Override
  public <U> NodeBuilder<ChildBindingPointBuilder<E>> overrideNode() {
    return new NodeBuilderImpl<>(new NodeBuilderContext<ChildBindingPointBuilder<E>>() {
      @Override
      public Optional<Namespace> namespace() {
        return getName().map(QualifiedName::getNamespace);
      }

      @Override
      public Stream<Node> overrideNode() {
        return Stream.concat(
            streamOptional(getModel().map(Model::rootNode)),
            getOverriddenBindingPoints().map(ChildBindingPoint::override));
      }

      @Override
      public ChildBindingPointBuilder<E> endNode(NodeImpl overriddenNode) {
        return new ChildBindingPointBuilderImpl<>(
            context,
            name,
            model,
            type,
            inputExpression,
            outputExpression,
            bindingCondition,
            ordered,
            overriddenNode);
      }
    });
  }
}

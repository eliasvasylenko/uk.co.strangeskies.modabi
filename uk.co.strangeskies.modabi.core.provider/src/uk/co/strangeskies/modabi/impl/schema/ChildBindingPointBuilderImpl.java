package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointBuilderContext;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.BindingCondition;
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

public class ChildBindingPointBuilderImpl<T, E extends NodeBuilder<?, ?>>
    implements ChildBindingPointBuilder<T, E> {
  private final ChildBindingPointBuilderContext context;

  private final QualifiedName name;
  private final Model<? super T> model;
  private final TypeToken<T> type;

  private final ValueExpression inputExpression;
  private final ValueExpression outputExpression;

  private final BindingCondition bindingCondition;
  private final Boolean ordered;

  private final NodeImpl<T> overriddenNode;

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
      Model<? super T> model,
      TypeToken<T> type,
      ValueExpression inputExpression,
      ValueExpression outputExpression,
      BindingCondition bindingCondition,
      Boolean ordered,
      NodeImpl<T> overriddenNode) {
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
      Function<? super ChildBindingPointBuilder<T, E>, Optional<? extends U>> overridingValue) {
    return new OverrideBuilder<>(
        getOverriddenBindingPoints().map(overriddenValues::apply).collect(toList()),
        overridingValue.apply(this),
        () -> Methods.findMethod(ChildBindingPoint.class, overriddenValues::apply).getName());
  }

  @Override
  public InputBuilder input() {
    return new InputBuilderImpl();
  }

  @Override
  public OutputBuilder output() {
    return new OutputBuilderImpl();
  }

  @Override
  public final ChildBindingPointBuilder<T, E> name(String name) {
    return name(name, context.namespace().get());
  }

  @Override
  public ChildBindingPointBuilder<T, E> ordered(boolean ordered) {
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
  public ChildBindingPointBuilder<T, E> bindingCondition(
      BindingCondition bindingCondition) {
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
  public Optional<BindingCondition> getBindingCondition() {
    return ofNullable(bindingCondition);
  }

  @Override
  public E endChild() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<QualifiedName> getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<Node<?>> getNode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChildBindingPointBuilder<T, E> name(QualifiedName name) {
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
  public <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNode(
      TypeToken<U> type) {
    return overrideNodeImpl(type, null);
  }

  @Override
  public <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNode(
      TypeToken<U> type,
      Model<? super U> model) {
    return overrideNodeImpl(type, model);
  }

  protected <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNodeImpl(
      TypeToken<U> type,
      Model<? super U> model) {
    return new NodeBuilderImpl<>(new NodeBuilderContext<U, ChildBindingPointBuilder<U, E>>() {
      @Override
      public Optional<Namespace> namespace() {
        return getName().map(QualifiedName::getNamespace);
      }

      @Override
      public Stream<Node<? super U>> overrideNode() {
        return Stream.concat(
            Stream.of(model.rootNode()),
            getOverriddenBindingPoints().map(ChildBindingPoint::override));
      }

      @Override
      public ChildBindingPointBuilder<U, E> endNode(NodeImpl<U> overriddenNode) {
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

  @Override
  public Optional<Model<? super T>> getModel() {
    return Optional.ofNullable(model);
  }

  @Override
  public Optional<TypeToken<T>> getType() {
    return Optional.ofNullable(type);
  }

  @Override
  public Optional<Node<T>> getNodeOverride() {
    return Optional.ofNullable(overriddenNode);
  }
}

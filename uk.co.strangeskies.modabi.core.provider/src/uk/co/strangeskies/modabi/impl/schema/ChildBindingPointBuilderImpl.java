package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.ChildBindingPointConfigurationContext;
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
  private final ChildBindingPointConfigurationContext context;

  private ValueExpression inputExpression;
  private ValueExpression outputExpression;

  private BindingCondition<? super T> bindingCondition;
  private Boolean ordered;

  /*
   * A mapping from placeholder iteration item expressions to the iterable
   * expressions they come from.
   */
  private final Map<ValueExpression, ValueExpression> iterationExpressions;

  public ChildBindingPointBuilderImpl(ChildBindingPointBuilder<T, E> other) {
    context = null;

    iterationExpressions = new HashMap<>();
  }

  public ChildBindingPointBuilderImpl(ChildBindingPointConfigurationContext context) {
    this.context = context;

    /*
     * output
     */
    iterationExpressions = new HashMap<>();
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
    return name(name, context.namespace());
  }

  @Override
  public ChildBindingPointBuilder<T, E> ordered(boolean ordered) {
    this.ordered = ordered;
    return this;
  }

  @Override
  public Optional<Boolean> getOrdered() {
    return ofNullable(ordered);
  }

  @Override
  public ChildBindingPointBuilder<T, E> bindingCondition(BindingCondition<? super T> condition) {
    this.bindingCondition = condition;
    return this;
  }

  @Override
  public Optional<BindingCondition<? super T>> getBindingCondition() {
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <U> ChildBindingPointBuilder<U, E> model(Model<U> model) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <U> ChildBindingPointBuilder<U, E> type(Class<U> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <U> ChildBindingPointBuilder<U, E> type(TypeToken<U> dataType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NodeBuilder<T, ChildBindingPointBuilder<T, E>> overrideNode() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<Model<? super T>> getModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<TypeToken<T>> getType() {
    // TODO Auto-generated method stub
    return null;
  }
}

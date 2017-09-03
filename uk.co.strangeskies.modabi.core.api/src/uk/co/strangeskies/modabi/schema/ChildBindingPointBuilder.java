package uk.co.strangeskies.modabi.schema;

import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointBuilder<T, E extends NodeBuilder<?, ?>> {
  Optional<QualifiedName> getName();

  Optional<Node<?>> getNode();

  ChildBindingPointBuilder<T, E> name(QualifiedName name);

  ChildBindingPointBuilder<T, E> name(String name);

  default ChildBindingPointBuilder<T, E> name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ChildBindingPointBuilder<T, E> ordered(boolean ordered);

  Optional<Boolean> getOrdered();

  ChildBindingPointBuilder<T, E> bindingCondition(BindingCondition<? super T> condition);

  Optional<BindingCondition<? super T>> getBindingCondition();

  InputBuilder input();

  OutputBuilder output();

  <U extends T> ChildBindingPointBuilder<U, E> model(Model<U> model);

  Optional<Model<? super T>> getModel();

  default <U extends T> ChildBindingPointBuilder<U, E> type(Class<U> type) {
    return type(TypeToken.forClass(type));
  }

  <U extends T> ChildBindingPointBuilder<U, E> type(TypeToken<U> dataType);

  Optional<TypeToken<T>> getType();

  default ChildBindingPointBuilder<T, E> input(
      Function<InputBuilder, ValueExpression> inputExpression) {
    input().expression(inputExpression.apply(input()));
    return this;
  }

  default ChildBindingPointBuilder<T, E> output(
      Function<OutputBuilder, ValueExpression> outputExpression) {
    output().expression(outputExpression.apply(output()));
    return this;
  }

  /**
   * Override the {@link #model(Model)} to apply when binding to this binding
   * point.
   * 
   * @return a configuration object for the override
   */
  NodeBuilder<T, ChildBindingPointBuilder<T, E>> overrideNode();

  E endChild();
}

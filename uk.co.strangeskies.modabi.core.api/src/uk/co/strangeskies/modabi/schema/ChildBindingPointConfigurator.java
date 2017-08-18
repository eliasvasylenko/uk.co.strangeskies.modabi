package uk.co.strangeskies.modabi.schema;

import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.expression.Expression;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointConfigurator<T, E extends SchemaNodeConfigurator<?, ?>>
    extends ChildBindingPointFactory<E> {
  Optional<QualifiedName> getName();

  Optional<StructuralNode<?>> getNode();

  ChildBindingPointConfigurator<T, E> name(QualifiedName name);

  ChildBindingPointConfigurator<T, E> name(String name);

  default ChildBindingPointConfigurator<T, E> name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ChildBindingPointConfigurator<T, E> ordered(boolean ordered);

  Optional<Boolean> getOrdered();

  ChildBindingPointConfigurator<T, E> bindingCondition(BindingCondition<? super T> condition);

  Optional<BindingCondition<? super T>> getBindingCondition();

  InputConfigurator<T> input();

  OutputConfigurator<T> output();

  <U> ChildBindingPointConfigurator<U, E> model(Model<U> model);

  <U> ChildBindingPointConfigurator<U, E> type(Class<U> type);

  <U> ChildBindingPointConfigurator<U, E> type(TypeToken<U> dataType);

  default ChildBindingPointConfigurator<T, E> input(
      Function<InputConfigurator<T>, Expression> inputExpression) {
    input().expression(inputExpression.apply(input()));
    return this;
  }

  default ChildBindingPointConfigurator<T, E> output(
      Function<OutputConfigurator<T>, ValueExpression<T>> outputExpression) {
    output().expression(outputExpression.apply(output()));
    return this;
  }

  /**
   * Override the {@link #model(Model)} to apply when binding to this binding
   * point.
   * 
   * @return a configuration object for the override
   */
  SchemaNodeConfigurator<T, ChildBindingPointFactory<E>> override();
}

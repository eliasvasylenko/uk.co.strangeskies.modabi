package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointBuilder<E extends NodeBuilder<?, ?>> {
  Optional<QualifiedName> getName();

  Optional<Node<?>> getNode();

  ChildBindingPointBuilder<E> name(QualifiedName name);

  ChildBindingPointBuilder<E> name(String name);

  default ChildBindingPointBuilder<E> name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ChildBindingPointBuilder<E> ordered(boolean ordered);

  Optional<Boolean> getOrdered();

  ChildBindingPointBuilder<E> bindingCondition(BindingCondition condition);

  Optional<BindingCondition> getBindingCondition();

  InputBuilder input();

  OutputBuilder output();

  default ChildBindingPointBuilder<T, E> input(
      Function<InputBuilder, ValueExpression> inputExpression) {
    input().expression(inputExpression.apply(input()));
    return this;
  }

  ValueExpression getInput();

  default ChildBindingPointBuilder<T, E> output(
      Function<OutputBuilder, ValueExpression> outputExpression) {
    output().expression(outputExpression.apply(output()));
    return this;
  }

  ValueExpression getOutput();

  default <U extends T> ChildBindingPointBuilder<U, E> model(Model<U> model) {
    return model(model.dataType(), model);
  }

  default <U extends T> ChildBindingPointBuilder<U, E> model(
      Class<U> type,
      Model<? super U> model) {
    return model(forClass(type), model);
  }

  /**
   * Override the {@link #model(Model)} to apply when binding to this binding
   * point.
   * 
   * @return a configuration object for the override
   */
  <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNode(
      TypeToken<U> type,
      Model<? super U> baseModel);

  default <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNode(Class<U> type) {
    return overrideNode(forClass(type));
  }

  <U extends T> NodeBuilder<U, ChildBindingPointBuilder<U, E>> overrideNode(TypeToken<U> type);

  Optional<Node<T>> getNodeOverride();

  Optional<Model<? super T>> getModel();

  Optional<TypeToken<T>> getType();

  E endChild();
}

package uk.co.strangeskies.modabi.schema;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Optional;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBindingPointBuilder<E extends NodeBuilder<?>> {
  Optional<QualifiedName> getName();

  Optional<Node> getNode();

  ChildBindingPointBuilder<E> name(QualifiedName name);

  ChildBindingPointBuilder<E> name(String name);

  default ChildBindingPointBuilder<E> name(String name, Namespace namespace) {
    return name(new QualifiedName(name, namespace));
  }

  ChildBindingPointBuilder<E> ordered(boolean ordered);

  Optional<Boolean> getOrdered();

  ChildBindingPointBuilder<E> bindingCondition(BindingConditionPrototype condition);

  Optional<BindingConditionPrototype> getBindingCondition();

  ChildBindingPointBuilder<E> input(Expression expression);

  Expression getInput();

  ChildBindingPointBuilder<E> output(Expression expression);

  Expression getOutput();

  default <U> ChildBindingPointBuilder<E> model(Model<U> model) {
    return model(model, model.dataType());
  }

  default <U> ChildBindingPointBuilder<E> model(Model<? super U> model, Class<U> type) {
    return model(model, forClass(type));
  }

  /**
   * Override the {@link #model(Model)} to apply when binding to this binding
   * point.
   * 
   * @return a configuration object for the override
   */
  <U> ChildBindingPointBuilder<E> model(Model<? super U> baseModel, TypeToken<U> type);

  default <U> ChildBindingPointBuilder<E> type(Class<U> type) {
    return type(forClass(type));
  }

  <U> ChildBindingPointBuilder<E> type(TypeToken<U> type);

  <U> NodeBuilder<ChildBindingPointBuilder<E>> overrideNode();

  Optional<Node> getNodeOverride();

  Optional<Model<?>> getModel();

  Optional<TypeToken<?>> getType();

  E endChild();
}

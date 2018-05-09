package uk.co.strangeskies.modabi.schema.meta;

import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Type;
import java.util.Optional;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder.ChildrenStep;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ChildBuilder<E> {
  interface PropertiesStep<E> extends OverrideStep<E> {
    PropertiesStep<E> name(String name);

    PropertiesStep<E> ordered(boolean ordered);

    PropertiesStep<E> bindingConstraint(BindingConstraint condition);

    PropertiesStep<E> input(Expression expression);

    PropertiesStep<E> output(Expression expression);

    PropertiesStep<E> model(QualifiedName name);

    default PropertiesStep<E> type(Type type) {
      return type(forType(type));
    }

    PropertiesStep<E> type(TypeToken<?> type);
  }

  interface OverrideStep<E> extends ChildBuilder<E> {
    ChildrenStep<ChildBuilder<E>> overrideModel();
  }

  Optional<String> getName();

  Optional<Boolean> getOrdered();

  Optional<BindingConstraint> getBindingConstraint();

  Optional<Expression> getInput();

  Optional<Expression> getOutput();

  Optional<QualifiedName> getModel();

  Optional<TypeToken<?>> getType();

  Optional<AnonymousModelBuilder<?>> getModelOverride();

  E endChild();
}

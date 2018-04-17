package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.modabi.schema.BindingConstraintSpecification.allOf;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.BindingFunctionImpl;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.ChildLookup;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildImpl<T> implements Child<T> {
  private final int index;
  private final String name;

  private final TypeToken<T> type;
  private final Model<? super T> model;

  private final boolean ordered;
  private final BindingConstraint<T> condition;
  private final BindingFunction input;
  private final BindingFunction output;

  @SuppressWarnings("unchecked")
  protected ChildImpl(
      ChildBuilderImpl<?> configurator,
      List<? extends Child<?>> previousChildren,
      List<? extends Child<?>> overriddenChildren,
      TypeToken<?> parentType) {
    name = configurator.getName().orElse(null);

    Optional<? extends Child<?>> overriddenChild = getOverriddenChild(
        name,
        previousChildren,
        overriddenChildren);

    index = overriddenChild
        .map(Child::index)
        .orElseGet(() -> previousChildren.size() + overriddenChildren.size());

    ordered = configurator.getOrdered().or(() -> overriddenChild.map(Child::ordered)).orElse(false);

    Model<?> model = concat(
        configurator.getModelImpl().stream(),
        overriddenChild.map(Child::model).stream()).reduce((a, b) -> {
          StreamUtilities.<Model<?>>iterate(a, Model::baseModel).anyMatch(b::equals);
          return a;
        }).orElse(null);
    this.model = (Model<? super T>) model;

    /*
     * TODO how to determine the type
     * 
     * If the model is specified:
     * 
     * - if the type is given, use it
     * 
     * - type must be more specific than the give/inherited model type, infer the
     * actual model type to make sure it fits
     * 
     * - if the type is not given, use the type of the model
     * 
     * If the model is not specified:
     * 
     * - if the type is given, use it
     * 
     * - if the type is not given, use void
     */
    TypeToken<?> type = concat(
        configurator.getType().stream(),
        overriddenChild.map(Child::type).stream())
            .reduce((a, b) -> a.withConstraintTo(SUBTYPE, b))
            .or(() -> Optional.ofNullable(model).map(Model::type))
            .orElseGet(() -> forClass(void.class));
    this.type = (TypeToken<T>) type;
    System.out.println(this.name + ": " + this.type + "- " + this.model);

    ChildLookup childLookup = name -> name.isEmpty()
        ? Optional.of(this)
        : concat(
            previousChildren.stream(),
            overriddenChildren.stream().takeWhile(c -> c.index() < index))
                .filter(c -> c.name().equals(name))
                .findFirst();

    BindingConstraintSpecification conditionPrototype = concat(
        configurator.getBindingConstraint().stream(),
        overriddenChild.map(c -> c.bindingConstraint().getSpecification()).stream())
            .reduce((a, b) -> allOf(a, b))
            .orElseGet(() -> v -> v.required());
    // TODO shouldn't have to 'recompile' overridden constraint
    condition = new BindingConditionFactory<>(this.type, configurator.getExpressionCompiler())
        .create(conditionPrototype);

    // TODO deal with hasInput
    input = overriddenChild.map(c -> {
      if (configurator.getInput().isPresent()) {
        throw new ModabiSchemaException("Cannot override input " + this);
      }
      return c.inputExpression();
    }).orElseGet(() -> {
      Expression inputExpression = configurator.getInput().orElseGet(() -> {
        throw new UnsupportedOperationException(
            "Generate input expression from name / binding object");
      });
      TypeToken<?> objectType = previousChildren.isEmpty()
          ? forClass(void.class)
          : previousChildren.get(previousChildren.size() - 1).inputExpression().getTypeAfter();
      return new BindingFunctionImpl(
          childLookup,
          objectType,
          inputExpression,
          configurator.getExpressionCompiler());
    });

    // TODO deal with hasOutput
    output = overriddenChild.map(c -> {
      if (configurator.getOutput().isPresent()) {
        throw new ModabiSchemaException("Cannot override output " + this);
      }
      return c.outputExpression();
    }).orElseGet(() -> {
      Expression outputExpression = configurator.getOutput().orElseGet(() -> {
        throw new UnsupportedOperationException(
            "Generate input expression from name / binding object");
      });
      TypeToken<?> objectType = previousChildren.isEmpty()
          ? parentType
          : previousChildren.get(previousChildren.size() - 1).outputExpression().getTypeAfter();
      return new BindingFunctionImpl(
          childLookup,
          objectType,
          outputExpression,
          configurator.getExpressionCompiler());
    });
  }

  private static Optional<? extends Child<?>> getOverriddenChild(
      String name,
      List<? extends Child<?>> previousChildren,
      List<? extends Child<?>> overriddenChildren) {
    if (name != null) {
      if (previousChildren.stream().anyMatch(c -> name.equals(c.name()))) {
        throw new ModabiSchemaException(MESSAGES.childNameAlreadyExists(name));
      }

      return overriddenChildren.stream().filter(c -> name.equals(c.name())).findAny();
    } else {
      return Optional.empty();
    }
  }

  @Override
  public int index() {
    return index;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean ordered() {
    return ordered;
  }

  @Override
  public BindingConstraint<T> bindingConstraint() {
    return condition;
  }

  @Override
  public TypeToken<T> type() {
    return type;
  }

  @Override
  public Model<? super T> model() {
    return model;
  }

  @Override
  public BindingFunction inputExpression() {
    return input;
  }

  @Override
  public BindingFunction outputExpression() {
    return output;
  }
}

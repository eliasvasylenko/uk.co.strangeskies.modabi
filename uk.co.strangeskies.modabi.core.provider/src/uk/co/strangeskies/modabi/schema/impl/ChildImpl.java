package uk.co.strangeskies.modabi.schema.impl;

import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.modabi.schema.BindingConstraintSpecification.allOf;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.BindingFunctionContext;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.BindingFunctionImpl;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildImpl<T> implements Child<T> {
  private final String name;

  private final TypeToken<T> type;
  private final Model<? super T> model;

  private final boolean ordered;
  private final BindingConstraint<T> condition;
  private final BindingFunction input;
  private final BindingFunction output;

  @SuppressWarnings("unchecked")
  protected ChildImpl(ChildBuilderImpl<?> configurator, Child<?> overriddenChild) {
    Optional<Child<?>> overridden = Optional.ofNullable(overriddenChild);

    name = configurator.getName().orElse(null);

    ordered = configurator.getOrdered().or(() -> overridden.map(Child::ordered)).orElse(false);

    /*
     * TODO get proper type information for binding point, validate overrides, etc.
     */
    TypeToken<?> type = concat(
        configurator.getType().stream(),
        overridden.map(Child::type).stream())
            .reduce((a, b) -> a.withConstraintTo(SUBTYPE, b))
            .orElseThrow(() -> new ModabiSchemaException(""));
    this.type = (TypeToken<T>) type;

    Model<?> model = concat(
        configurator.getModelImpl().stream(),
        overridden.map(Child::model).stream()).reduce((a, b) -> {
          StreamUtilities.<Model<?>>iterate(a, Model::baseModel).anyMatch(b::equals);
          return a;
        }).orElseThrow(() -> new ModabiSchemaException(""));
    this.model = (Model<? super T>) model;

    BindingConstraintSpecification conditionPrototype = mergeOverride(
        configurator.getBindingConstraint(),
        overridden.map(c -> c.bindingConstraint().getSpecification()),
        (a, b) -> allOf(a, b)).orElseGet(() -> v -> v.required());
    // TODO shouldn't have to 'recompile' overridden constraint
    condition = new BindingConditionFactory<>(this.type, configurator.getExpressionCompiler())
        .create(conditionPrototype);

    // TODO deal with hasInput
    Expression inputExpression = validateOverride(
        configurator.getInput(),
        overridden.map(b -> b.inputExpression().getExpression()),
        (a, b) -> false).get();
    input = new BindingFunctionImpl(new BindingFunctionContext() {
      @Override
      public TypeToken<?> typeBefore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }

      @Override
      public TypeToken<?> typeAfter() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }

      @Override
      public ChildImpl<?> getChild(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }
    }, inputExpression, configurator.getExpressionCompiler());

    // TODO deal with hasOutput
    Expression outputExpression = validateOverride(
        configurator.getOutput(),
        overridden.map(b -> b.outputExpression().getExpression()),
        (a, b) -> false).get();
    output = new BindingFunctionImpl(new BindingFunctionContext() {
      @Override
      public TypeToken<?> typeBefore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }

      @Override
      public TypeToken<?> typeAfter() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }

      @Override
      public ChildImpl<?> getChild(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
      }
    }, outputExpression, configurator.getExpressionCompiler());
  }

  private static <T> Optional<T> validateOverride(
      Optional<? extends T> override,
      Optional<? extends T> overridden,
      BiPredicate<? super T, ? super T> object) {
    // TODO Auto-generated method stub
    return null;
  }

  private static <T> Optional<T> mergeOverride(
      Optional<? extends T> override,
      Optional<? extends T> overridden,
      BiFunction<? super T, ? super T, ? extends T> object) {
    // TODO Auto-generated method stub
    return null;
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

package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.BindingFunctionContext;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.BindingFunctionImpl;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildImpl<T> implements Child<T> {
  private final QualifiedName name;

  private final boolean extensible;
  private final TypeToken<T> type;
  private final Model<? super T> model;

  private final boolean ordered;
  private final BindingCondition<T> condition;
  private final BindingFunction input;
  private final BindingFunction output;

  @SuppressWarnings("unchecked")
  protected ChildImpl(ChildBuilderImpl<?> configurator) {
    Child<?> overriddenChild = null;

    name = overrideChildren(configurator, overriddenChild, Child::name, ChildBuilder::getName)
        .validateOverride(Objects::equals)
        .or(() -> configurator.getModel().orElse(null))
        .get();

    extensible = overrideChildren(
        configurator,
        overriddenChild,
        Child::extensible,
        ChildBuilder::getExtensible).validateOverride((a, b) -> true).orDefault(false).get();

    ordered = overrideChildren(
        configurator,
        overriddenChild,
        Child::ordered,
        ChildBuilder::getOrdered).validateOverride((a, b) -> a || !b).or(false).get();

    /*
     * TODO get proper type information for binding point, validate overrides, etc.
     */
    TypeToken<?> type = overrideChildren(
        configurator,
        overriddenChild,
        Child::type,
        ChildBuilder::getType).orDefault(forClass(Object.class)).get();
    this.type = (TypeToken<T>) type;

    model = overrideChildren(
        configurator,
        overriddenChild,
        Child::model,
        ChildBuilderImpl::getModelImpl)
            .validateOverride(
                (a, b) -> StreamUtilities
                    .<Model<?>>iterate(a, m -> m.baseModel())
                    .anyMatch(b::equals))
            .tryGet()
            .map(m -> (Model<? super T>) m)
            .orElse(null);

    BindingConditionPrototype conditionPrototype = overrideChildren(
        configurator,
        overriddenChild,
        b -> b.bindingCondition().getPrototype(),
        ChildBuilder::getBindingCondition)
            .mergeOverride((a, b) -> BindingConditionPrototype.allOf(a, b))
            .or(v -> v.required())
            .get();
    condition = new BindingConditionFactory<>(this.type, configurator.getExpressionCompiler())
        .create(conditionPrototype);

    // TODO deal with hasInput
    Expression inputExpression = overrideChildren(
        configurator,
        overriddenChild,
        b -> b.inputExpression().getExpression(),
        ChildBuilder::getInput).validateOverride((a, b) -> false).get();
    input = new BindingFunctionImpl(new BindingFunctionContext() {
      @Override
      public TypeToken<?> typeBefore() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<?> typeAfter() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ChildImpl<?> getChild(String name) {
        // TODO Auto-generated method stub
        return null;
      }
    }, inputExpression, configurator.getExpressionCompiler());

    // TODO deal with hasOutput
    Expression outputExpression = overrideChildren(
        configurator,
        overriddenChild,
        b -> b.outputExpression().getExpression(),
        ChildBuilder::getOutput).validateOverride((a, b) -> false).get();
    output = new BindingFunctionImpl(new BindingFunctionContext() {
      @Override
      public TypeToken<?> typeBefore() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<?> typeAfter() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public ChildImpl<?> getChild(String name) {
        // TODO Auto-generated method stub
        return null;
      }
    }, outputExpression, configurator.getExpressionCompiler());
  }

  private <U> OverrideBuilder<U> overrideChildren(
      ChildBuilderImpl<?> configurator,
      Child<?> overriddenChild,
      Function<Child<?>, ? extends U> overriddenValues,
      Function<ChildBuilderImpl<?>, Optional<? extends U>> overridingValue) {
    return new OverrideBuilder<>(
        () -> Methods.findMethod(Child.class, overriddenValues::apply).getName(),
        Optional.ofNullable(overriddenChild).map(overriddenValues::apply),
        overridingValue.apply(configurator));
  }

  @Override
  public QualifiedName name() {
    return name;
  }

  @Override
  public boolean ordered() {
    return ordered;
  }

  @Override
  public BindingCondition<T> bindingCondition() {
    return condition;
  }

  @Override
  public boolean extensible() {
    return extensible;
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

  @Override
  public Model<?> parent() {
    // TODO Auto-generated method stub
    return null;
  }
}

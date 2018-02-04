package uk.co.strangeskies.modabi.schema.impl;

import java.util.Objects;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.InputFunction;
import uk.co.strangeskies.modabi.schema.impl.bindingfunctions.OutputFunction;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointImpl<T> implements ChildBindingPoint<T> {
  private final QualifiedName name;

  private final boolean extensible;
  private final TypeToken<T> type;
  private final Model<? super T> model;

  private final boolean ordered;
  private final BindingCondition<T> condition;
  private final BindingFunction input;
  private final BindingFunction output;

  @SuppressWarnings("unchecked")
  protected ChildBindingPointImpl(ChildBindingPointBuilderImpl<?> configurator) {
    name = configurator
        .overrideChildren(ChildBindingPoint::name, ChildBindingPointBuilder::getName)
        .validateOverride(Objects::equals)
        .or(() -> configurator.getModel().get().name())
        .get();

    extensible = configurator
        .overrideChildren(ChildBindingPoint::extensible, ChildBindingPointBuilder::getExtensible)
        .validateOverride((a, b) -> true)
        .orDefault(false)
        .get();

    ordered = configurator
        .overrideChildren(ChildBindingPoint::ordered, ChildBindingPointBuilder::getOrdered)
        .validateOverride((a, b) -> a || !b)
        .or(false)
        .get();

    type = null; // TODO

    model = configurator
        .overrideChildren(ChildBindingPoint::model, ChildBindingPointBuilder::getModel)
        .validateOverride((a, b) -> a.equals(b) || a.baseModels().anyMatch(b::equals))
        .tryGet()
        .map(m -> (Model<? super T>) m)
        .orElse(null);

    BindingConditionPrototype conditionPrototype = configurator
        .overrideChildren(
            b -> b.bindingCondition().getPrototype(),
            ChildBindingPointBuilder::getBindingCondition)
        .orMerged(c -> BindingConditionPrototype.allOf(c))
        .or(v -> v.required())
        .get();
    condition = new BindingConditionFactory<>(type, configurator.getExpressionCompiler())
        .create(conditionPrototype);

    // TODO deal with hasInput
    Expression inputExpression = configurator
        .overrideChildren(
            b -> b.inputExpression().getExpression(),
            ChildBindingPointBuilder::getInput)
        .validateOverride((a, b) -> false)
        .get();
    input = new InputFunction(inputExpression, configurator.getExpressionCompiler());

    // TODO deal with hasOutput
    Expression outputExpression = configurator
        .overrideChildren(
            b -> b.outputExpression().getExpression(),
            ChildBindingPointBuilder::getOutput)
        .validateOverride((a, b) -> false)
        .get();
    output = new OutputFunction(outputExpression, configurator.getExpressionCompiler());
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
  public TypeToken<T> dataType() {
    return type;
  }

  @Override
  public Model<? super T> model() {
    return model;
  }

  @Override
  public Node override() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node parent() {
    // TODO Auto-generated method stub
    return null;
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

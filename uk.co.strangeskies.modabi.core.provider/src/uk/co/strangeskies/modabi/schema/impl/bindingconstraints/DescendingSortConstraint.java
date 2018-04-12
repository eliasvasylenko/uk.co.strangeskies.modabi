package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static uk.co.strangeskies.modabi.expression.Expressions.named;

import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class DescendingSortConstraint<T extends Comparable<? super T>> extends SortCondition<T> {
  public DescendingSortConstraint(FunctionalExpressionCompiler compiler) {
    super(named("b").invoke("compareTo", named("a")), new TypeToken<T>() {}, compiler);
  }
}

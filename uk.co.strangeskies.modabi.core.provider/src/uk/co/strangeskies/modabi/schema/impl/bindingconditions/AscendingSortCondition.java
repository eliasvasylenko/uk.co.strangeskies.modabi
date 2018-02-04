package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static uk.co.strangeskies.modabi.expression.Expressions.named;

import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class AscendingSortCondition<T extends Comparable<? super T>> extends SortCondition<T> {
  public AscendingSortCondition(FunctionalExpressionCompiler compiler) {
    super(named("a").invoke("compareTo", named("b")), new TypeToken<T>() {}, compiler);
  }
}

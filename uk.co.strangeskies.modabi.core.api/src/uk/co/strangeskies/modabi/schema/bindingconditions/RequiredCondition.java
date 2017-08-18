package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A simple rule for binding points which are required to always be processed.
 * 
 * @author Elias N Vasylenko
 */
public class RequiredCondition<T> extends OccurrencesCondition<T> {
  static final RequiredCondition<?> INSTANCE = new RequiredCondition<>();

  @SuppressWarnings("unchecked")
  public static <T> BindingCondition<T> required() {
    return (BindingCondition<T>) RequiredCondition.INSTANCE;
  }

  protected RequiredCondition() {
    super(Interval.bounded(1, 1));
  }
}

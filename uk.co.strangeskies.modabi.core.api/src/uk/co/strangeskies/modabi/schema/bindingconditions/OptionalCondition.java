package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.modabi.schema.BindingCondition;

public class OptionalCondition<T> extends OccurrencesCondition<T> {
  static final OptionalCondition<?> INSTANCE = new OptionalCondition<>();

  @SuppressWarnings("unchecked")
  public static <T> BindingCondition<T> optional() {
    return (BindingCondition<T>) OptionalCondition.INSTANCE;
  }

  protected OptionalCondition() {
    super(Interval.bounded(0, 1));
  }
}

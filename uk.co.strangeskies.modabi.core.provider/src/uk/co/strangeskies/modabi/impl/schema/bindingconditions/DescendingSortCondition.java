package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class DescendingSortCondition<T extends Comparable<? super T>> extends SortCondition<T> {
  public DescendingSortCondition(BindingConditionPrototype prototype) {
    super(prototype, (a, b) -> b.compareTo(a));
  }
}

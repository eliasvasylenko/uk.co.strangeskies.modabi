package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import java.util.function.Predicate;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class ValidationCondition<T> extends BindingConditionImpl<T> {
  private final String expressionString;
  private final Predicate<T> test;

  public ValidationCondition(
      BindingConditionPrototype prototype,
      String expressionString,
      Predicate<T> test) {
    super(prototype);
    this.expressionString = expressionString;
    this.test = test;
  }

  @Override
  public BindingConditionEvaluation<T> forState(ProcessingContext state) {
    return new BindingConditionEvaluation<T>() {
      public void failProcess() {
        throw new ProcessingException(
            MESSAGES
                .validationFailed((ChildBindingPoint<?>) state.getBindingPoint(), expressionString),
            state);
      }

      @Override
      public void beginProcessingNext() {}

      @Override
      public void completeProcessingNext(T binding) {
        if (!test.test(binding)) {
          failProcess();
        }
      }

      @Override
      public void endProcessing() {}
    };
  }
}

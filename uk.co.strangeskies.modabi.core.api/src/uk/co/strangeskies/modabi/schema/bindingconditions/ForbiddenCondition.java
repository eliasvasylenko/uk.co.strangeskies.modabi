package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.modabi.processing.ProcessingException.MESSAGES;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;

/**
 * A simple rule for binding points which are required to never be processed.
 * 
 * @author Elias N Vasylenko
 */
public class ForbiddenCondition<T> implements BindingCondition<T> {
  static final ForbiddenCondition<?> INSTANCE = new ForbiddenCondition<>();

  @SuppressWarnings("unchecked")
  public static <T> BindingCondition<T> forbidden() {
    return (BindingCondition<T>) ForbiddenCondition.INSTANCE;
  }

  protected ForbiddenCondition() {}

  @Override
  public BindingConditionEvaluation<T> forState(ProcessingContext state) {
    return new BindingConditionEvaluation<T>() {
      private boolean processed = false;

      @Override
      public void beginProcessingNext() {
        processed = true;
      }

      @Override
      public void completeProcessingNext(T binding) {}

      @Override
      public void endProcessing() {
        if (processed) {
          throw new ProcessingException(MESSAGES.mustNotHaveData(state.getBindingPoint()), state);
        }
      }
    };
  }
}

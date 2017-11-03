package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;

/**
 * A simple rule for binding points which are required to never be processed.
 * 
 * @author Elias N Vasylenko
 */
public class ForbiddenCondition<T> extends BindingConditionImpl<T> {
  public ForbiddenCondition(BindingConditionPrototype prototype) {
    super(prototype);
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
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
          throw new BindingException(MESSAGES.mustNotHaveData(state.getBindingPoint()), state);
        }
      }
    };
  }
}

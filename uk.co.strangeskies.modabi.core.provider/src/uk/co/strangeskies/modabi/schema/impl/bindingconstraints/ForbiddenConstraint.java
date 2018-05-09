package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static uk.co.strangeskies.modabi.binding.BindingException.MESSAGES;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingConstraintVisitor;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;

/**
 * A simple rule for binding points which are required to never be processed.
 * 
 * @author Elias N Vasylenko
 */
public class ForbiddenConstraint<T> implements BindingProcedure<T> {
  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    return new BindingProcess<T>() {
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

  @Override
  public BindingConstraint getConstraint() {
    return BindingConstraintVisitor::forbidden;
  }
}

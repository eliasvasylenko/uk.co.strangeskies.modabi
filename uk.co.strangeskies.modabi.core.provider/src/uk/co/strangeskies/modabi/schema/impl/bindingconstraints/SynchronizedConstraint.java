package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import uk.co.strangeskies.modabi.schema.BindingConstraintVisitor;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;

/**
 * A binding condition which only allows processing of each item to proceed once
 * the previous item has completed.
 * 
 * @author Elias N Vasylenko
 */
public class SynchronizedConstraint<T> implements BindingProcedure<T> {
  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    return new BindingProcess<T>() {
      private boolean locked = false;

      @Override
      public synchronized void beginProcessingNext() {
        while (locked) {
          try {
            wait();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
        locked = true;
      }

      @Override
      public synchronized void completeProcessingNext(T binding) {
        locked = false;
        notifyAll();
      }

      @Override
      public void endProcessing() {}
    };
  }

  @Override
  public BindingConstraint getConstraint() {
    return BindingConstraintVisitor::synchronous;
  }
}

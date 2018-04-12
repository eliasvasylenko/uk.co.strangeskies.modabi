package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import uk.co.strangeskies.modabi.schema.BindingConstraintVisitor;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcedure;

/**
 * A binding condition which only allows processing of each item to proceed once
 * the previous item has completed.
 * 
 * @author Elias N Vasylenko
 */
public class SynchronizedConstraint<T> implements BindingConstraint<T> {
  @Override
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    return new BindingProcedure<T>() {
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
  public BindingConstraintSpecification getSpecification() {
    return BindingConstraintVisitor::synchronous;
  }
}

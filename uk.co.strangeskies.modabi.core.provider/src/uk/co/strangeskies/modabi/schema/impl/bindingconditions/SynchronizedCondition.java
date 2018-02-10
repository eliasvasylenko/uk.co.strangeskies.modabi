package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingConditionVisitor;
import uk.co.strangeskies.modabi.schema.BindingContext;

/**
 * A binding condition which only allows processing of each item to proceed once
 * the previous item has completed.
 * 
 * @author Elias N Vasylenko
 */
public class SynchronizedCondition<T> implements BindingCondition<T> {
  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    return new BindingConditionEvaluation<T>() {
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
  public BindingConditionPrototype getPrototype() {
    return BindingConditionVisitor::synchronous;
  }
}

package uk.co.strangeskies.modabi.schema;

/**
 * A binding context evaluation represents the current state of the
 * {@link BindingProcedure binding condition} specified for a particular
 * {@link BindingPoint binding point} within a running {@link BindingContext
 * binding process}. An instance of this class should be generated via the
 * {@link BindingProcedure#procedeWithState(BindingContext)} method.
 * 
 * <p>
 * If any of the methods of this class report that they were unsuccessful, the
 * binding process is cancelled in failure for the owning binding point.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 */
public interface BindingProcess<T> {
  /**
   * This method is called once for each data item to be processed. Each
   * invocation will ultimately be followed by an invocation of
   * {@link #completeProcessingNext(Object)}, unless the binding process itself
   * fails.
   * 
   * <p>
   * Invocations will synchronized automatically such that they are only made
   * after the previous has completed. They may occur before the invocation of
   * {@link #completeProcessingNext(Object)} has been made for the previous item,
   * so as to allow for concurrent processing by default.
   * 
   * <p>
   * This method may block to wait for resources. Unless timely release of the
   * block can be guaranteed by other means, this should usually be done via the
   * {@link BlockingExecutor} of the {@link BindingContext running process} to
   * make sure deadlocks and unsatisfied dependencies can be properly detected and
   * reported.
   * 
   * @throws BindingException
   *           if the next item was unable to begin processing successfully
   */
  void beginProcessingNext();

  /**
   * Each invocation of {@link #completeProcessingNext(Object)} is preceded by an
   * invocation of {@link #beginProcessingNext()}.
   * 
   * <p>
   * This method will always be called as early as possible, with the stipulation
   * that processing order is maintained for a binding point which is
   * {@link Child#ordered() ordered},
   * 
   * @param binding
   *          the item that was bound
   * @throws BindingException
   *           if the next item was unable to complete processing successfully
   */
  void completeProcessingNext(T binding);

  /**
   * Attempt to end the processing of the owning {@link BindingPoint}.
   * 
   * <p>
   * This method will only be invoked once all available items have been
   * processed, and {@link #completeProcessingNext(Object)} has been invoked with
   * the bound object associated with each invocation of
   * {@link #beginProcessingNext()}.
   * 
   * @throws BindingException
   *           if the process was unable to complete successfully
   */
  void endProcessing();
}

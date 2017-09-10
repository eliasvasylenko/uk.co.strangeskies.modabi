package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;

public class AnyOfCondition<T> extends BindingConditionImpl<T> {
  private final List<BindingCondition<? super T>> conditions;

  public AnyOfCondition(
      BindingConditionPrototype prototype,
      Collection<? extends BindingCondition<? super T>> conditions) {
    super(prototype);
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingCondition<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingConditionEvaluation<T> forState(ProcessingContext state) {
    return new BindingConditionEvaluation<T>() {
      private List<BindingConditionEvaluation<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.forState(state))
          .collect(Collectors.toCollection(ArrayList::new));

      private Set<Exception> swallowed = new LinkedHashSet<>();

      @Override
      public void beginProcessingNext() {
        tryMultiple(BindingConditionEvaluation::beginProcessingNext);
      }

      @Override
      public void completeProcessingNext(T binding) {
        tryMultiple(e -> e.completeProcessingNext(binding));
      }

      @Override
      public void endProcessing() {
        tryMultiple(BindingConditionEvaluation::endProcessing);
      }

      private void tryMultiple(Consumer<BindingConditionEvaluation<? super T>> process) {
        for (Iterator<BindingConditionEvaluation<? super T>> evaluationIterator = conditionEvaluations
            .iterator(); evaluationIterator.hasNext();) {
          BindingConditionEvaluation<? super T> evaluation = evaluationIterator.next();

          try {
            process.accept(evaluation);
          } catch (Exception e) {
            evaluationIterator.remove();
            swallowed.add(e);
          }
        }

        if (!conditionEvaluations.isEmpty()) {
          throw ProcessingException.mergeExceptions(state, swallowed);
        }
      }
    };
  }
}

package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;

public class AnyOfCondition<T> implements BindingCondition<T> {
  private final List<BindingCondition<? super T>> conditions;

  public AnyOfCondition(Collection<? extends BindingCondition<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingCondition<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
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
          throw BindingException.mergeExceptions(state, swallowed);
        }
      }
    };
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    return v -> v.anyOf(conditions.stream().map(c -> c.getPrototype()).collect(toList()));
  }
}

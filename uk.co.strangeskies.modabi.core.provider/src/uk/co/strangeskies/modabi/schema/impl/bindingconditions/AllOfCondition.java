package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingContext;

public class AllOfCondition<T> implements BindingCondition<T> {
  private final List<BindingCondition<? super T>> conditions;

  public AllOfCondition(Collection<? extends BindingCondition<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingCondition<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    return new BindingConditionEvaluation<T>() {
      private final List<BindingConditionEvaluation<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.forState(state))
          .collect(toList());

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
        Set<Exception> exceptions = new HashSet<>();

        for (BindingConditionEvaluation<? super T> evaluation : conditionEvaluations) {
          try {
            process.accept(evaluation);
          } catch (Exception e) {
            exceptions.add(e);
          }
        }

        if (!exceptions.isEmpty()) {
          throw BindingException.mergeExceptions(state, exceptions);
        }
      }
    };
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    return v -> v.allOf(conditions.stream().map(c -> c.getPrototype()).collect(toList()));
  }
}

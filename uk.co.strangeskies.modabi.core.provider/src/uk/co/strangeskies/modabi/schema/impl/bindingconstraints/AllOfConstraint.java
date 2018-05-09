package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;

public class AllOfConstraint<T> implements BindingProcedure<T> {
  private final List<BindingProcedure<? super T>> conditions;

  public AllOfConstraint(Collection<? extends BindingProcedure<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingProcedure<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    return new BindingProcess<T>() {
      private final List<BindingProcess<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.procedeWithState(state))
          .collect(toList());

      @Override
      public void beginProcessingNext() {
        tryMultiple(BindingProcess::beginProcessingNext);
      }

      @Override
      public void completeProcessingNext(T binding) {
        tryMultiple(e -> e.completeProcessingNext(binding));
      }

      @Override
      public void endProcessing() {
        tryMultiple(BindingProcess::endProcessing);
      }

      private void tryMultiple(Consumer<BindingProcess<? super T>> process) {
        Set<Exception> exceptions = new HashSet<>();

        for (BindingProcess<? super T> evaluation : conditionEvaluations) {
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
  public BindingConstraint getConstraint() {
    return v -> v.allOf(conditions.stream().map(c -> c.getConstraint()).collect(toList()));
  }
}

package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcedure;

public class AllOfConstraint<T> implements BindingConstraint<T> {
  private final List<BindingConstraint<? super T>> conditions;

  public AllOfConstraint(Collection<? extends BindingConstraint<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingConstraint<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    return new BindingProcedure<T>() {
      private final List<BindingProcedure<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.procedeWithState(state))
          .collect(toList());

      @Override
      public void beginProcessingNext() {
        tryMultiple(BindingProcedure::beginProcessingNext);
      }

      @Override
      public void completeProcessingNext(T binding) {
        tryMultiple(e -> e.completeProcessingNext(binding));
      }

      @Override
      public void endProcessing() {
        tryMultiple(BindingProcedure::endProcessing);
      }

      private void tryMultiple(Consumer<BindingProcedure<? super T>> process) {
        Set<Exception> exceptions = new HashSet<>();

        for (BindingProcedure<? super T> evaluation : conditionEvaluations) {
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
  public BindingConstraintSpecification getSpecification() {
    return v -> v.allOf(conditions.stream().map(c -> c.getSpecification()).collect(toList()));
  }
}

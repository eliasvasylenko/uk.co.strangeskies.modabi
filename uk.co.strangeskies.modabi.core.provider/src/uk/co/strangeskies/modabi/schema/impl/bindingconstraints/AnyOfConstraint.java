package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.binding.BindingException;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcedure;

public class AnyOfConstraint<T> implements BindingConstraint<T> {
  private final List<BindingConstraint<? super T>> conditions;

  public AnyOfConstraint(Collection<? extends BindingConstraint<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingConstraint<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    return new BindingProcedure<T>() {
      private List<BindingProcedure<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.procedeWithState(state))
          .collect(Collectors.toCollection(ArrayList::new));

      private Set<Exception> swallowed = new LinkedHashSet<>();

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
        for (Iterator<BindingProcedure<? super T>> evaluationIterator = conditionEvaluations
            .iterator(); evaluationIterator.hasNext();) {
          BindingProcedure<? super T> evaluation = evaluationIterator.next();

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
  public BindingConstraintSpecification getSpecification() {
    return v -> v.anyOf(conditions.stream().map(c -> c.getSpecification()).collect(toList()));
  }
}

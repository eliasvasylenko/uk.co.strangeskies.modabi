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
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;

public class AnyOfConstraint<T> implements BindingProcedure<T> {
  private final List<BindingProcedure<? super T>> conditions;

  public AnyOfConstraint(Collection<? extends BindingProcedure<? super T>> conditions) {
    this.conditions = new ArrayList<>(conditions);
  }

  public List<BindingProcedure<? super T>> getConditions() {
    return conditions;
  }

  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    return new BindingProcess<T>() {
      private List<BindingProcess<? super T>> conditionEvaluations = conditions
          .stream()
          .map(c -> c.procedeWithState(state))
          .collect(Collectors.toCollection(ArrayList::new));

      private Set<Exception> swallowed = new LinkedHashSet<>();

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
        for (Iterator<BindingProcess<? super T>> evaluationIterator = conditionEvaluations
            .iterator(); evaluationIterator.hasNext();) {
          BindingProcess<? super T> evaluation = evaluationIterator.next();

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
  public BindingConstraint getConstraint() {
    return v -> v.anyOf(conditions.stream().map(c -> c.getConstraint()).collect(toList()));
  }
}

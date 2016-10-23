package uk.co.strangeskies.modabi.schema.bindingconditions;

import java.util.ArrayList;
import java.util.Arrays;
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

public class Or<T> implements BindingCondition<T> {
	private final List<BindingCondition<? super T>> conditions;

	public static <T> BindingCondition<T> or(Collection<? extends BindingCondition<? super T>> conditions) {
		return new Or<>(conditions);
	}

	@SafeVarargs
	public static <T> BindingCondition<T> or(BindingCondition<? super T>... conditions) {
		return or(Arrays.asList(conditions));
	}

	protected Or(Collection<? extends BindingCondition<? super T>> conditions) {
		this.conditions = new ArrayList<>(conditions);
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

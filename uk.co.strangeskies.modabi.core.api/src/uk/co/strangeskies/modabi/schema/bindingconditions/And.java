package uk.co.strangeskies.modabi.schema.bindingconditions;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;

public class And<T> implements BindingCondition<T> {
	private final List<BindingCondition<? super T>> conditions;

	public static <T> BindingCondition<T> and(Collection<? extends BindingCondition<? super T>> conditions) {
		return new And<>(conditions);
	}

	@SafeVarargs
	public static <T> BindingCondition<T> and(BindingCondition<? super T>... conditions) {
		return and(Arrays.asList(conditions));
	}

	protected And(Collection<? extends BindingCondition<? super T>> conditions) {
		this.conditions = new ArrayList<>(conditions);
	}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
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
					throw ProcessingException.mergeExceptions(state, exceptions);
				}
			}
		};
	}
}

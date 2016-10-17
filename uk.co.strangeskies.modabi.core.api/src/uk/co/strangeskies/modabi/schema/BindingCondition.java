package uk.co.strangeskies.modabi.schema;

import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;

/**
 * A {@link BindingCondition binding condition} is associated with a
 * {@link ChildBindingPoint binding point}, and specifies rules for determining
 * whether it may be bound or skipped during some processing operation.
 * <p>
 * Upon reaching the associated binding point during some process, it is
 * evaluated for the current {@link ProcessingContext processing state}. Binding
 * conditions may be re-evaluated multiple times.
 * 
 * 
 * 
 * 
 * 
 * TODO EqualTo, GreaterThan, LessThan, GreaterThanOrEqualTo, LessThanOrEqualTo
 * 
 * maybe those contained in a ForEach type class which takes a predicate
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Elias N Vasylenko
 */
public interface BindingCondition<T> {
	BindingConditionEvaluation<T> forState(ProcessingContext state);

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> required() {
		return (BindingCondition<T>) Required.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> forbidden() {
		return (BindingCondition<T>) Forbidden.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> optional() {
		return (BindingCondition<T>) Optional.INSTANCE;
	}

	public static <T> BindingCondition<T> occurrences(Range<Integer> range) {
		return new Occurrences<>(range);
	}

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> ascending() {
		return (BindingCondition<T>) Ascending.INSTANCE;
	}

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> descending() {
		return (BindingCondition<T>) Descending.INSTANCE;
	}

	public static <T> BindingCondition<T> ordered(Comparator<T> order) {
		return new Ordered<>(order);
	}

	public static <T> BindingCondition<T> and(Collection<? extends BindingCondition<? super T>> conditions) {
		return new And<>(conditions);
	}

	@SafeVarargs
	public static <T> BindingCondition<T> and(BindingCondition<? super T>... conditions) {
		return and(Arrays.asList(conditions));
	}

	public static <T> BindingCondition<T> or(Collection<? extends BindingCondition<? super T>> conditions) {
		return new Or<>(conditions);
	}

	@SafeVarargs
	public static <T> BindingCondition<T> or(BindingCondition<? super T>... conditions) {
		return or(Arrays.asList(conditions));
	}

	/**
	 * A simple rule for binding points which are required to always be processed.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Required<T> implements BindingCondition<T> {
		private static final Required<?> INSTANCE = new Required<>();

		private Required() {}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private boolean processed = false;

				@Override
				public void processNext(T binding) {
					processed = true;
				}

				@Override
				public boolean canContinueProcess() {
					return true;
				}

				@Override
				public boolean canEndProcess() {
					return processed;
				}

				@Override
				public ProcessingException failProcess() {
					return new ProcessingException(p -> p.mustHaveData(state.getNode().name()), state);
				}
			};
		}
	}

	/**
	 * A simple rule for binding points which are required to never be processed.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Forbidden<T> implements BindingCondition<T> {
		private static final Forbidden<?> INSTANCE = new Forbidden<>();

		private Forbidden() {}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private boolean processed = false;

				@Override
				public void processNext(T binding) {
					processed = true;
				}

				@Override
				public boolean canContinueProcess() {
					return false;
				}

				@Override
				public boolean canEndProcess() {
					return !processed;
				}

				@Override
				public ProcessingException failProcess() {
					return new ProcessingException(p -> p.mustNotHaveData(state.getNode().name()), state);
				}
			};
		}
	}

	class Optional<T> implements BindingCondition<T> {
		private static final Optional<?> INSTANCE = new Optional<>();

		private Optional() {}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				@Override
				public void processNext(T binding) {}

				@Override
				public boolean canContinueProcess() {
					return true;
				}

				@Override
				public boolean canEndProcess() {
					return true;
				}

				@Override
				public ProcessingException failProcess() {
					throw new AssertionError();
				}
			};
		}
	}

	/**
	 * A rule to specify that a binding point must be processed a number of times
	 * within a given range.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Occurrences<T> implements BindingCondition<T> {
		private final Range<Integer> range;

		private Occurrences(Range<Integer> range) {
			this.range = range;
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private int count = 0;

				@Override
				public void processNext(T binding) {
					count++;
				}

				@Override
				public boolean canContinueProcess() {
					return !range.isValueAbove(count + 1);
				}

				@Override
				public boolean canEndProcess() {
					return range.contains(count);
				}

				@Override
				public ProcessingException failProcess() {
					return new ProcessingException(p -> p.mustHaveDataWithinRange((ChildBindingPoint<?>) state.getNode(), range),
							state);
				}
			};
		}
	}

	/**
	 * A rule to specify that a binding point must be processed a number of times
	 * within a given range.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Ascending<T extends Comparable<? super T>> extends Ordered<T> {
		private static final Ascending<?> INSTANCE = new Ascending<>();

		private Ascending() {
			super(Comparable::compareTo);
		}
	}

	/**
	 * A rule to specify that a binding point must be processed a number of times
	 * within a given range.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Descending<T extends Comparable<? super T>> extends Ordered<T> {
		private static final Descending<?> INSTANCE = new Descending<>();

		private Descending() {
			super((a, b) -> b.compareTo(a));
		}
	}

	/**
	 * A rule to specify that a binding point must be processed a number of times
	 * within a given range.
	 * 
	 * @author Elias N Vasylenko
	 */
	class Ordered<T> implements BindingCondition<T> {
		private final Comparator<? super T> comparator;

		private Ordered(Comparator<? super T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private final List<T> bindings = new ArrayList<>();
				private boolean ordered = true;

				@Override
				public void processNext(T binding) {
					if (ordered && !bindings.isEmpty() && comparator.compare(bindings.get(bindings.size() - 1), binding) > 0) {
						ordered = false;
					}

					bindings.add(binding);
				}

				@Override
				public boolean canContinueProcess() {
					return ordered;
				}

				@Override
				public boolean canEndProcess() {
					return ordered;
				}

				@SuppressWarnings("unchecked")
				@Override
				public ProcessingException failProcess() {
					return new ProcessingException(p -> p.mustBeOrdered((ChildBindingPoint<T>) state.getNode(), bindings,
							(Class<? extends Comparator<?>>) comparator.getClass()), state);
				}
			};
		}
	}

	class And<T> implements BindingCondition<T> {
		private final List<BindingCondition<? super T>> conditions;

		private And(Collection<? extends BindingCondition<? super T>> conditions) {
			this.conditions = new ArrayList<>(conditions);
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private final List<BindingConditionEvaluation<? super T>> conditionEvaluations = conditions.stream()
						.map(c -> c.forState(state)).collect(toList());

				@Override
				public void processNext(T binding) {
					conditionEvaluations.forEach(c -> c.processNext(binding));
				}

				@Override
				public boolean canContinueProcess() {
					return conditionEvaluations.stream().map(c -> c.canContinueProcess()).allMatch(TRUE::equals);
				}

				@Override
				public boolean canEndProcess() {
					return conditionEvaluations.stream().map(c -> c.canEndProcess()).allMatch(TRUE::equals);
				}

				@Override
				public ProcessingException failProcess() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
	}

	class Or<T> implements BindingCondition<T> {
		private final List<BindingCondition<? super T>> conditions;

		private Or(Collection<? extends BindingCondition<? super T>> conditions) {
			this.conditions = new ArrayList<>(conditions);
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			return new BindingConditionEvaluation<T>() {
				private final List<BindingConditionEvaluation<? super T>> conditionEvaluations = conditions.stream()
						.map(c -> c.forState(state)).collect(toList());

				@Override
				public void processNext(T binding) {
					conditionEvaluations.forEach(c -> c.processNext(binding));
				}

				@Override
				public boolean canContinueProcess() {
					return conditionEvaluations.stream().map(c -> c.canContinueProcess()).anyMatch(TRUE::equals);
				}

				@Override
				public boolean canEndProcess() {
					return conditionEvaluations.stream().map(c -> c.canEndProcess()).anyMatch(TRUE::equals);
				}

				@Override
				public ProcessingException failProcess() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
	}

	class IsBound<T> implements BindingCondition<T> {
		private final ChildBindingPoint<?> target;

		private IsBound(ChildBindingPoint<?> target) {
			this.target = target;
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			throw new UnsupportedOperationException(); // TODO
		}
	}

	class IsNotBound<T> implements BindingCondition<T> {
		private final ChildBindingPoint<?> target;

		private IsNotBound(ChildBindingPoint<?> target) {
			this.target = target;
		}

		@Override
		public BindingConditionEvaluation<T> forState(ProcessingContext state) {
			throw new UnsupportedOperationException(); // TODO
		}
	}
}

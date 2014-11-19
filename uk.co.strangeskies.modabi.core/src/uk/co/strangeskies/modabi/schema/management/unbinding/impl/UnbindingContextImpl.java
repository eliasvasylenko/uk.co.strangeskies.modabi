package uk.co.strangeskies.modabi.schema.management.unbinding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.impl.ProcessingContextImpl;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.utilities.factory.Factory;

public class UnbindingContextImpl extends ProcessingContextImpl implements
		UnbindingContext {
	private interface UnbindingProvisions {
		<U> U provide(Class<U> clazz, UnbindingContextImpl headContext);

		boolean isProvided(Class<?> clazz);
	}

	private final List<Object> unbindingSourceStack;
	private final StructuredDataTarget output;
	private final UnbindingProvisions provider;

	public UnbindingContextImpl(SchemaManager manager) {
		super(manager);

		unbindingSourceStack = Collections.emptyList();
		output = null;
		provider = new UnbindingProvisions() {
			@Override
			public <U> U provide(Class<U> clazz, UnbindingContextImpl headContext) {
				return manager.provisions().provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return manager.provisions().isProvided(clazz);
			}
		};
	}

	private UnbindingContextImpl(UnbindingContextImpl parent,
			List<Object> unbindingSourceStack, StructuredDataTarget output,
			UnbindingProvisions provider) {
		super(parent);
		this.unbindingSourceStack = unbindingSourceStack;
		this.output = output;
		this.provider = provider;
	}

	private UnbindingContextImpl(UnbindingContextImpl parent,
			SchemaNode.Effective<?, ?> unbindingNode) {
		super(parent, unbindingNode);
		this.unbindingSourceStack = parent.unbindingSourceStack;
		this.output = parent.output;
		this.provider = parent.provider;
	}

	@Override
	public List<Object> unbindingSourceStack() {
		return unbindingSourceStack;
	}

	@Override
	public StructuredDataTarget output() {
		return output;
	}

	private <U> U provide(Class<U> clazz, UnbindingContextImpl headContext) {
		return provider.provide(clazz, headContext);
	}

	@Override
	public Provisions provisions() {
		return new Provisions() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return UnbindingContextImpl.this.provide(clazz,
						UnbindingContextImpl.this);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return provider.isProvided(clazz);
			}
		};
	}

	public <T> UnbindingContextImpl withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	public <T> UnbindingContextImpl withProvision(Class<T> providedClass,
			Function<UnbindingContextImpl, T> provider) {
		UnbindingContextImpl base = this;

		return new UnbindingContextImpl(this, unbindingSourceStack, output,
				new UnbindingProvisions() {
					@SuppressWarnings("unchecked")
					@Override
					public <U> U provide(Class<U> clazz, UnbindingContextImpl headContext) {
						if (clazz.equals(providedClass))
							return (U) provider.apply(headContext);

						return base.provide(clazz, headContext);
					}

					@Override
					public boolean isProvided(Class<?> clazz) {
						return clazz.equals(providedClass)
								|| base.provisions().isProvided(clazz);
					}
				});
	}

	public <T> UnbindingContextImpl withUnbindingSource(Object target) {
		List<Object> unbindingSourceStack = new ArrayList<>(unbindingSourceStack());
		unbindingSourceStack.add(target);

		return new UnbindingContextImpl(this,
				Collections.unmodifiableList(unbindingSourceStack), output, provider);
	}

	public <T> UnbindingContextImpl withUnbindingNode(
			SchemaNode.Effective<?, ?> node) {
		return new UnbindingContextImpl(this, node);
	}

	public UnbindingContextImpl withOutput(StructuredDataTarget output) {
		return new UnbindingContextImpl(this, unbindingSourceStack, output,
				provider);
	}

	public void attemptUnbinding(Consumer<UnbindingContextImpl> unbindingMethod) {
		UnbindingContextImpl context = this;

		BufferingDataTarget dataTarget = null;
		BufferingStructuredDataTarget output = new BufferingStructuredDataTarget();

		/*
		 * Mark output! (by redirecting to a new buffer)
		 */
		if (context.provisions().isProvided(DataTarget.class)) {
			dataTarget = new BufferingDataTarget();
			DataTarget finalTarget = dataTarget;
			context = context.withProvision(DataTarget.class, () -> finalTarget);
		}
		context = context.withOutput(output);

		/*
		 * Make unbinding attempt! (Reset output to mark on failutre by discarding
		 * buffer, via exception.)
		 */
		unbindingMethod.accept(context);

		/*
		 * Remove mark! (by flushing buffer into output)
		 */
		if (dataTarget != null)
			dataTarget.buffer().pipe(provisions().provide(DataTarget.class));

		output.buffer().pipeNextChild(output());
	}

	public <I> I attemptUnbindingUntilSuccessful(Iterable<I> attemptItems,
			BiConsumer<UnbindingContextImpl, I> unbindingMethod,
			Function<Set<Exception>, UnbindingException> onFailure) {
		if (!attemptItems.iterator().hasNext())
			throw new IllegalArgumentException(
					"Must supply items for unbinding attempt.");

		Set<Exception> failures = new HashSet<>();

		for (I item : attemptItems)
			try {
				attemptUnbinding(c -> unbindingMethod.accept(c, item));

				return item;
			} catch (Exception e) {
				failures.add(e);
			}

		for (Exception failure : failures) {
			failure.printStackTrace();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		}

		throw onFailure.apply(failures);
	}
}

package uk.co.strangeskies.modabi.schema.processing.unbinding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.io.structured.BufferingStructuredDataTarget;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.Provisions;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.ProcessingContextImpl;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;
import uk.co.strangeskies.utilities.factory.Factory;

public class UnbindingContextImpl extends ProcessingContextImpl implements
		UnbindingContext {
	private interface UnbindingProvisions {
		<U> U provide(Class<U> clazz, UnbindingContextImpl headContext);

		boolean isProvided(Class<?> clazz);
	}

	private interface UnbindingSchemaAccess {
		<T> List<Model.Effective<T>> getMatchingModels(Class<T> dataClass);

		<T> List<Model.Effective<? extends T>> getMatchingModels(
				ComplexNode.Effective<T> element, Class<? extends T> dataClass);

		<T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
				DataNode.Effective<T> node, Class<?> dataClass);
	}

	private final List<Object> unbindingSourceStack;
	private final StructuredDataTarget output;
	private final Bindings bindings;
	private final UnbindingProvisions provider;
	private final UnbindingSchemaAccess schemaAccess;

	public UnbindingContextImpl(SchemaManager manager) {
		this(Collections.emptyList(), null, new Bindings(),
				new UnbindingProvisions() {
					@Override
					public <U> U provide(Class<U> clazz, UnbindingContextImpl headContext) {
						return manager.provisions().provide(clazz);
					}

					@Override
					public boolean isProvided(Class<?> clazz) {
						return manager.provisions().isProvided(clazz);
					}
				}, new UnbindingSchemaAccess() {
					private final Map<Class<?>, List<? extends Model.Effective<?>>> attemptedMatchingModels = new HashMap<>();
					private final Map<Class<?>, List<? extends DataBindingType.Effective<?>>> attemptedMatchingTypes = new HashMap<>();

					@Override
					public <U> List<Model.Effective<U>> getMatchingModels(
							Class<U> dataClass) {
						return manager.registeredModels().getMatchingModels(dataClass)
								.stream().map(n -> n.effective())
								.collect(Collectors.toCollection(ArrayList::new));
					}

					@Override
					public <T> List<Model.Effective<? extends T>> getMatchingModels(
							ComplexNode.Effective<T> element, Class<? extends T> dataClass) {
						@SuppressWarnings("unchecked")
						List<Model.Effective<? extends T>> cached = (List<Model.Effective<? extends T>>) attemptedMatchingModels
								.get(dataClass);

						if (cached == null) {
							cached = manager.registeredModels()
									.getMatchingModels(element, dataClass).stream()
									.map(n -> n.effective())
									.collect(Collectors.toCollection(ArrayList::new));
							attemptedMatchingModels.put(dataClass, cached);
						}

						return cached;
					}

					@Override
					public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
							DataNode.Effective<T> node, Class<?> dataClass) {
						@SuppressWarnings("unchecked")
						List<DataBindingType.Effective<? extends T>> cached = (List<DataBindingType.Effective<? extends T>>) attemptedMatchingTypes
								.get(dataClass);

						if (cached == null) {
							cached = manager.registeredTypes()
									.getMatchingTypes(node, dataClass).stream()
									.map(n -> n.effective())
									.collect(Collectors.toCollection(ArrayList::new));
							attemptedMatchingTypes.put(dataClass, cached);
						}

						return cached;
					}
				});
	}

	private UnbindingContextImpl(List<Object> unbindingSourceStack,
			StructuredDataTarget output, Bindings bindings,
			UnbindingProvisions provider, UnbindingSchemaAccess schemaAccess) {
		this.unbindingSourceStack = unbindingSourceStack;
		this.output = output;
		this.bindings = bindings;
		this.provider = provider;
		this.schemaAccess = schemaAccess;
	}

	private UnbindingContextImpl(UnbindingContextImpl parent,
			List<Object> unbindingSourceStack, StructuredDataTarget output,
			Bindings bindings, UnbindingProvisions provider,
			UnbindingSchemaAccess schemaAccess) {
		super(parent);
		this.unbindingSourceStack = unbindingSourceStack;
		this.output = output;
		this.bindings = bindings;
		this.provider = provider;
		this.schemaAccess = schemaAccess;
	}

	private UnbindingContextImpl(UnbindingContextImpl parent,
			SchemaNode.Effective<?, ?> unbindingNode) {
		super(parent, unbindingNode);
		this.unbindingSourceStack = parent.unbindingSourceStack;
		this.output = parent.output;
		this.bindings = parent.bindings;
		this.provider = parent.provider;
		this.schemaAccess = parent.schemaAccess;
	}

	@Override
	public List<Object> unbindingSourceStack() {
		return unbindingSourceStack;
	}

	@Override
	public StructuredDataTarget output() {
		return output;
	}

	@Override
	public Bindings bindings() {
		return bindings;
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

	@Override
	public <T> List<Model.Effective<T>> getMatchingModels(Class<T> dataClass) {
		return schemaAccess.getMatchingModels(dataClass);
	}

	@Override
	public <T> List<Model.Effective<? extends T>> getMatchingModels(
			ComplexNode.Effective<T> element, Class<? extends T> dataClass) {
		return schemaAccess.getMatchingModels(element, dataClass);
	}

	@Override
	public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass) {
		return schemaAccess.getMatchingTypes(node, dataClass);
	}

	public <T> UnbindingContextImpl withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	public <T> UnbindingContextImpl withProvision(Class<T> providedClass,
			Function<UnbindingContextImpl, T> provider) {
		UnbindingContextImpl base = this;

		return new UnbindingContextImpl(this, unbindingSourceStack, output,
				bindings, new UnbindingProvisions() {
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
				}, schemaAccess);
	}

	public <T> UnbindingContextImpl withUnbindingSource(Object target) {
		List<Object> unbindingSourceStack = new ArrayList<>(unbindingSourceStack());
		unbindingSourceStack.add(target);

		return new UnbindingContextImpl(this,
				Collections.unmodifiableList(unbindingSourceStack), output, bindings,
				provider, schemaAccess);
	}

	public <T> UnbindingContextImpl withUnbindingNode(
			SchemaNode.Effective<?, ?> node) {
		return new UnbindingContextImpl(this, node);
	}

	public UnbindingContextImpl withOutput(StructuredDataTarget output) {
		return new UnbindingContextImpl(this, unbindingSourceStack, output,
				bindings, provider, schemaAccess);
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

	public <I extends SchemaNode.Effective<?, ?>> I attemptUnbindingUntilSuccessful(
			List<I> attemptItems,
			BiConsumer<UnbindingContextImpl, I> unbindingMethod,
			Function<Set<Exception>, UnbindingException> onFailure) {
		if (attemptItems.isEmpty())
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

		throw onFailure.apply(failures);
	}
}

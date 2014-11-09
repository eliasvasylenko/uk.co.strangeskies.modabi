package uk.co.strangeskies.modabi.schema.processing.binding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.Provisions;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingException;
import uk.co.strangeskies.modabi.schema.processing.impl.ProcessingContextImpl;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingException;
import uk.co.strangeskies.utilities.factory.Factory;

public class BindingContextImpl extends ProcessingContextImpl implements
		BindingContext {
	private interface BindingProvisions {
		<U> U provide(Class<U> clazz, BindingContextImpl headContext);

		boolean isProvided(Class<?> clazz);
	}

	private interface BindingSchemaAccess {
		Model.Effective<?> getModel(QualifiedName nextElement);

		<T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
				DataNode.Effective<T> node, Class<?> dataClass);
	}

	private final List<Object> bindingTargetStack;
	private final StructuredDataSource input;
	private final BindingProvisions provider;
	private final BindingSchemaAccess schemaAccess;

	public BindingContextImpl(SchemaManager manager) {
		super();

		this.bindingTargetStack = Collections.emptyList();
		this.input = null;
		this.provider = new BindingProvisions() {
			@Override
			public <U> U provide(Class<U> clazz, BindingContextImpl headContext) {
				return manager.provisions().provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return manager.provisions().isProvided(clazz);
			}
		};
		this.schemaAccess = new BindingSchemaAccess() {
			private final Map<Class<?>, List<? extends DataBindingType.Effective<?>>> attemptedMatchingTypes = new HashMap<>();

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				Model<?> model = manager.registeredModels().get(nextElement);
				return model == null ? null : model.effective();
			}

			@Override
			public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
					DataNode.Effective<T> node, Class<?> dataClass) {
				@SuppressWarnings("unchecked")
				List<DataBindingType.Effective<? extends T>> cached = (List<DataBindingType.Effective<? extends T>>) attemptedMatchingTypes
						.get(dataClass);

				if (cached == null) {
					cached = manager.registeredTypes().getMatchingTypes(node, dataClass)
							.stream().map(n -> n.effective())
							.collect(Collectors.toCollection(ArrayList::new));
					attemptedMatchingTypes.put(dataClass, cached);
				}

				return cached;
			}
		};
	}

	private BindingContextImpl(BindingContextImpl parent,
			List<Object> bindingTargetStack, StructuredDataSource input,
			BindingProvisions provider) {
		super(parent);
		this.bindingTargetStack = bindingTargetStack;
		this.input = input;
		this.provider = provider;
		this.schemaAccess = parent.schemaAccess;
	}

	private BindingContextImpl(BindingContextImpl parent,
			SchemaNode.Effective<?, ?> node) {
		super(parent, node);
		this.bindingTargetStack = parent.bindingTargetStack;
		this.input = parent.input;
		this.provider = parent.provider;
		this.schemaAccess = parent.schemaAccess;
	}

	public <U> U provide(Class<U> clazz) {
		return provide(clazz, this);
	}

	public StructuredDataSource input() {
		return input;
	}

	private <U> U provide(Class<U> clazz, BindingContextImpl headContext) {
		return provider.provide(clazz, headContext);
	}

	@Override
	public Provisions provisions() {
		return new Provisions() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return BindingContextImpl.this.provide(clazz, BindingContextImpl.this);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return provider.isProvided(clazz);
			}
		};
	}

	@Override
	public List<Object> bindingTargetStack() {
		return bindingTargetStack;
	}

	@Override
	public Model.Effective<?> getModel(QualifiedName nextElement) {
		return schemaAccess.getModel(nextElement);
	}

	@Override
	public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
			DataNode.Effective<T> node, Class<?> dataClass) {
		return schemaAccess.getMatchingTypes(node, dataClass);
	}

	public <T> BindingContextImpl withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	public <T> BindingContextImpl withProvision(Class<T> providedClass,
			Function<BindingContextImpl, T> provider) {
		BindingContextImpl base = this;

		return new BindingContextImpl(this, bindingTargetStack, input,
				new BindingProvisions() {
					@SuppressWarnings("unchecked")
					@Override
					public <U> U provide(Class<U> clazz, BindingContextImpl headContext) {
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

	public <T> BindingContextImpl withBindingTarget(Object target) {
		List<Object> bindingTargetStack = new ArrayList<>(bindingTargetStack());
		bindingTargetStack.add(target);

		return new BindingContextImpl(this,
				Collections.unmodifiableList(bindingTargetStack), input, provider);
	}

	public <T> BindingContextImpl withBindingNode(SchemaNode.Effective<?, ?> node) {
		return new BindingContextImpl(this, node);
	}

	public BindingContextImpl withInput(StructuredDataSource input) {
		return new BindingContextImpl(this, bindingTargetStack, input, provider);
	}

	public void attempt(Consumer<BindingContextImpl> bindingMethod) {
		bindingMethod.accept(this);
	}

	public <U> U attempt(Function<BindingContextImpl, U> bindingMethod) {
		return bindingMethod.apply(this);
	}

	public <I extends SchemaNode.Effective<?, ?>> I attemptUntilSuccessful(
			List<I> attemptItems, BiConsumer<BindingContextImpl, I> bindingMethod,
			Function<Set<Exception>, UnbindingException> onFailure) {
		throw new BindingException("attemptUntilSuccessful unimplemented.", this);
	}
}

package uk.co.strangeskies.modabi.schema.management.binding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.schema.management.Provisions;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.management.impl.ProcessingContextImpl;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.utilities.factory.Factory;

public class BindingContextImpl extends ProcessingContextImpl implements
		BindingContext {
	private interface BindingProvisions {
		<U> U provide(Class<U> clazz, BindingContext headContext);

		boolean isProvided(Class<?> clazz);
	}

	private final List<Object> bindingTargetStack;
	private final StructuredDataSource input;
	private final BindingProvisions provider;

	public BindingContextImpl(SchemaManager manager) {
		super(manager);

		this.bindingTargetStack = Collections.emptyList();
		this.input = null;
		this.provider = new BindingProvisions() {
			@Override
			public <U> U provide(Class<U> clazz, BindingContext headContext) {
				return manager.provisions().provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return manager.provisions().isProvided(clazz);
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
	}

	private BindingContextImpl(BindingContextImpl parent,
			SchemaNode.Effective<?, ?> node) {
		super(parent, node);
		this.bindingTargetStack = parent.bindingTargetStack;
		this.input = parent.input;
		this.provider = parent.provider;
	}

	public <U> U provide(Class<U> clazz) {
		return provide(clazz, this);
	}

	public StructuredDataSource input() {
		return input;
	}

	private <U> U provide(Class<U> clazz, BindingContext headContext) {
		return provider.provide(clazz, headContext);
	}

	@Override
	public Provisions provisions() {
		return new Provisions() {
			@Override
			public <U> U provide(Class<U> clazz) {
				return provider.provide(clazz, BindingContextImpl.this);
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

	public <T> BindingContextImpl withProvision(Class<T> providedClass,
			Factory<T> provider) {
		return withProvision(providedClass, c -> provider.create());
	}

	public <T> BindingContextImpl withProvision(Class<T> providedClass,
			Function<BindingContext, T> provider) {
		BindingContextImpl base = this;

		return new BindingContextImpl(this, bindingTargetStack, input,
				new BindingProvisions() {
					@SuppressWarnings("unchecked")
					@Override
					public <U> U provide(Class<U> clazz, BindingContext headContext) {
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

	public <I> I attemptUntilSuccessful(Iterable<I> attemptItems,
			BiConsumer<BindingContextImpl, I> bindingMethod,
			Function<Set<Exception>, BindingException> onFailure) {
		throw new BindingException("attemptUntilSuccessful unimplemented.", this);
	}
}

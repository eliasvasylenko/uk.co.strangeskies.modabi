package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class ElementNodeConfiguratorImpl<T>
		extends
		AbstractModelConfiguratorImpl<ElementNodeConfigurator<T>, ElementNode<T>, T>
		implements ElementNodeConfigurator<T> {
	protected static class ElementNodeImpl<T> extends AbstractModelImpl<T>
			implements ElementNode<T> {
		private final Range<Integer> occurances;
		private final Boolean iterable;
		private final String outMethodName;
		private final Method outMethod;
		private final String inMethodName;
		private final Method inMethod;
		private final Boolean inMethodChained;

		public ElementNodeImpl(ElementNodeConfiguratorImpl<T> configurator) {
			super(configurator);

			occurances = configurator.occurances;
			iterable = configurator.iterable;
			outMethodName = configurator.outMethodName;
			inMethodName = configurator.inMethodName;

			if (outMethodName == "this" && !iterable)
				throw new SchemaException();

			try {
				Class<?> outputClass = configurator.parent().getDataClass();

				outMethod = (outputClass == null || outMethodName == null || outMethodName == "this") ? null
						: outputClass.getMethod(outMethodName);

				if (isOutMethodIterable() != null && !isOutMethodIterable()
						&& getDataClass() != null && outMethod != null
						&& !getDataClass().isAssignableFrom(outMethod.getReturnType()))
					throw new SchemaException();

				Class<?> inputClass = configurator.parent()
						.getCurrentChildPreInputClass();
				inMethod = (inputClass == null || getDataClass() == null || inMethodName == null) ? null
						: inputClass.getMethod(inMethodName, getDataClass());
			} catch (NoSuchMethodException | SecurityException e) {
				throw new SchemaException(e);
			}
			inMethodChained = configurator.inMethodChained;
		}

		ElementNodeImpl(ElementNode<T> node,
				Collection<? extends ElementNode<T>> overriddenNodes,
				List<SchemaNode> effectiveChildren) {
			super(node, overriddenNodes, effectiveChildren);

			occurances = getValue(node, overriddenNodes, n -> n.getOccurances(), (v,
					o) -> o.contains(v));

			iterable = getValue(node, overriddenNodes, n -> n.isOutMethodIterable(),
					(n, o) -> Objects.equals(n, o));

			outMethodName = getValue(node, overriddenNodes, n -> n.getOutMethodName());

			outMethod = getValue(node, overriddenNodes, n -> n.getOutMethod(),
					(m, n) -> m.equals(n));

			inMethodName = getValue(node, overriddenNodes, n -> n.getInMethodName());

			inMethod = getValue(node, overriddenNodes, n -> n.getInMethod(),
					(m, n) -> m.equals(n));

			inMethodChained = getValue(node, overriddenNodes,
					n -> n.isInMethodChained());
		}

		@Override
		public Range<Integer> getOccurances() {
			return occurances;
		}

		@Override
		public String getOutMethodName() {
			return outMethodName;
		}

		@Override
		public Boolean isOutMethodIterable() {
			return iterable;
		}

		@Override
		public String getInMethodName() {
			return inMethodName;
		}

		@Override
		public Boolean isInMethodChained() {
			return inMethodChained;
		}

		@Override
		public <U> U process(SchemaProcessingContext<U> context) {
			return context.accept(this);
		}

		@Override
		public Method getOutMethod() {
			return outMethod;
		}

		@Override
		public Method getInMethod() {
			return inMethod;
		}
	}

	private Range<Integer> occurances;
	private Boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Boolean inMethodChained;

	public ElementNodeConfiguratorImpl(BranchingNodeConfiguratorImpl<?, ?> parent) {
		super(parent);
	}

	@Override
	public ElementNode<T> tryCreate() {
		return new ElementNodeImpl<>(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V extends T> ElementNodeConfigurator<V> dataClass(
			Class<V> dataClass) {
		return (ElementNodeConfigurator<V>) super.dataClass(dataClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementNodeConfigurator<V> baseModel(
			Model<? super V>... base) {
		return (ElementNodeConfigurator<V>) super.baseModel(base);
	}

	@Override
	public ElementNodeConfigurator<T> occurances(Range<Integer> occuranceRange) {
		this.occurances = occuranceRange;
		return this;
	}

	@Override
	public ElementNodeConfigurator<T> inMethod(String inMethodName) {
		this.inMethodName = inMethodName;
		return this;
	}

	@Override
	public ElementNodeConfigurator<T> inMethodChained(boolean chained) {
		this.inMethodChained = chained;
		return this;
	}

	@Override
	public ElementNodeConfigurator<T> outMethod(String outMethodName) {
		this.outMethodName = outMethodName;
		return this;
	}

	@Override
	public ElementNodeConfigurator<T> outMethodIterable(boolean iterable) {
		this.iterable = iterable;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ElementNode<T>> getNodeClass() {
		return (Class<ElementNode<T>>) (Object) ElementNode.class;
	}

	@Override
	protected ElementNode<T> getEffective(ElementNode<T> node) {
		return new ElementNodeImpl<>(node, getOverriddenNodes(),
				getEffectiveChildren());
	}
}

package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.building.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;

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
			if (outMethodName == "this" && !iterable)
				throw new SchemaException();
			inMethodName = configurator.inMethodName;

			Method outMethod = null;
			try {
				Class<?> outputClass = configurator.getParentClass();
				Class<?> resultClass = (isOutMethodIterable() == null || isOutMethodIterable()) ? resultClass = Iterable.class
						: getDataClass();
				outMethod = (getId() == null || outputClass == null
						|| resultClass == null || outMethodName == "this") ? null
						: SchemaBinderImpl.findMethod(
								SchemaBinderImpl.generateOutMethodNames(this, resultClass),
								outputClass, resultClass);
			} catch (NoSuchMethodException | SecurityException e) {
				if ((getBaseModel() != null && !getBaseModel().isEmpty())
						|| outMethodName != null)
					throw new SchemaException(e);
			}
			this.outMethod = outMethod;

			Method inMethod = null;
			try {
				Class<?> inputClass = configurator.getPreInputClass();
				inMethod = (inputClass == null || getDataClass() == null || inMethodName == null) ? null
						: inputClass.getMethod(inMethodName, getDataClass());
			} catch (NoSuchMethodException | SecurityException e) {
				if ((getBaseModel() != null && !getBaseModel().isEmpty())
						|| inMethodName != null)
					throw new SchemaException(e);
			}

			this.inMethod = inMethod;

			inMethodChained = configurator.inMethodChained;
		}

		ElementNodeImpl(ElementNode<T> node,
				Collection<? extends ElementNode<T>> overriddenNodes,
				List<ChildNode> effectiveChildren, Class<?> parentClass) {
			super(node, overriddenNodes, effectiveChildren);

			occurances = getValue(node, overriddenNodes, n -> n.getOccurances(), (v,
					o) -> o.contains(v));

			iterable = getValue(node, overriddenNodes, n -> n.isOutMethodIterable(),
					(n, o) -> Objects.equals(n, o));

			outMethodName = getValue(node, overriddenNodes, n -> n.getOutMethodName());

			Class<?> resultClass = (isOutMethodIterable() == null || isOutMethodIterable()) ? resultClass = Iterable.class
					: getDataClass();
			Method inheritedOutMethod = getValue(node, overriddenNodes,
					n -> n.getOutMethod());
			try {
				outMethod = outMethodName == "this" ? null
						: inheritedOutMethod != null ? inheritedOutMethod
								: SchemaBinderImpl.findMethod(
										SchemaBinderImpl.generateOutMethodNames(this), parentClass,
										resultClass);
			} catch (NoSuchMethodException e) {
				throw new SchemaException(e);
			}

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
		public void process(SchemaProcessingContext context) {
			context.accept(this);
		}

		@Override
		public <U> U process(SchemaResultProcessingContext<U> context) {
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

		@Override
		public Class<?> getPreInputClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getPostInputClass() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Method getUnbindingMethod() {
			// TODO Auto-generated method stub
			return null;
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
	protected Class<ElementNode<T>> getNodeClass() {
		return (Class<ElementNode<T>>) (Object) ElementNode.class;
	}

	@Override
	protected ElementNode<T> getEffective(ElementNode<T> node) {
		return new ElementNodeImpl<>(node, getOverriddenNodes(),
				getEffectiveChildren(), getParentClass());
	}
}

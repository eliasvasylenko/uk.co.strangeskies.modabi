package uk.co.strangeskies.modabi.model.impl;

import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.EffectiveModel;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.SchemaResultProcessingContext;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;

public class ElementNodeWrapper<T> implements ElementNode<T> {
	private final AbstractModel<T> component;
	private final ElementNode<? super T> base;

	public ElementNodeWrapper(AbstractModel<T> component) {
		this.component = component;
		base = null;
	}

	public ElementNodeWrapper(EffectiveModel<T> component,
			ElementNode<? super T> base) {
		this.component = component;
		this.base = base;

		if (base.getDataClass() != null
				&& !base.getDataClass().isAssignableFrom(component.getDataClass()))
			throw new SchemaException();

		if (base.getBindingClass() != null
				&& !base.getBindingClass()
						.isAssignableFrom(component.getBindingClass()))
			throw new SchemaException();

		if (base.getUnbindingClass() != null
				&& !base.getUnbindingClass().isAssignableFrom(
						component.getUnbindingClass()))
			throw new SchemaException();

		if (!component.getBaseModel().containsAll(base.getBaseModel()))
			throw new SchemaException();

		if (base.getBindingStrategy() != null
				&& base.getBindingStrategy() != component.getBindingStrategy())
			throw new SchemaException();

		if (base.getUnbindingStrategy() != null
				&& base.getUnbindingStrategy() != component.getUnbindingStrategy())
			throw new SchemaException();

		if (base.getUnbindingMethodName() != null
				&& base.getUnbindingMethodName() != component.getUnbindingMethodName())
			throw new SchemaException();

		if (!base.getChildren().isEmpty())
			throw new SchemaException();
	}

	@Override
	public Boolean isAbstract() {
		return component.isAbstract();
	}

	@Override
	public List<Model<? super T>> getBaseModel() {
		return component.getBaseModel();
	}

	@Override
	public Class<T> getDataClass() {
		return component.getDataClass();
	}

	@Override
	public BindingStrategy getBindingStrategy() {
		return component.getBindingStrategy();
	}

	@Override
	public Class<?> getBindingClass() {
		return component.getBindingClass();
	}

	@Override
	public UnbindingStrategy getUnbindingStrategy() {
		return component.getUnbindingStrategy();
	}

	@Override
	public Class<?> getUnbindingClass() {
		return component.getUnbindingClass();
	}

	@Override
	public String getUnbindingMethodName() {
		return component.getUnbindingMethodName();
	}

	@Override
	public Method getUnbindingMethod() {
		return component.getUnbindingMethod();
	}

	@Override
	public String getId() {
		return component.getId();
	}

	@Override
	public void process(SchemaProcessingContext context) {
		component.process(context);
	}

	@Override
	public <U> U process(SchemaResultProcessingContext<U> context) {
		return component.process(context);
	}

	@Override
	public List<? extends ChildNode> getChildren() {
		return component.getChildren();
	}

	@Override
	public Method getOutMethod() {
		return base == null ? null : base.getOutMethod();
	}

	@Override
	public String getOutMethodName() {
		return base == null ? null : base.getOutMethodName();
	}

	@Override
	public Boolean isOutMethodIterable() {
		return base == null ? null : base.isOutMethodIterable();
	}

	@Override
	public Range<Integer> occurances() {
		return base == null ? null : base.occurances();
	}

	@Override
	public String getInMethodName() {
		return base == null ? null : base.getInMethodName();
	}

	@Override
	public Method getInMethod() {
		return base == null ? null : base.getInMethod();
	}

	@Override
	public Boolean isInMethodChained() {
		return base == null ? null : base.isInMethodChained();
	}

	@Override
	public Class<?> getPreInputClass() {
		return base == null ? null : base.getPreInputClass();
	}

	@Override
	public Class<?> getPostInputClass() {
		return base == null ? null : base.getPostInputClass();
	}
}

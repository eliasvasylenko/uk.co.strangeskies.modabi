package uk.co.strangeskies.modabi.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.model.ImplementationStrategy;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.SchemaNode;
import uk.co.strangeskies.modabi.model.building.AbstractModelConfigurator;

public abstract class AbstractModelConfiguratorImpl<S extends AbstractModelConfigurator<S, N, T>, N extends Model<T>, T>
		extends BranchingNodeConfiguratorImpl<S, N> implements
		AbstractModelConfigurator<S, N, T> {
	protected static abstract class AbstractModelImpl<T> extends
			BranchingNodeImpl implements Model<T> {

		public AbstractModelImpl(String id, List<SchemaNode> children) {
			super(id, children);
			// TODO Auto-generated constructor stub
		}

	}

	private Class<T> dataClass;
	private final List<Model<? super T>> baseModel;
	private Model<?> adaptionModel;
	private String buildMethodName;
	private Class<?> builderClass;
	private ImplementationStrategy bindingStrategy;
	private Boolean isAbstract;

	public AbstractModelConfiguratorImpl(NodeBuilderContext context) {
		super(context);

		baseModel = new ArrayList<>();
	}

	@Override
	protected void configuration() {
		super.configuration();
		foldOverride();
		if (!getBaseModel().isEmpty())
			foldModel(getBaseModel());
		else {
			if (isAbstract == null)
				isAbstract(false);
		}
	}

	protected void foldOverride() {
		String id = getId();
		if (id != null) {
			getContext().getOverride(id);
		}
	}

	protected void foldModel(List<Model<? super T>> models) {
		if (getDataClass() != null) {
			if (!models.stream().allMatch(
					model -> model.getDataClass().isAssignableFrom(getDataClass())))
				throw new IllegalArgumentException();
		} else if (models.size() > 1)
			throw new IllegalArgumentException();

		String id = getId();
		if (id == null)
			if (models.size() == 1)
				id = models.get(0).getId();

		if (isAbstract == null)
			isAbstract = models.get(0).isAbstract();

		for (Model<? super T> model : models) {
			getChildren().addAll(model.getChildren());
		}
	}

	@Override
	protected boolean assertReady() {
		assertHasId();
		if (!isAbstract())
			assertHasDataClass();
		return super.assertReady();
	}

	private void assertHasDataClass() {
		if (dataClass == null)
			throw new IllegalArgumentException();
	}

	@Override
	public final S isAbstract(boolean isAbstract) {
		assertConfigurable();
		this.isAbstract = isAbstract;

		return getThis();
	}

	protected final Boolean isAbstract() {
		return isAbstract;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			Model<? super V>... base) {
		assertConfigurable();
		AbstractModelConfiguratorImpl<?, ?, V> thisV = (AbstractModelConfiguratorImpl<?, ?, V>) this;
		thisV.baseModel.addAll(Arrays.asList(base));

		return thisV;
	}

	protected final List<Model<? super T>> getBaseModel() {
		return baseModel;
	}

	@Override
	public final S adaptionModel(Model<?> adaptionModel) {
		assertConfigurable();
		this.adaptionModel = adaptionModel;

		return getThis();
	}

	protected final Model<?> getAdaptionModel() {
		return adaptionModel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> AbstractModelConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		assertConfigurable();
		this.dataClass = (Class<T>) dataClass;

		return (AbstractModelConfigurator<?, ?, V>) this;
	}

	protected final Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	public final S builderClass(Class<?> factoryClass) {
		assertConfigurable();
		this.builderClass = factoryClass;

		return getThis();
	}

	protected final Class<?> getBuilderClass() {
		return builderClass;
	}

	@Override
	public final S builderMethod(String buildMethodName) {
		assertConfigurable();
		this.buildMethodName = buildMethodName;

		return getThis();
	}

	protected final String getBuilderMethod() {
		return buildMethodName;
	}

	@Override
	public final S implementationStrategy(ImplementationStrategy bindingStrategy) {
		assertConfigurable();
		this.bindingStrategy = bindingStrategy;

		return getThis();
	}

	protected final ImplementationStrategy getImplementationStrategy() {
		return bindingStrategy;
	}
}

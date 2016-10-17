package uk.co.strangeskies.modabi.impl.schema;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class BindingPointImpl<T> implements BindingPoint<T> {
	private final QualifiedName name;
	private final boolean concrete;
	private final TypeToken<T> dataType;
	private final SchemaNode node;
	private final List<Model<? super T>> baseModel;

	protected BindingPointImpl(BindingPointConfiguratorImpl<T, ?> configurator) {
		if (!configurator.isChildContextAbstract())
			requireConcreteDescendents(new ArrayDeque<>(), this);
	}

	@Override
	public QualifiedName name() {
		return name;
	}

	@Override
	public boolean concrete() {
		return concrete;
	}

	@Override
	public TypeToken<T> dataType() {
		return dataType;
	}

	@Override
	public SchemaNode node() {
		return node;
	}

	@Override
	public List<Model<? super T>> baseModel() {
		return baseModel;
	}

	protected void requireConcreteDescendents(Deque<ChildBindingPoint<?>> nodeStack, BindingPoint<?> top) {
		/*
		 * 
		 * 
		 * TODO do we need to do this? if a descendant is required to be concrete,
		 * shouldn't its configurator have isChildContextAbstract = false already so
		 * it can be checked directly?
		 * 
		 * 
		 * 
		 */
		for (ChildBindingPoint<?> child : top.node().childBindingPoints()) {
			if (!child.extensible())
				requireConcrete(nodeStack, child);

		}
	}

	protected void requireConcrete(Deque<ChildBindingPoint<?>> nodeStack, ChildBindingPoint<?> top) {
		if (!nodeStack.peek().concrete())
			throw new ModabiException(t -> t.mustOverrideDescendant(nodeStack));

		nodeStack.push(top);
		requireConcreteDescendents(nodeStack, top);
		nodeStack.pop();
	}
}

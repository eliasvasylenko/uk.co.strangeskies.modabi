package uk.co.strangeskies.modabi.node.builder.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.node.builder.BindingNodeBuilder;
import uk.co.strangeskies.modabi.node.impl.BindingNodeImpl;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class ElementSchemaNodeBuilderImpl<T, U extends SchemaProcessingContext<? extends U>>
		implements BindingNodeBuilder<T, U> {
	private String name;
	private final Set<SchemaNode<? super U>> children;
	private Class<T> dataClass;
	private Range<Integer> occurances;
	private BindingNode<? super T, U> base;
	private String buildMethodName;
	private boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private boolean inMethodChained;
	private Class<?> factoryClass;

	public ElementSchemaNodeBuilderImpl() {
		children = new HashSet<>();
	}

	@Override
	public BindingNode<T, U> create() {
		return new BindingNodeImpl<>(name, base, children, occurances, dataClass,
				factoryClass, inMethodName, inMethodChained, buildMethodName, iterable,
				outMethodName);
	}

	@Override
	public BindingNodeBuilder<T, U> name(String name) {
		this.name = name;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeBuilder<V, U> base(
			BindingNode<? super V, U> base) {
		this.base = (BindingNode<? super T, U>) base;

		return (BindingNodeBuilder<V, U>) this;
	}

	@Override
	public BindingNodeBuilder<T, U> occurances(Range<Integer> occuranceRange) {
		this.occurances = occuranceRange;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> BindingNodeBuilder<V, U> dataClass(Class<V> dataClass) {
		this.dataClass = (Class<T>) dataClass;

		return (BindingNodeBuilder<V, U>) this;
	}

	@Override
	public BindingNodeBuilder<T, U> addChild(SchemaNode<? super U> child) {
		children.add(child);

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> addChildren(
			Collection<? extends SchemaNode<? super U>> children) {
		this.children.addAll(children);

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> factoryClass(Class<?> factoryClass) {
		this.factoryClass = factoryClass;

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> inMethod(String inMethodName) {
		this.inMethodName = inMethodName;

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> outMethod(String outMethodName) {
		this.outMethodName = outMethodName;

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> iterable(boolean iterable) {
		this.iterable = iterable;

		return this;
	}

	@Override
	public BindingNodeBuilder<T, U> factoryMethod(String buildMethodName) {
		this.buildMethodName = buildMethodName;

		return this;
	}
}

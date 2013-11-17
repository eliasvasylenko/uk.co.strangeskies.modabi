package uk.co.strangeskies.modabi.schema.node.builder.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.builder.ElementSchemaNodeBuilder;
import uk.co.strangeskies.modabi.schema.node.impl.ElementSchemaNodeImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ElementSchemaNodeBuilderImpl<T, U extends SchemaProcessingContext<? extends U>>
		implements ElementSchemaNodeBuilder<T, U> {
	private String name;
	private final Set<SchemaNode<? super U>> children;
	private Class<T> dataClass;
	private Range<Integer> occurances;
	private ElementSchemaNode<? super T, U> base;
	private String buildMethodName;
	private boolean iterable;
	private String outMethodName;
	private String inMethodName;
	private Class<?> factoryClass;

	public ElementSchemaNodeBuilderImpl() {
		children = new HashSet<>();
	}

	@Override
	public ElementSchemaNode<T, U> create() {
		return new ElementSchemaNodeImpl<>(name, base, children, occurances,
				dataClass, factoryClass, inMethodName, buildMethodName, iterable,
				outMethodName);
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> name(String name) {
		this.name = name;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementSchemaNodeBuilder<V, U> base(
			ElementSchemaNode<? super V, U> base) {
		this.base = (ElementSchemaNode<? super T, U>) base;

		return (ElementSchemaNodeBuilder<V, U>) this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> occurances(Range<Integer> occuranceRange) {
		this.occurances = occuranceRange;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> ElementSchemaNodeBuilder<V, U> dataClass(
			Class<V> dataClass) {
		this.dataClass = (Class<T>) dataClass;

		return (ElementSchemaNodeBuilder<V, U>) this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> addChild(SchemaNode<? super U> child) {
		children.add(child);

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> addChildren(
			Collection<? extends SchemaNode<? super U>> children) {
		this.children.addAll(children);

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> factoryClass(Class<?> factoryClass) {
		this.factoryClass = factoryClass;

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> inMethod(String inMethodName) {
		this.inMethodName = inMethodName;

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> outMethod(String outMethodName) {
		this.outMethodName = outMethodName;

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> iterable(boolean iterable) {
		this.iterable = iterable;

		return this;
	}

	@Override
	public ElementSchemaNodeBuilder<T, U> buildMethod(String buildMethodName) {
		this.buildMethodName = buildMethodName;

		return this;
	}
}

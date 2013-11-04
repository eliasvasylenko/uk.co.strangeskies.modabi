package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public class ElementSchemaNodeImpl<T> extends BranchingSchemaNodeImpl implements
		ElementSchemaNode<T> {
	private final boolean iterable;
	private final String buildMethod;
	private final String outMethod;
	private final Class<?> buildClass;
	private final Class<T> dataClass;
	private final Range<Integer> occurances;
	private final String name;
	private final ElementSchemaNode<? super T> base;

	public ElementSchemaNodeImpl(String name, ElementSchemaNode<? super T> base,
			Collection<? extends SchemaNode> children, Range<Integer> occurances,
			boolean choice, Class<T> dataClass, Class<?> buildClass, String inMethod,
			String buildMethod, boolean iterable, String outMethod) {
		super(children, choice, inMethod);

		this.name = name;

		this.base = base;

		this.dataClass = dataClass;

		this.buildClass = buildClass;

		this.outMethod = outMethod;

		this.buildMethod = buildMethod;

		this.occurances = occurances.copy();

		this.iterable = iterable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Range<Integer> getOccurances() {
		return occurances;
	}

	@Override
	public Class<T> getDataClass() {
		return dataClass;
	}

	@Override
	public Class<?> getBuildClass() {
		return buildClass;
	}

	@Override
	public String getOutMethod() {
		return outMethod;
	}

	@Override
	public boolean isIterable() {
		return iterable;
	}

	@Override
	public String getBuildMethod() {
		return buildMethod;
	}

	@Override
	public ElementSchemaNode<? super T> getBase() {
		return base;
	}
}

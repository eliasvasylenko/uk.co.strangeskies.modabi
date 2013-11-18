package uk.co.strangeskies.modabi.schema.node.impl;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ElementSchemaNodeImpl<T, U extends SchemaProcessingContext<? extends U>>
		extends BranchingSchemaNodeImpl<U> implements ElementSchemaNode<T, U> {
	private final boolean iterable;
	private final String buildMethod;
	private final String outMethod;
	private final Class<?> buildClass;
	private final Class<T> dataClass;
	private final Range<Integer> occurances;
	private final String name;
	private final ElementSchemaNode<? super T, ? super U> base;

	public ElementSchemaNodeImpl(String name,
			ElementSchemaNode<? super T, ? super U> base,
			Collection<? extends SchemaNode<? super U>> children,
			Range<Integer> occurances, Class<T> dataClass, Class<?> buildClass,
			String inMethod, String buildMethod, boolean iterable, String outMethod) {
		super(children, inMethod);

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
	public Class<?> getBuilderClass() {
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
	public String getFactoryMethod() {
		return buildMethod;
	}

	@Override
	public ElementSchemaNode<? super T, ? super U> getBase() {
		return base;
	}

	@Override
	public void process(U context) {
		context.element(this);
	}
}

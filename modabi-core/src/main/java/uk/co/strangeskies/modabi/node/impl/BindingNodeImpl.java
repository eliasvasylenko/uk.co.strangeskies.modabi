package uk.co.strangeskies.modabi.node.impl;

import java.util.Collection;

import uk.co.strangeskies.gears.mathematics.Range;
import uk.co.strangeskies.modabi.node.BindingNode;
import uk.co.strangeskies.modabi.node.SchemaNode;
import uk.co.strangeskies.modabi.processing.SchemaProcessingContext;

public class BindingNodeImpl<T> extends BranchingNodeImpl implements
		BindingNode<T> {
	private final boolean iterable;
	private final String buildMethod;
	private final String outMethod;
	private final Class<?> buildClass;
	private final Class<T> dataClass;
	private final Range<Integer> occurances;
	private final String name;
	private final BindingNode<? super T> base;

	public BindingNodeImpl(String name, BindingNode<? super T> base,
			Collection<? extends SchemaNode> children, Range<Integer> occurances,
			Class<T> dataClass, Class<?> buildClass, String inMethod,
			boolean inMethodChained, String buildMethod, boolean iterable,
			String outMethod) {
		super(children, inMethod, inMethodChained);

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
	public Class<T> getBindingClass() {
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
	public String getBuildMethod() {
		return buildMethod;
	}

	@Override
	public BindingNode<? super T> getBase() {
		return base;
	}

	@Override
	public void process(SchemaProcessingContext context) {
		context.accept(this);
	}
}

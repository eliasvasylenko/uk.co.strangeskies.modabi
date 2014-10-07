package uk.co.strangeskies.modabi.schema.node.building.configuration.impl;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.Methods;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.OverrideMerge;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities.SchemaNodeConfigurationContext;

public class InputNodeConfigurationHelper<N extends InputNode<N, E>, E extends InputNode.Effective<N, E>> {
	private final E effective;
	private final OverrideMerge<N, ? extends ChildNodeConfigurator<?, N, ?, ?>> overrideMerge;
	private final SchemaNodeConfigurationContext<? super N> context;

	public InputNodeConfigurationHelper(
			E effective,
			OverrideMerge<N, ? extends ChildNodeConfigurator<?, N, ?, ?>> overrideMerge,
			SchemaNodeConfigurationContext<? super N> context) {
		this.effective = effective;
		this.overrideMerge = overrideMerge;
		this.context = context;
	}

	public Boolean isInMethodChained() {
		return overrideMerge.getValue(InputNode::isInMethodChained, false);
	}

	public Boolean isInMethodCast() {
		return effective.isInMethodChained() != null
				&& !effective.isInMethodChained() ? null : overrideMerge.getValue(
				InputNode::isInMethodCast, false);
	}

	private Class<?> inputTargetClass() {
		return context.inputTargetClass(effective.getName());
	}

	public Executable inMethod(List<Class<?>> parameters) {
		String overriddenInMethodName = overrideMerge
				.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected())
			if (overriddenInMethodName == null)
				overriddenInMethodName = "null";
			else if (overriddenInMethodName != "null")
				throw new SchemaException(
						"In method name should not be provided for this node.");

		Executable inMethod;

		if (effective.isAbstract() || "null".equals(overriddenInMethodName)) {
			inMethod = null;
		} else if (context.isConstructorExpected()) {
			inMethod = Methods.getConstructor(effective, inputTargetClass(),
					parameters);
		} else {
			inMethod = Methods.getInMethod(effective, overriddenInMethodName,
					inputTargetClass(), parameters);
		}

		return inMethod;
	}

	public String inMethodName() {
		String inMethodName = overrideMerge.tryGetValue(InputNode::getInMethodName);

		if (!context.isInputExpected() && inMethodName == null)
			inMethodName = "null";

		if (context.isInputExpected() && inMethodName == null
				&& !effective.isAbstract())
			inMethodName = effective.getInMethod().getName();

		return inMethodName;
	}

	public Class<?> preInputClass() {
		return (effective.isAbstract() || "null"
				.equals(effective.getInMethodName())) ? null : effective.getInMethod()
				.getDeclaringClass();
	}

	public Class<?> postInputClass() {
		Class<?> postInputClass;

		if ("null".equals(effective.getInMethodName())
				|| (effective.isInMethodChained() != null && !effective
						.isInMethodChained())) {
			postInputClass = inputTargetClass();
		} else if (effective.isAbstract()) {
			postInputClass = overrideMerge.tryGetValue(InputNode::getPostInputClass,
					(n, o) -> o.isAssignableFrom(n));
		} else {
			Class<?> methodReturn;

			if (context.isConstructorExpected())
				methodReturn = effective.getInMethod().getDeclaringClass();
			else
				methodReturn = ((Method) effective.getInMethod()).getReturnType();

			Class<?> localPostInputClass = overrideMerge.node().getPostInputClass();

			if (localPostInputClass == null
					|| localPostInputClass.isAssignableFrom(methodReturn))
				localPostInputClass = methodReturn;

			postInputClass = overrideMerge.getValueWithOverride(localPostInputClass,
					InputNode::getPostInputClass, (n, o) -> o.isAssignableFrom(n));
		}

		return postInputClass;
	}
}

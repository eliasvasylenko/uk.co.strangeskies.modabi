package uk.co.strangeskies.modabi.schema.processing.binding.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.invoker.NullInvoker;

import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingException;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.impl.PartialSchemaProcessingContext;
import uk.co.strangeskies.utilities.IdentityProperty;

public class BindingNodeBinder {
	private final BindingContext context;

	public BindingNodeBinder(BindingContext context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> U bind(BindingNode.Effective<U, ?, ?> node) {
		BindingContext childContext = context.withBindingNode(node).withProvision(
				BindingNode.Effective.class, () -> node);

		Object binding;
		List<ChildNode.Effective<?, ?>> children = node.children();

		BindingStrategy strategy = node.getBindingStrategy();
		if (strategy == null)
			strategy = BindingStrategy.PROVIDED;

		switch (strategy) {
		case PROVIDED:
			Class<?> providedClass = node.getBindingClass() != null ? node
					.getBindingClass() : node.getDataClass();
			binding = context.provide(providedClass);

			break;
		case CONSTRUCTOR:
			ChildNode.Effective<?, ?> firstChild = children.get(0);
			children = children.subList(1, children.size());

			Executable inputMethod = getInputMethod(firstChild);
			List<Object> parameters = getSingleBindingSequence(firstChild,
					childContext);
			try {
				binding = ((Constructor<?>) inputMethod).newInstance(parameters
						.toArray());
			} catch (IllegalAccessException | InvocationTargetException
					| InstantiationException e) {
				throw new BindingException("Cannot invoke static factory method '"
						+ inputMethod + "' on class '" + node.getUnbindingClass()
						+ "' with parameters '" + parameters + "'.", context, e);
			}
			break;
		case IMPLEMENT_IN_PLACE:
			// TODO some proxy magic with simple bean-like semantics
			binding = new ProxyFactory().createInvokerProxy(new NullInvoker(),
					new Class[] { node.getDataClass() });

			break;
		case SOURCE_ADAPTOR:
			binding = getSingleBinding(children.get(0), childContext);
			children = children.subList(1, children.size());
			break;
		case STATIC_FACTORY:
			firstChild = children.get(0);
			children = children.subList(1, children.size());

			inputMethod = getInputMethod(firstChild);
			parameters = getSingleBindingSequence(firstChild, childContext);
			try {
				binding = ((Method) inputMethod).invoke(null, parameters.toArray());
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Cannot invoke static factory method '"
						+ inputMethod + "' on class '" + node.getUnbindingClass()
						+ "' with parameters '" + parameters + "'.", context, e);
			}
			break;
		case TARGET_ADAPTOR:
			binding = context.bindingTarget();
			break;
		default:
			throw new AssertionError();
		}

		childContext = childContext.withBindingTarget(binding);

		for (ChildNode.Effective<?, ?> child : children) {
			binding = bindChild(child, childContext);
			childContext = childContext.withReplacedBindingTarget(binding);
		}

		return (U) binding;
	}

	private Object bindChild(ChildNode.Effective<?, ?> next,
			BindingContext context) {
		IdentityProperty<Object> result = new IdentityProperty<>(
				context.bindingTarget());

		next.process(new SchemaProcessingContext() {
			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				process(node, new ComplexNodeBinder(context).bind(node), context);
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				process(node, new DataNodeBinder(context).bind(node), context);
			}

			public void process(InputNode.Effective<?, ?> node, List<?> data,
					BindingContext context) {
				for (Object item : data)
					result.set(invokeInMethod(node, context, result.get(), item));
			}

			@Override
			public void accept(InputSequenceNode.Effective node) {
				List<Object> parameters = getSingleBindingSequence(node,
						context.withBindingNode(node));
				result.set(invokeInMethod(node, context, result.get(),
						parameters.toArray()));
			}

			@Override
			public void accept(SequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					bindChild(child, context.withBindingNode(node));
			}

			@Override
			public void accept(ChoiceNode.Effective node) {
			}
		});

		return result.get();
	}

	private static Object invokeInMethod(InputNode.Effective<?, ?> node,
			BindingContext context, Object target, Object... parameters) {
		if (!"null".equals(node.getInMethodName())) {
			Object object;

			try {
				object = ((Method) node.getInMethod()).invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ node.getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + node + "'.",
						context, e);
			}

			if (node.isInMethodChained())
				target = object;
		}

		return target;
	}

	private static Executable getInputMethod(ChildNode.Effective<?, ?> node) {
		IdentityProperty<Executable> result = new IdentityProperty<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputNode.Effective<?, ?> node) {
				result.set(node.getInMethod());
			}
		});
		return result.get();
	}

	private static List<Object> getSingleBindingSequence(
			ChildNode.Effective<?, ?> node, BindingContext context) {
		List<Object> parameters = new ArrayList<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					parameters.add(getSingleBinding(child, context));
			}

			@Override
			public <U> void accept(BindingChildNode.Effective<U, ?, ?> node) {
				parameters.add(getSingleBinding(node, context));
			}
		});

		return parameters;
	}

	private static Object getSingleBinding(ChildNode.Effective<?, ?> node,
			BindingContext context) {
		IdentityProperty<Object> result = new IdentityProperty<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				result.set(new ComplexNodeBinder(context).bind(node).get(0));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.set(new DataNodeBinder(context).bind(node).get(0));
			}
		});
		return result.get();
	}
}

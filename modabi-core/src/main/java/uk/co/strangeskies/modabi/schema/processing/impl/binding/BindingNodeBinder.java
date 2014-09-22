package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.invoker.NullInvoker;

import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.impl.PartialSchemaProcessingContext;
import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.ResultWrapper;

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
		Iterator<ChildNode.Effective<?, ?>> children = node.children().iterator();

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
			binding = null;
			/*-
			try {
				List<Binding<?>> input = getInput();
				Class<?> a = null;
				a.getc
				Constructor<?> c = node.getBindingClass().getConstructor();
				return c.newInstance(prepareUnbingingParameterList(node, u));
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				throw new SchemaException("Cannot invoke constructor " + c + " on "
						+ node.getUnbindingClass(), e);
			}
			 */
			break;
		case IMPLEMENT_IN_PLACE:
			// TODO some proxy magic with simple bean-like semantics
			binding = new ProxyFactory().createInvokerProxy(new NullInvoker(),
					new Class[] { node.getDataClass() });

			break;
		case SOURCE_ADAPTOR:
			binding = getSingleBinding(children.next(), childContext);
			break;
		case STATIC_FACTORY:
			ChildNode.Effective<?, ?> firstChild = children.next();

			Method inputMethod = getInputMethod(firstChild);
			List<Object> parameters = getSingleBindingSequence(firstChild,
					childContext);
			try {
				binding = inputMethod.invoke(null, parameters.toArray());
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw context.exception("Cannot invoke static factory method '"
						+ inputMethod + "' on class '" + node.getUnbindingClass()
						+ "' with parameters '" + parameters + "'.", e);
			}
			break;
		case TARGET_ADAPTOR:
			binding = context.bindingTarget();
			break;
		default:
			throw new AssertionError();
		}

		childContext = childContext.withBindingTarget(binding);
		System.out.println(node.getName() + " ? "
				+ childContext.bindingTargetStack());

		while (children.hasNext()) {
			ChildNode.Effective<?, ?> next = children.next();
			childContext = childContext.withReplacedBindingTarget(binding);
			binding = bindChild(next, childContext);
			System.out.println(next.getName() + " ? "
					+ childContext.bindingTargetStack());
		}

		return (U) binding;
	}

	private Object bindChild(ChildNode.Effective<?, ?> next,
			BindingContext context) {
		IdentityProperty<Object> result = new IdentityProperty<>(
				context.bindingTarget());

		next.process(new SchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				process(node, new ElementNodeBinder(context).bind(node), context);
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
				object = node.getInMethod().invoke(target, parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw context.exception("Unable to call method '" + node.getInMethod()
						+ "' with parameters '" + Arrays.toString(parameters) + "'.", e);
			}

			if (node.isInMethodChained())
				target = object;
		}

		return target;
	}

	private static Method getInputMethod(ChildNode.Effective<?, ?> node) {
		ResultWrapper<Method> result = new ResultWrapper<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputNode.Effective<?, ?> node) {
				result.setResult(node.getInMethod());
			}
		});
		return result.getResult();
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
		ResultWrapper<Object> result = new ResultWrapper<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				result.setResult(new ElementNodeBinder(context).bind(node).get(0));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.setResult(new DataNodeBinder(context).bind(node).get(0));
			}
		});
		return result.getResult();
	}
}

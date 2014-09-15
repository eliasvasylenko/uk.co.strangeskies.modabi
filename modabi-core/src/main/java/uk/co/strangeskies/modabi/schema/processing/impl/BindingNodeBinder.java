package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.invoker.NullInvoker;

import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode.Effective;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.PartialSchemaProcessingContext;
import uk.co.strangeskies.utilities.ResultWrapper;

public class BindingNodeBinder {
	private final BindingContext bindingContext;

	public BindingNodeBinder(BindingContext bindingContext) {
		this.bindingContext = bindingContext;
	}

	public BindingContext getBindingContext() {
		return bindingContext;
	}

	@SuppressWarnings("unchecked")
	public <U> U bindNode(BindingNode.Effective<U, ?, ?> node) {
		Object binding;
		Iterator<ChildNode.Effective<?, ?>> children = node.children().iterator();

		BindingStrategy strategy = node.getBindingStrategy();
		if (strategy == null)
			strategy = BindingStrategy.PROVIDED;

		switch (strategy) {
		case PROVIDED:
			Class<?> providedClass = node.getBindingClass() != null ? node
					.getBindingClass() : node.getDataClass();
			binding = bindingContext.provide(providedClass);

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
			binding = tryGetBinding(children.next());
			break;
		case STATIC_FACTORY:
			ChildNode.Effective<?, ?> firstChild = children.next();

			Method inputMethod = getInputMethod(firstChild);
			List<Object> parameters = getBindings(firstChild);
			try {
				binding = inputMethod.invoke(null, parameters.toArray());
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw bindingContext.exception("Cannot invoke static factory method '"
						+ inputMethod + "' on class '" + node.getUnbindingClass()
						+ "' with parameters '" + parameters + "'.", e);
			}
			break;
		case TARGET_ADAPTOR:
			binding = bindingContext.bindingObject();
			break;
		default:
			throw new AssertionError();
		}

		while (children.hasNext())
			binding = bindChild(children.next(), binding);

		return (U) binding;
	}

	private Object bindChild(Effective<?, ?> next, Object binding) {
		// TODO Auto-generated method stub
		return null;
	}

	private Method getInputMethod(ChildNode.Effective<?, ?> node) {
		ResultWrapper<Method> result = new ResultWrapper<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputNode.Effective<?, ?> node) {
				result.setResult(node.getInMethod());
			}
		});
		return result.getResult();
	}

	private List<Object> getBindings(ChildNode.Effective<?, ?> node) {
		List<Object> parameters = new ArrayList<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					parameters.add(tryGetBinding(child));
			}

			@Override
			public <U> void accept(BindingChildNode.Effective<U, ?, ?> node) {
				parameters.add(tryGetBinding(node));
			}
		});
		return parameters;
	}

	private Object tryGetBinding(ChildNode.Effective<?, ?> node) {
		ResultWrapper<Object> result = new ResultWrapper<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				result.setResult(bindElementNode(node).get(0));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.setResult(bindDataNode(node).get(0));
			}
		});
		return result.getResult();
	}
}

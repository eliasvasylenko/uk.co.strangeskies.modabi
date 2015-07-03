/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.invoker.NullInvoker;

import uk.co.strangeskies.modabi.SchemaProcessingContext;
import uk.co.strangeskies.modabi.impl.PartialSchemaProcessingContext;
import uk.co.strangeskies.modabi.processing.BindingContext;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;

public class BindingNodeBinder {
	private final BindingContextImpl context;

	public BindingNodeBinder(BindingContextImpl context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> U bind(BindingNode.Effective<U, ?, ?> node) {
		BindingContextImpl childContext = context.withBindingNode(node)
				.withProvision(BindingNode.Effective.class, () -> node);

		Object binding;
		List<ChildNode.Effective<?, ?>> children = node.children();

		BindingStrategy strategy = node.getBindingStrategy();
		if (strategy == null)
			strategy = BindingStrategy.PROVIDED;

		switch (strategy) {
		case PROVIDED:
			TypeToken<?> providedType = node.getBindingType() != null ? node
					.getBindingType() : node.getDataType();
			binding = context.provisions().provide(providedType);

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
						+ inputMethod + "' on class '" + node.getUnbindingType()
						+ "' with parameters '" + parameters + "'", context, e);
			}
			break;
		case IMPLEMENT_IN_PLACE:
			/*
			 * TODO some proxy magic with simple bean-like semantics. Remember, this
			 * may be more complex if we want proper *generic* type safety!
			 */
			binding = new ProxyFactory().createInvokerProxy(new NullInvoker(),
					new Class[] { node.getDataType().getRawType() });

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
						+ inputMethod + "' on class '" + node.getUnbindingType()
						+ "' with parameters '" + parameters + "'", context, e);
			}
			break;
		case TARGET_ADAPTOR:
			binding = context.bindingTarget();
			break;
		default:
			throw new AssertionError();
		}

		BindingContextImpl rootContext = childContext;
		childContext = rootContext.withBindingTarget(binding);

		for (ChildNode.Effective<?, ?> child : children) {
			binding = bindChild(child, childContext);
			childContext = rootContext.withBindingTarget(binding);
		}

		return (U) binding;
	}

	private Object bindChild(ChildNode.Effective<?, ?> next,
			BindingContextImpl context) {
		IdentityProperty<Object> result = new IdentityProperty<>(
				context.bindingTarget());

		try {
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
					BindingContextImpl childContext = context.withBindingNode(node);
					for (ChildNode.Effective<?, ?> child : node.children())
						bindChild(child, childContext);
				}

				@Override
				public void accept(ChoiceNode.Effective node) {
					try {
						context.withBindingNode(node).attemptUntilSuccessful(
								node.children(),
								(c, n) -> bindChild(n, c),
								n -> new BindingException(
										"Option '" + n + "' under choice node '" + node
												+ "' could not be unbound", context, n));
					} catch (Exception e) {
						if (node.isMandatory() != null && node.isMandatory())
							throw e;
					}
				}
			});
		} catch (Exception e) {
			throw new BindingException("Failed to bind node '" + next + "'", context,
					e);
		}

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
						+ Arrays.toString(parameters) + "' at node '" + node + "'",
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
			ChildNode.Effective<?, ?> node, BindingContextImpl context) {
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
			BindingContextImpl context) {
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

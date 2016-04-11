/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.ReturningNodeProcessor;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.IdentityProperty;

public class BindingNodeBinder {
	private final ProcessingContextImpl context;

	public BindingNodeBinder(ProcessingContextImpl context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> U bind(BindingNode.Effective<U, ?, ?> node) {
		/*
		 * We need to replace the current binding node here as it may have been
		 * overridden in the case that this node is extensible.
		 */
		ProcessingContextImpl context = this.context.withReplacementBindingNode(node);

		TypedObject<?> binding;
		List<ChildNode.Effective<?, ?>> children = node.children();

		BindingStrategy strategy = node.getBindingStrategy();
		if (strategy == null)
			strategy = BindingStrategy.PROVIDED;

		TypeToken<?> bindingType = node.getBindingType() != null ? node.getBindingType() : node.getDataType();

		switch (strategy) {
		case PROVIDED:
			binding = this.context.provide(bindingType);

			break;
		case CONSTRUCTOR:
			if (children.isEmpty())
				throw new SchemaException("Node '" + node.getName() + "' with binding strategy '" + BindingStrategy.CONSTRUCTOR
						+ "' should contain at least one child");

			ChildNode.Effective<?, ?> firstChild;
			List<NodeBinding<?>> parameters;
			do {
				firstChild = children.get(0);
				children = children.subList(1, children.size());

				parameters = getSingleBindingSequence(firstChild, context);
			} while (parameters == null);

			if (isBindingNode(firstChild)) {
				firstChild = parameters.get(0).getExactNode();
			}
			Executable inputMethod = getInputMethod(firstChild);

			try {
				Object[] parameterArray = parameters.stream().map(NodeBinding::getBinding).toArray();
				binding = TypedObject.castInto(bindingType, ((Constructor<?>) inputMethod).newInstance(parameterArray));
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
				throw new ProcessingException("Cannot invoke static factory method '" + inputMethod + "' on class '"
						+ node.getUnbindingType() + "' with parameters '" + parameters + "'", context, e);
			}
			break;
		case STATIC_FACTORY:
			if (children.isEmpty())
				throw new SchemaException("Node '" + node.getName() + "' with binding strategy '"
						+ BindingStrategy.STATIC_FACTORY + "' should contain at least one child");

			do {
				firstChild = children.get(0);
				children = children.subList(1, children.size());

				parameters = getSingleBindingSequence(firstChild, context);
			} while (parameters == null);

			if (isBindingNode(firstChild)) {
				firstChild = parameters.get(0).getExactNode();
			}
			inputMethod = getInputMethod(firstChild);

			try {
				Object[] parameterArray = parameters.stream().map(NodeBinding::getBinding).toArray();
				binding = TypedObject.castInto(bindingType, ((Method) inputMethod).invoke(null, parameterArray));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				throw new ProcessingException("Cannot invoke static factory method '" + inputMethod + "' on class '"
						+ node.getUnbindingType() + "' with parameters '" + parameters + "'", context, e);
			}
			break;
		case IMPLEMENT_IN_PLACE:
			/*
			 * TODO some proxy magic with simple bean-like semantics. Remember, this
			 * may be more complex if we want proper *generic* type safety!
			 */
			Set<? extends Class<?>> classes = node.getDataType().getRawTypes();

			binding = new TypedObject<>(node.getDataType(), (U) Proxy.newProxyInstance(getClass().getClassLoader(),
					classes.toArray(new Class<?>[classes.size()]), new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							return null;
						}
					}));

			break;
		case SOURCE_ADAPTOR:
			binding = getSingleBinding(children.get(0), context).getTypedBinding();
			children = children.subList(1, children.size());
			break;
		case TARGET_ADAPTOR:
			binding = context.getBindingObject();
			break;
		default:
			throw new AssertionError();
		}

		context = context.withBindingObject(binding);

		for (ChildNode.Effective<?, ?> child : children) {
			context = ChildNodeBinder.bind(context, child);
			binding = context.getBindingObject();
		}

		return (U) binding.getObject();
	}

	private static boolean isBindingNode(ChildNode.Effective<?, ?> child) {
		return child.process(new ReturningNodeProcessor<Boolean>() {
			@Override
			public <U> Boolean accept(ComplexNode.Effective<U> node) {
				return true;
			}

			@Override
			public <U> Boolean accept(DataNode.Effective<U> node) {
				return true;
			}

			@Override
			public Boolean acceptDefault(SchemaNode.Effective<?, ?> node) {
				return false;
			}
		});
	}

	private static Executable getInputMethod(ChildNode.Effective<?, ?> node) {
		IdentityProperty<Executable> result = new IdentityProperty<>();
		node.process(new NodeProcessor() {
			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				result.set(node.getInMethod());
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.set(node.getInMethod());
			}

			@Override
			public void accept(InputSequenceNode.Effective node) {
				result.set(node.getInMethod());
			}
		});
		return result.get();
	}

	public static List<NodeBinding<?>> getSingleBindingSequence(ChildNode.Effective<?, ?> node,
			ProcessingContextImpl context) {
		List<NodeBinding<?>> parameters = new ArrayList<>();
		node.process(new NodeProcessor() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					parameters.add(getSingleBinding(child, context));
			}

			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				parameters.add(getSingleBinding(node, context));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				parameters.add(getSingleBinding(node, context));
			}
		});

		return parameters;
	}

	public static NodeBinding<?> getSingleBinding(ChildNode.Effective<?, ?> node, ProcessingContextImpl context) {
		IdentityProperty<NodeBinding<?>> result = new IdentityProperty<>();
		node.process(new NodeProcessor() {
			@Override
			public <U> void accept(ComplexNode.Effective<U> node) {
				List<NodeBinding<U>> results = new ComplexNodeBinder<>(context, node).getBinding();

				check(node, results);
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				List<NodeBinding<U>> results = new DataNodeBinder<>(context, node).getBinding();

				check(node, results);
			}

			private <U> void check(BindingChildNode.Effective<U, ?, ?> node, List<NodeBinding<U>> results) {
				if (!results.isEmpty()) {
					result.set(results.get(0));
				} else if (!node.occurrences().contains(0)) {
					throw new ProcessingException("Node must be bound data", context);
				} else {
					result.set(new NodeBinding<>(null, node));
				}
			}
		});
		return result.get();
	}
}

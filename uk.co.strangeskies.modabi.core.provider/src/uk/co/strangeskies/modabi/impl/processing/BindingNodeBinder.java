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

import uk.co.strangeskies.modabi.ChildNodeBinding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.ReturningNodeProcessor;
import uk.co.strangeskies.modabi.declarative.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SimpleNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;
import uk.co.strangeskies.utilities.IdentityProperty;

public class BindingNodeBinder {
	private final ProcessingContextImpl context;

	public BindingNodeBinder(ProcessingContextImpl context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public <U> U bind(BindingNode<U, ?> node) {
		/*
		 * We need to replace the current binding node here as it may have been
		 * overridden in the case that this node is extensible.
		 */
		ProcessingContextImpl context = this.context.withReplacementBindingNode(node);

		TypedObject<?> binding;
		List<ChildNode<?>> children = node.children();

		InputBindingStrategy strategy = node.inputBindingStrategy();
		if (strategy == null)
			strategy = InputBindingStrategy.PROVIDED;

		TypeToken<?> bindingType = node.inputBindingType() != null ? node.inputBindingType() : node.dataType();

		switch (strategy) {
		case PROVIDED:
			binding = this.context.provide(bindingType);

			break;
		case CONSTRUCTOR:
			if (children.isEmpty())
				throw new ProcessingException(t -> t.mustHaveChildren(node.name(), InputBindingStrategy.CONSTRUCTOR), context);

			ChildNode<?> firstChild;
			List<ChildNodeBinding<?, ?>> parameters;
			do {
				firstChild = children.get(0);
				children = children.subList(1, children.size());

				parameters = getSingleBindingSequence(firstChild, context);
			} while (parameters == null);

			if (isBindingNode(firstChild)) {
				firstChild = parameters.get(0).getNode();
			}
			Executable inputMethod = getInputMethod(firstChild);

			try {
				Object[] parameterArray = parameters.stream().map(ChildNodeBinding::getData).toArray();
				binding = TypedObject.castInto(bindingType, ((Constructor<?>) inputMethod).newInstance(parameterArray));
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
				List<ChildNodeBinding<?, ?>> parameterList = parameters;
				throw new ProcessingException(t -> t.cannotInvoke(inputMethod, bindingType, node, parameterList), context, e);
			}
			break;
		case STATIC_FACTORY:
			if (children.isEmpty())
				throw new ProcessingException(t -> t.mustHaveChildren(node.name(), InputBindingStrategy.STATIC_FACTORY),
						context);

			do {
				firstChild = children.get(0);
				children = children.subList(1, children.size());

				parameters = getSingleBindingSequence(firstChild, context);
			} while (parameters == null);

			if (isBindingNode(firstChild)) {
				firstChild = parameters.get(0).getNode();
			}
			inputMethod = getInputMethod(firstChild);

			try {
				Object[] parameterArray = parameters.stream().map(ChildNodeBinding::getData).toArray();
				binding = TypedObject.castInto(bindingType, ((Method) inputMethod).invoke(null, parameterArray));
			} catch (IllegalAccessException | ModabiException | InvocationTargetException | SecurityException e) {
				List<ChildNodeBinding<?, ?>> parameterList = parameters;
				throw new ProcessingException(t -> t.cannotInvoke(inputMethod, bindingType, node, parameterList), context, e);
			}
			break;
		case IMPLEMENT_IN_PLACE:
			/*
			 * TODO some proxy magic with simple bean-like semantics. Remember, this
			 * may be more complex if we want proper *generic* type safety!
			 */
			Set<? extends Class<?>> classes = node.dataType().getRawTypes();

			binding = new TypedObject<>(node.dataType(), (U) Proxy.newProxyInstance(getClass().getClassLoader(),
					classes.toArray(new Class<?>[classes.size()]), new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							return null;
						}
					}));

			break;
		case SOURCE_ADAPTOR:
			binding = getSingleBinding(children.get(0), context).getTypedData();
			children = children.subList(1, children.size());
			break;
		case TARGET_ADAPTOR:
			binding = context.getBindingObject();
			break;
		default:
			throw new AssertionError();
		}

		context = context.withBindingObject(binding);

		for (ChildNode<?> child : children) {
			context = ChildNodeBinder.bind(context, child);
			binding = context.getBindingObject();
		}

		return (U) binding.getObject();
	}

	private static boolean isBindingNode(ChildNode<?> child) {
		return child.process(new ReturningNodeProcessor<Boolean>() {
			@Override
			public <U> Boolean accept(ComplexNode<U> node) {
				return true;
			}

			@Override
			public <U> Boolean accept(SimpleNode<U> node) {
				return true;
			}

			@Override
			public Boolean acceptDefault(SchemaNode<?> node) {
				return false;
			}
		});
	}

	private static Executable getInputMethod(ChildNode<?> node) {
		IdentityProperty<Executable> result = new IdentityProperty<>();
		node.process(new NodeProcessor() {
			@Override
			public <U> void accept(ComplexNode<U> node) {
				result.set(node.inputExecutable().getMember());
			}

			@Override
			public <U> void accept(SimpleNode<U> node) {
				result.set(node.inputExecutable().getMember());
			}

			@Override
			public void accept(InputSequenceNode node) {
				result.set(node.inputExecutable().getMember());
			}
		});
		return result.get();
	}

	public static List<ChildNodeBinding<?, ?>> getSingleBindingSequence(ChildNode<?> node,
			ProcessingContextImpl context) {
		List<ChildNodeBinding<?, ?>> parameters = new ArrayList<>();
		node.process(new NodeProcessor() {
			@Override
			public void accept(InputSequenceNode node) {
				for (ChildNode<?> child : node.children())
					parameters.add(getSingleBinding(child, context));
			}

			@Override
			public <U> void accept(ComplexNode<U> node) {
				parameters.add(getSingleBinding(node, context));
			}

			@Override
			public <U> void accept(SimpleNode<U> node) {
				parameters.add(getSingleBinding(node, context));
			}
		});

		return parameters;
	}

	public static ChildNodeBinding<?, ?> getSingleBinding(ChildNode<?> node, ProcessingContextImpl context) {
		IdentityProperty<ChildNodeBinding<?, ?>> result = new IdentityProperty<>();
		node.process(new NodeProcessor() {
			@Override
			public <U> void accept(ComplexNode<U> node) {
				List<ChildNodeBinding<? extends U, ?>> results = new ComplexNodeBinder<>(context, node).getBinding();

				check(node, results);
			}

			@Override
			public <U> void accept(SimpleNode<U> node) {
				List<ChildNodeBinding<? extends U, ?>> results = new DataNodeBinder<>(context, node).getBinding();

				check(node, results);
			}

			private <U> void check(BindingChildNode<U, ?> node, List<ChildNodeBinding<? extends U, ?>> results) {
				if (!results.isEmpty()) {
					result.set(results.get(0));
				} else if (!node.occurrences().contains(0)) {
					throw new ProcessingException(t -> t.mustHaveData(node.name()), context);
				} else {
					result.set(new ChildNodeBinding<>(null, node));
				}
			}
		});
		return result.get();
	}
}

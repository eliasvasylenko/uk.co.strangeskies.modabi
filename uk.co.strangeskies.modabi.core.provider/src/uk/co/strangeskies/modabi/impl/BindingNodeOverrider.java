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
package uk.co.strangeskies.modabi.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaProcessor;
import uk.co.strangeskies.modabi.impl.schema.utilities.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.DataNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.ModelWrapper;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.modabi.schema.building.DataTypeBuilder;
import uk.co.strangeskies.modabi.schema.building.ModelBuilder;

public class BindingNodeOverrider {
	public <T> ComplexNode.Effective<T> override(ModelBuilder builder,
			ComplexNode.Effective<? super T> node, Model.Effective<T> override) {
		try {
			if (isDirectOverridePossible(node, override))
				return new ComplexNodeWrapper<>(override, node);
			else
				return new OverridingProcessor().process(builder, node, override);
		} catch (Exception e) {
			throw new SchemaException("Cannot override complex node '" + node
					+ "' with model '" + override + "'", e);
		}
	}

	private <T> boolean isDirectOverridePossible(
			ComplexNode.Effective<? super T> node, Model.Effective<T> override) {
		return node.children().isEmpty(); // TODO is this enough?
	}

	public <T> DataNode.Effective<T> override(DataTypeBuilder builder,
			DataNode.Effective<? super T> node, DataType.Effective<T> override) {
		try {
			if (isDirectOverridePossible(node, override))
				return new DataNodeWrapper<>(override, node);
			else
				return new OverridingProcessor().process(builder, node, override);
		} catch (Exception e) {
			throw new SchemaException("Cannot override data node '" + node
					+ "' with data binding type '" + override + "'", e);
		}
	}

	private <T> boolean isDirectOverridePossible(
			DataNode.Effective<? super T> node, DataType.Effective<T> override) {
		return node.children().isEmpty(); // TODO is this enough?
	}

	private class OverridingProcessor implements SchemaProcessor {
		private final Deque<SchemaNodeConfigurator<?, ?>> configuratorStack;
		private List<?> currentProvidedValue;

		public OverridingProcessor() {
			configuratorStack = new ArrayDeque<>();
			currentProvidedValue = null;
		}

		@SuppressWarnings("unchecked")
		public <T> ComplexNode.Effective<T> process(ModelBuilder builder,
				ComplexNode.Effective<? super T> node, Model.Effective<T> override) {
			DataLoader loader = new DataLoader() {
				@Override
				public <V> List<V> loadData(DataNode<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};

			ModelConfigurator<Object> configurator = builder.configure(loader)
					.name(new QualifiedName("base")).isAbstract(true);
			if (node.getPreInputType() != null)
				configurator = configurator.bindingType(node.getPreInputType());
			if (node.getOutMethod().getDeclaringClass() != null)
				configurator = configurator
						.unbindingType(node.getOutMethod().getDeclaringClass());

			ComplexNodeConfigurator<T> elementConfigurator;

			List<Model<? super T>> models = new ArrayList<>(
					override.source().baseModel());
			models.add(0, new ModelWrapper<>(node));
			elementConfigurator = configurator.addChild().complex()
					.outMethodCast(true).baseModel(models);
			elementConfigurator = processBindingNode(override, elementConfigurator);
			elementConfigurator = processBindingChildNode(node, elementConfigurator);

			doChildren(override.children(),
					elementConfigurator.name(override.getName()));

			return (ComplexNode.Effective<T>) configurator.create().children().get(0)
					.effective();
		}

		@SuppressWarnings("unchecked")
		public <T> DataNode.Effective<T> process(DataTypeBuilder builder,
				DataNode.Effective<? super T> node, DataType.Effective<T> override) {
			DataLoader loader = new DataLoader() {
				@Override
				public <V> List<V> loadData(DataNode<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};

			DataTypeConfigurator<Object> configurator = builder.configure(loader)
					.name(new QualifiedName("base")).bindingType(node.getPreInputType())
					.unbindingType(node.getOutMethod().getDeclaringClass())
					.isAbstract(true);

			DataNodeConfigurator<T> dataNodeConfigurator = (DataNodeConfigurator<T>) configurator
					.addChild().data().name(override.getName())
					.type(override.source().baseType())
					.nullIfOmitted(node.nullIfOmitted());

			dataNodeConfigurator = tryProperty(node.format(),
					DataNodeConfigurator::format, dataNodeConfigurator);

			dataNodeConfigurator = processBindingNode(override, dataNodeConfigurator);
			dataNodeConfigurator = processBindingChildNode(node,
					dataNodeConfigurator);

			doChildren(override.children(), dataNodeConfigurator);

			return (DataNode.Effective<T>) configurator.create().children().get(0)
					.effective();
		}

		private <C extends SchemaNodeConfigurator<?, ?>> C next(
				Function<ChildBuilder, C> next) {
			return next.apply(configuratorStack.peek().addChild());
		}

		private <N extends SchemaNode<N, ?>> N doChildren(
				List<? extends ChildNode<?, ?>> children,
				SchemaNodeConfigurator<?, ? extends N> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode<?, ?> child : children)
				try {
					child.effective().process(this);
				} catch (Exception e) {
					throw new SchemaException("Cannot override child '" + child + "'", e);
				}

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends ChildNode<N, ?>, C extends ChildNodeConfigurator<C, N>> N processChildNode(
				N node, C c) {
			c = tryProperty(node.isAbstract(), C::isAbstract, c);
			c = tryProperty(node.occurrences(), C::occurrences, c);
			c = tryProperty(node.isOrdered(), C::ordered, c);

			return doChildren(node.children(), c.name(node.getName()));
		}

		@SuppressWarnings("unchecked")
		public <U, C extends BindingNodeConfigurator<C, ?, U>> C processBindingNode(
				BindingNode<U, ?, ?> node, C c) {
			c = tryProperty(node.effective().getDataType(),
					(cc, a) -> (C) cc.dataType(a), c);
			c = tryProperty(node.getBindingType(), C::bindingType, c);
			c = tryProperty(node.getBindingStrategy(), C::bindingStrategy, c);
			c = tryProperty(node.getUnbindingType(), C::unbindingType, c);
			c = tryProperty(node.getUnbindingFactoryType(), C::unbindingFactoryType,
					c);
			c = tryProperty(node.getUnbindingMethodName(), C::unbindingMethod, c);
			c = tryProperty(node.isUnbindingMethodUnchecked(),
					C::unbindingMethodUnchecked, c);
			c = tryProperty(node.getUnbindingStrategy(), C::unbindingStrategy, c);
			c = tryProperty(node.getProvidedUnbindingMethodParameterNames(),
					C::providedUnbindingMethodParameters, c);

			return c;
		}

		public <U, C extends BindingChildNodeConfigurator<C, ?, ? extends U>> C processBindingChildNode(
				BindingChildNode<U, ?, ?> node, C c) {
			c = tryProperty(node.getOutMethodName(), C::outMethod, c);
			c = tryProperty(node.isOutMethodIterable(), C::outMethodIterable, c);
			c = tryProperty(node.isOutMethodUnchecked(), C::outMethodUnchecked, c);
			c = tryProperty(node.isOutMethodCast(), C::outMethodCast, c);

			return processInputNode(node, c);
		}

		public <C extends InputNodeConfigurator<C, ?>> C processInputNode(
				InputNode<?, ?> node, C c) {
			c = tryProperty(node.isInMethodCast(), C::inMethodCast, c);
			c = tryProperty(node.isInMethodUnchecked(), C::inMethodUnchecked, c);
			c = tryProperty(node.getInMethodName(), C::inMethod, c);
			c = tryProperty(node.isInMethodChained(), C::inMethodChained, c);
			c = tryProperty(node.getPostInputType(), C::postInputType, c);

			return c;
		}

		@Override
		public <U> void accept(ComplexNode.Effective<U> node) {
			ComplexNode<U> source = node.source();

			/*
			 * TODO some magic here to shortcut when no children are being overridden,
			 * by folding the node as an overridden model rather than continuing to
			 * propagate through the tree of children manually.
			 */
			ComplexNodeConfigurator<U> c = next(ChildBuilder::complex)
					.extensible(node.isExtensible()).baseModel(node.source().baseModel());

			c = tryProperty(source.isInline(), ComplexNodeConfigurator::inline, c);

			processChildNode(source,
					processBindingNode(source, processBindingChildNode(source, c)));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(DataNode.Effective<U> node) {
			DataNode<U> source = node.source();

			DataNodeConfigurator<Object> c = next(ChildBuilder::data);

			c = tryProperty(source.format(), DataNodeConfigurator::format, c);
			c = tryProperty(source.providedValueBuffer(),
					DataNodeConfigurator::provideValue, c);
			c = tryProperty(source.valueResolution(),
					DataNodeConfigurator::valueResolution, c);
			c = tryProperty(source.isExtensible(), DataNodeConfigurator::extensible,
					c);

			currentProvidedValue = node.providedValues();

			DataNodeConfigurator<U> cu;
			if (source.type() == null) {
				cu = (DataNodeConfigurator<U>) c;
			} else {
				cu = c.type(source.type());
			}

			processChildNode(source,
					processBindingNode(source, processBindingChildNode(source, cu)));
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			InputSequenceNode source = node.source();

			processChildNode(source,
					processInputNode(source, next(ChildBuilder::inputSequence)));
		}

		@Override
		public void accept(SequenceNode.Effective node) {
			SequenceNode source = node.source();

			processChildNode(source, next(ChildBuilder::sequence));
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
			ChoiceNode source = node.source();

			processChildNode(source, next(ChildBuilder::choice));
		}

		private <U, C extends SchemaNodeConfigurator<?, ?>> C tryProperty(
				U property, BiFunction<C, U, C> consumer, C c) {
			if (property != null)
				return consumer.apply(c, property);
			else
				return c;
		}
	}
}

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
package uk.co.strangeskies.modabi.schema.management.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataBindingTypeBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataBindingTypeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SchemaNodeConfigurator;

public class BindingNodeOverrider {
	public <T> ComplexNode.Effective<T> override(ModelBuilder builder,
			ComplexNode.Effective<? super T> node, Model.Effective<T> override) {
		try {
			return new OverridingProcessor().process(builder, node, override);
		} catch (Exception e) {
			throw new SchemaException("Cannot override complex node '" + node
					+ "' with model '" + override + "'.", e);
		}
	}

	public <T> DataNode.Effective<T> override(DataBindingTypeBuilder builder,
			DataNode.Effective<? super T> node, DataBindingType.Effective<T> override) {
		try {
			return new OverridingProcessor().process(builder, node, override);
		} catch (Exception e) {
			throw new SchemaException("Cannot override data node '" + node
					+ "' with data binding type '" + override + "'.", e);
		}
	}

	private class OverridingProcessor implements SchemaProcessingContext {
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
					.name(new QualifiedName("base")).bindingType(node.getPreInputType())
					.unbindingType(node.getOutMethod().getDeclaringClass())
					.isAbstract(true);

			ComplexNodeConfigurator<T> elementConfigurator;

			elementConfigurator = processAbstractComplexNode(override, configurator
					.addChild().complex());
			elementConfigurator = processBindingNode(override, elementConfigurator);
			elementConfigurator = processBindingChildNode(node, elementConfigurator);

			doChildren(override.children(),
					elementConfigurator.name(override.getName()));

			return (ComplexNode.Effective<T>) configurator.create().children().get(0)
					.effective();
		}

		@SuppressWarnings("unchecked")
		public <T> DataNode.Effective<T> process(DataBindingTypeBuilder builder,
				DataNode.Effective<? super T> node,
				DataBindingType.Effective<T> override) {
			DataLoader loader = new DataLoader() {
				@Override
				public <V> List<V> loadData(DataNode<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};

			DataBindingTypeConfigurator<Object> configurator = builder
					.configure(loader).name(new QualifiedName("base"))
					.bindingType(node.getPreInputType())
					.unbindingType(node.getOutMethod().getDeclaringClass())
					.isAbstract(true);

			DataNodeConfigurator<T> dataNodeConfigurator = configurator.addChild()
					.data().name(override.getName()).dataType(override.getDataType())
					.type(override.source().baseType());

			dataNodeConfigurator = tryProperty(node.optional(),
					dataNodeConfigurator::optional, dataNodeConfigurator);
			dataNodeConfigurator = tryProperty(node.format(),
					dataNodeConfigurator::format, dataNodeConfigurator);

			dataNodeConfigurator = processBindingNode(override, dataNodeConfigurator);
			dataNodeConfigurator = processBindingChildNode(node, dataNodeConfigurator);

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
					throw new SchemaException("Cannot override child '" + child + "'.", e);
				}

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends SchemaNode<N, ?>> N doChildren(N node,
				SchemaNodeConfigurator<?, N> c) {
			if (node.isAbstract() != null)
				c = c.isAbstract(node.isAbstract());

			return doChildren(node.children(), c.name(node.getName()));
		}

		public <U, C extends BindingNodeConfigurator<C, ?, U>> C processBindingNode(
				BindingNode<U, ?, ?> node, C c) {
			c = tryProperty(node.getBindingType(), c::bindingType, c);
			c = tryProperty(node.getBindingStrategy(), c::bindingStrategy, c);
			c = tryProperty(node.getUnbindingType(), c::unbindingType, c);
			c = tryProperty(node.getUnbindingFactoryType(), c::unbindingFactoryType,
					c);
			c = tryProperty(node.getUnbindingMethodName(), c::unbindingMethod, c);
			c = tryProperty(node.isUnbindingMethodUnchecked(),
					c::unbindingMethodUnchecked, c);
			c = tryProperty(node.getUnbindingStrategy(), c::unbindingStrategy, c);
			c = tryProperty(node.getProvidedUnbindingMethodParameterNames(),
					c::providedUnbindingMethodParameters, c);

			return c;
		}

		public <U, C extends BindingChildNodeConfigurator<C, ?, ? extends U>> C processBindingChildNode(
				BindingChildNode<U, ?, ?> node, C c) {
			c = tryProperty(node.getOutMethodName(), c::outMethod, c);
			c = tryProperty(node.isOutMethodIterable(), c::outMethodIterable, c);
			c = tryProperty(node.isOutMethodUnchecked(), c::outMethodUnchecked, c);
			c = tryProperty(node.occurrences(), c::occurrences, c);
			c = tryProperty(node.isOrdered(), c::ordered, c);

			return processInputNode(node, c);
		}

		public <U, C extends InputNodeConfigurator<C, ?>> C processInputNode(
				InputNode<?, ?> node, C c) {
			c = tryProperty(node.isInMethodCast(), c::inMethodCast, c);
			c = tryProperty(node.isInMethodUnchecked(), c::inMethodUnchecked, c);
			c = tryProperty(node.getInMethodName(), c::inMethod, c);
			c = tryProperty(node.isInMethodChained(), c::inMethodChained, c);
			c = tryProperty(node.getPostInputType(), c::postInputType, c);

			return c;
		}

		public <U> ComplexNodeConfigurator<U> processAbstractComplexNode(
				AbstractComplexNode<U, ?, ?> node, ComplexNodeConfigurator<Object> c) {
			ComplexNodeConfigurator<U> cu = c.dataType(node.getDataType()).baseModel(
					node.source().baseModel());

			return cu;
		}

		@Override
		public <U> void accept(ComplexNode.Effective<U> node) {
			ComplexNode<U> source = node.source();

			ComplexNodeConfigurator<U> c = processAbstractComplexNode(source,
					next(ChildBuilder::complex).extensible(node.isExtensible()));

			c = tryProperty(source.isInline(), c::inline, c);

			doChildren(source,
					processBindingNode(source, processBindingChildNode(source, c)));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(DataNode.Effective<U> node) {
			DataNode<U> source = node.source();

			DataNodeConfigurator<Object> c = next(ChildBuilder::data);

			c = tryProperty(source.format(), c::format, c);
			c = tryProperty(source.providedValueBuffer(), c::provideValue, c);
			c = tryProperty(source.valueResolution(), c::valueResolution, c);
			c = tryProperty(source.isExtensible(), c::extensible, c);
			c = tryProperty(source.optional(), c::optional, c);

			currentProvidedValue = node.providedValues();

			DataNodeConfigurator<U> cu;
			if (source.type() == null) {
				if (source.getDataType() == null) {
					cu = (DataNodeConfigurator<U>) c;
				} else {
					cu = c.dataType(source.getDataType());
				}
			} else {
				DataNodeConfigurator<U> finalCu = c.type(source.type());
				cu = tryProperty(source.getDataType(), p -> finalCu.dataType(p),
						finalCu);
			}

			doChildren(source,
					processBindingNode(source, processBindingChildNode(source, cu)));
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			InputSequenceNode source = node.source();

			doChildren(source,
					processInputNode(source, next(ChildBuilder::inputSequence)));
		}

		@Override
		public void accept(SequenceNode.Effective node) {
			SequenceNode source = node.source();

			doChildren(source, next(ChildBuilder::sequence));
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
			ChoiceNode source = node.source();

			ChoiceNodeConfigurator c = next(ChildBuilder::choice);

			doChildren(source,
					tryProperty(source.isMandatory(), p -> c.mandatory(p), c));
		}

		private <U, C extends SchemaNodeConfigurator<? extends C, ?>> C tryProperty(
				U property, Function<U, C> consumer, C c) {
			if (property != null)
				return consumer.apply(property);
			else
				return c;
		}
	}
}

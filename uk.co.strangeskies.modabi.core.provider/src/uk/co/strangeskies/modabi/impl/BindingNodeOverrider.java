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

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.declarative.InputBindingStrategy;
import uk.co.strangeskies.modabi.impl.schema.utilities.BindingChildNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ComplexNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class BindingNodeOverrider {
	private final SchemaBuilder builder;

	public BindingNodeOverrider(SchemaBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Override an extensible complex node in some node tree with a given model.
	 * Each {@link ComplexNode model node} which forms a part of the complex
	 * node's effective {@link ComplexNode.Effective#model() base model} must also
	 * appear in the given model's {@link ComplexNode.Effective#baseModel() base
	 * model}.
	 *
	 * @param builder
	 *          A schema builder for constructing the effective overriding node
	 * @param node
	 *          the complex node we wish to override
	 * @param override
	 *          the overriding model
	 * @return a node which overrides the given node by merging it with a model
	 *         which extends all components of the nodes model
	 */
	public SchemaNode override(SchemaNode node, Model<?> override) {
		SchemaNode wrappedNode = ComplexNodeWrapper.wrapNodeWithOverrideType(node, override);

		if (!isDirectOverridePossible(node, override)) {
			wrappedNode = new OverridingProcessor().process(builder, wrappedNode);
		}

		return wrappedNode;
	}

	private <T extends SchemaNode<?>> boolean isDirectOverridePossible(ChildBindingPoint<?, T> node,
			BindingPoint<?, T> override) {
		return node.node().childBindingPoints().isEmpty() && (node.concrete() || override.concrete());
	}

	private class OverridingProcessor implements NodeProcessor {
		private final Deque<SchemaNodeConfigurator<?, ?>> configuratorStack;
		private List<?> currentProvidedValue;

		public OverridingProcessor() {
			configuratorStack = new ArrayDeque<>();
			currentProvidedValue = null;
		}

		private DataLoader getDataLoader() {
			return new DataLoader() {
				@SuppressWarnings("unchecked")
				@Override
				public <V> List<V> loadData(DataType<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};
		}

		/*
		 * Configure the parent, or "base", node for the overriding node we are
		 * building.
		 */
		private <C extends BindingNodeConfigurator<C, ?, Object>> C configureParentNode(C configurator,
				BindingChildNodeWrapper<?, ?, ?> node) {
			configurator = configurator.name(new QualifiedName("base")).concrete(false);

			TypeToken<?> parentUnbindingType = null;
			TypeToken<?> parentBindingType = null;
			InputBindingStrategy parentBindingStrategy = null;

			SchemaNode parent = node.parent();

			parentUnbindingType = parent.outputBindingType();

			if (node.getBase() == parent.children().get(0)) {
				parentBindingStrategy = parent.inputBindingStrategy();

				if (parent.inputBindingType() != null) {
					parentBindingType = parent.inputBindingType();
				} else {
					parentBindingType = parent.dataType();
				}
			}

			if (parentBindingType != null) {
				configurator = configurator.inputBindingType(parentBindingType);
			} else if (node.preInputType() != null) {
				configurator = configurator.inputBindingType(node.preInputType());
			}

			if (parentBindingStrategy != null) {
				configurator = configurator.inputBindingStrategy(parentBindingStrategy);
			}

			if (node.outputMethod().getDeclaringType() != null) {
				configurator = configurator.outputBindingType(node.outputMethod().getDeclaringType());
			} else {
				configurator = configurator.outputBindingType(parentUnbindingType);
			}

			return configurator;
		}

		@SuppressWarnings("unchecked")
		public <T> ComplexNode<T> process(SchemaBuilder builder, ComplexNodeWrapper<T> node) {
			ComplexNode<? super T> override = node.model().get(0);

			ModelConfigurator<Object> configurator = configureParentNode(builder.configure(getDataLoader()).addModel(), node);

			List<ComplexNode<? super T>> models = new ArrayList<>(override.baseModel());

			ComplexNodeConfigurator<T> elementConfigurator = configurator
					.addChildBindingPoint()
					.complex()
					.name(override.name())
					.castOutput(true)
					.model(models);

			elementConfigurator = processBindingNode(node, elementConfigurator);
			elementConfigurator = processBindingChildNode(node, elementConfigurator);

			doChildren(override.children(), elementConfigurator);

			return (ComplexNode<T>) configurator.create().children().get(0);
		}

		private <C extends SchemaNodeConfigurator<?, ?>> C next(Function<ChildBuilder, C> next) {
			return next.apply(configuratorStack.peek().addChildBindingPoint());
		}

		private <N extends SchemaNode<? extends N>> N doChildren(List<? extends ChildNode<?>> children,
				SchemaNodeConfigurator<?, ? extends N> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode<?> child : children) {
				child.process(this);
			}

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends ChildNode<? extends N>, C extends ChildNodeConfigurator<C, ? extends N>> N processChildNode(
				N node, C c) {
			c = tryProperty(node, ChildNode::concrete, C::concrete, c);
			c = tryProperty(node, ChildNode::occurrences, C::occurrences, c);
			c = tryProperty(node, ChildNode::orderedOccurrences, C::orderedOccurrences, c);

			return doChildren(node.children(), c.name(node.name()));
		}

		@SuppressWarnings("unchecked")
		public <U, C extends BindingNodeConfigurator<C, ?, U>> C processBindingNode(BindingNode<U, ?> node, C c) {
			c = tryProperty(node, n -> n.dataType(), (cc, a) -> (C) cc.dataType(a), c);
			c = tryProperty(node, BindingNode::inputBindingType, (cc, t) -> cc.inputBindingType(t), c);
			c = tryProperty(node, BindingNode::inputBindingStrategy, C::inputBindingStrategy, c);
			c = tryProperty(node, BindingNode::outputBindingType, (cc, t) -> cc.outputBindingType(t), c);
			c = tryProperty(node, BindingNode::outputBindingFactoryType, (cc, t) -> cc.outputBindingFactoryType(t), c);
			c = tryProperty(node, b -> b.outputBindingMethod().getName(), C::outputBindingMethod, c);
			c = tryProperty(node, BindingNode::outputBindingMethodUnchecked, C::outputBindingMethodUnchecked, c);
			c = tryProperty(node, BindingNode::outputBindingStrategy, C::outputBindingStrategy, c);
			c = tryProperty(node,
					b -> b.providedOutputBindingMethodParameters().stream().map(BindingNode::name).collect(toList()),
					(cc, m) -> cc.providedOutputBindingMethodParameters(m), c);

			return c;
		}

		public <U, C extends BindingChildNodeConfigurator<C, ?, ? extends U>> C processBindingChildNode(
				BindingChildNode<U, ?> node, C c) {
			c = tryProperty(node, b -> b.outputMethod().getName(), C::outputMethod, c);
			c = tryProperty(node, BindingChildNode::nullIfOmitted, BindingChildNodeConfigurator::nullIfOmitted, c);
			c = tryProperty(node, BindingChildNode::iterableOutput, C::iterableOutput, c);
			c = tryProperty(node, BindingChildNode::uncheckedOutput, C::uncheckedOutput, c);
			c = tryProperty(node, BindingChildNode::castOutput, C::castOutput, c);

			return processInputNode(node, c);
		}

		public <C extends InputNodeConfigurator<C, ?>> C processInputNode(InputNode<?> node, C c) {
			c = tryProperty(node, InputNode::castInput, C::castInput, c);
			c = tryProperty(node, InputNode::uncheckedInput, C::uncheckedInput, c);
			c = tryProperty(node, b -> b.inputExecutable().getName(), C::inputMethod, c);
			c = tryProperty(node, InputNode::chainedInput, C::chainedInput, c);
			c = tryProperty(node, InputNode::postInputType, (cc, t) -> cc.postInputType(t), c);

			return c;
		}

		@Override
		public <U> void accept(ComplexNode<U> node) {
			/*
			 * TODO some magic here to shortcut when no children are being overridden,
			 * by folding the node as an overridden model rather than continuing to
			 * propagate through the tree of children manually.
			 */
			ComplexNodeConfigurator<U> c = next(ChildBuilder::complex).extensible(node.extensible()).model(node.model());

			c = tryProperty(node, ComplexNode::inline, ComplexNodeConfigurator::inline, c);

			processChildNode(node, processBindingNode(node, processBindingChildNode(node, c)));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(SimpleNode<U> node) {
			DataNodeConfigurator<Object> c = next(ChildBuilder::data);

			c = tryProperty(node, SimpleNode::format, DataNodeConfigurator::format, c);
			c = tryProperty(node, SimpleNode::providedValueBuffer, DataNodeConfigurator::provideValue, c);
			c = tryProperty(node, SimpleNode::valueResolution, DataNodeConfigurator::valueResolution, c);
			c = tryProperty(node, SimpleNode::extensible, DataNodeConfigurator::extensible, c);

			currentProvidedValue = node.providedValues();

			DataNodeConfigurator<U> cu;
			if (node.type() == null) {
				cu = (DataNodeConfigurator<U>) c;
			} else {
				cu = (DataNodeConfigurator<U>) c.type(node.type());
			}

			processChildNode(node, processBindingNode(node, processBindingChildNode(node, cu)));
		}

		@Override
		public void accept(InputSequenceNode node) {
			processChildNode(node, processInputNode(node, next(ChildBuilder::inputSequence)));
		}

		@Override
		public void accept(SequenceNode node) {
			processChildNode(node, next(ChildBuilder::sequence));
		}

		@Override
		public void accept(ChoiceNode node) {
			processChildNode(node, next(ChildBuilder::choice));
		}

		private <N extends SchemaNode<?>, U, C extends SchemaNodeConfigurator<?, ?>> C tryProperty(N node,
				Function<N, U> property, BiFunction<C, U, C> consumer, C c) {
			U value = property.apply(node);

			if (value != null) {
				try {
					return consumer.apply(c, value);
				} catch (Exception e) {
					@SuppressWarnings("unchecked")
					Class<N> nodeClass = (Class<N>) node.getThisType().getRawType();
					throw new ModabiException(
							t -> t.cannotOverrideIncompatibleProperty(node, property::apply, nodeClass, configuratorStack, value), e);
				}
			} else {
				return c;
			}
		}
	}
}

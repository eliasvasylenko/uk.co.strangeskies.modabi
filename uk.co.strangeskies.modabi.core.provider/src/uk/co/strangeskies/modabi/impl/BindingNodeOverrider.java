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

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.impl.schema.utilities.BindingChildNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.ComplexNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.DataNodeWrapper;
import uk.co.strangeskies.modabi.impl.schema.utilities.ModelWrapper;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
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
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.IdentityProperty;

public class BindingNodeOverrider {
	private final SchemaBuilder builder;

	public BindingNodeOverrider(SchemaBuilder builder) {
		this.builder = builder;
	}

	/**
	 * Override an extensible complex node in some node tree with a given model.
	 * Each {@link Model model node} which forms a part of the complex node's
	 * effective {@link ComplexNode.Effective#model() base model} must also appear
	 * in the given model's {@link Model.Effective#baseModel() base model}.
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
	public <T> ComplexNode.Effective<? extends T> override(ComplexNode.Effective<T> node, Model.Effective<?> override) {
		if (isDirectOverridePossible(node, override)) {
			return ComplexNodeWrapper.wrapNodeWithOverrideType(node, override);
		} else {
			return new OverridingProcessor().process(builder, ComplexNodeWrapper.wrapNodeWithOverrideType(node, override));
		}
	}

	private <T> boolean isDirectOverridePossible(ComplexNode.Effective<? super T> node, Model.Effective<?> override) {
		return node.children().isEmpty() && (node.abstractness().isAtMost(Abstractness.UNINFERRED)
				|| override.abstractness().isAtMost(Abstractness.UNINFERRED));
	}

	/**
	 * Override an extensible data node in some node tree with a given data type.
	 * The {@link DataType data type} of the data nodes {@link DataNode#type()
	 * base model} must also appear in the given data type's
	 * {@link DataType.Effective#base() base type}.
	 *
	 * @param builder
	 *          A schema builder for constructing the effective overriding node
	 * @param node
	 *          the complex node we wish to override
	 * @param override
	 *          the overriding data type
	 * @return a node which overrides the given node by merging it with a data
	 *         type which extends its type
	 */
	public <T> DataNode.Effective<? extends T> override(DataNode.Effective<T> node, DataType.Effective<?> override) {
		if (isDirectOverridePossible(node, override))
			return DataNodeWrapper.wrapNodeWithOverrideType(node, override);
		else
			return new OverridingProcessor().process(builder, DataNodeWrapper.wrapNodeWithOverrideType(node, override));
	}

	private <T> boolean isDirectOverridePossible(DataNode.Effective<? super T> node, DataType.Effective<?> override) {
		return node.children().isEmpty() && (node.abstractness().isAtMost(Abstractness.UNINFERRED)
				|| override.abstractness().isAtMost(Abstractness.UNINFERRED));
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
				public <V> List<V> loadData(DataNode<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};
		}

		/*
		 * Configure the parent, or "base", node for the overriding node we are
		 * building.
		 */
		private <C extends BindingNodeConfigurator<C, ?, Object>> C configureParentNode(C configurator,
				BindingChildNodeWrapper<?, ?, ?, ?> node) {
			configurator = configurator.name(new QualifiedName("base")).abstractness(Abstractness.ABSTRACT);

			IdentityProperty<TypeToken<?>> parentUnbindingType = new IdentityProperty<>();
			IdentityProperty<TypeToken<?>> parentBindingType = new IdentityProperty<>();
			IdentityProperty<InputBindingStrategy> parentBindingStrategy = new IdentityProperty<>();
			SchemaNode.Effective<?, ?> parent = node.parent();
			parent.process(new NodeProcessor() {
				@Override
				public <U> void accept(DataNode.Effective<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(ComplexNode.Effective<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(DataType.Effective<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(Model.Effective<U> node) {
					acceptParent(node);
				}

				@Override
				public void accept(ChoiceNode.Effective node) {
					node.parent().process(this);
				}

				@Override
				public void accept(InputSequenceNode.Effective node) {
					node.parent().process(this);
				}

				@Override
				public void accept(SequenceNode.Effective node) {
					node.parent().process(this);
				}

				private void acceptParent(BindingNode.Effective<?, ?, ?> parent) {
					parentUnbindingType.set(parent.unbindingType());

					if (node.getBase() == parent.children().get(0)) {
						parentBindingStrategy.set(parent.bindingStrategy());

						if (parent.bindingType() != null) {
							parentBindingType.set(parent.bindingType());
						} else {
							parentBindingType.set(parent.dataType());
						}
					}
				}
			});

			if (parentBindingType.get() != null) {
				configurator = configurator.bindingType(parentBindingType.get());
			} else if (node.preInputType() != null) {
				configurator = configurator.bindingType(node.preInputType());
			}

			if (parentBindingStrategy.get() != null) {
				configurator = configurator.bindingStrategy(parentBindingStrategy.get());
			}

			if (node.outMethod().getDeclaringType() != null) {
				configurator = configurator.unbindingType(node.outMethod().getDeclaringType());
			} else {
				configurator = configurator.unbindingType(parentUnbindingType.get());
			}

			return configurator;
		}

		@SuppressWarnings("unchecked")
		public <T> ComplexNode.Effective<T> process(SchemaBuilder builder, ComplexNodeWrapper<T> node) {
			Model.Effective<? super T> override = node.model().get(0);

			ModelConfigurator<Object> configurator = configureParentNode(builder.configure(getDataLoader()).addModel(), node);

			List<Model<? super T>> models = new ArrayList<>(override.source().baseModel());
			models.add(0, new ModelWrapper<>(node));

			ComplexNodeConfigurator<T> elementConfigurator = configurator.addChild().complex().name(override.name())
					.outMethodCast(true).model(models);

			elementConfigurator = processBindingNode(node, elementConfigurator);
			elementConfigurator = processBindingChildNode(node, elementConfigurator);

			doChildren(override.children(), elementConfigurator);

			return (ComplexNode.Effective<T>) configurator.create().children().get(0).effective();
		}

		@SuppressWarnings("unchecked")
		public <T> DataNode.Effective<T> process(SchemaBuilder builder, DataNodeWrapper<T> node) {
			DataType.Effective<? super T> override = node.type();

			DataTypeConfigurator<Object> configurator = configureParentNode(builder.configure(getDataLoader()).addDataType(),
					node);

			DataNodeConfigurator<T> dataNodeConfigurator = (DataNodeConfigurator<T>) configurator.addChild().data()
					.name(override.name()).outMethodCast(true).type(override.source().baseType())
					.nullIfOmitted(node.nullIfOmitted());

			dataNodeConfigurator = tryProperty(node, DataNode::format, DataNodeConfigurator::format, dataNodeConfigurator);

			dataNodeConfigurator = processBindingNode(node, dataNodeConfigurator);
			dataNodeConfigurator = processBindingChildNode(node, dataNodeConfigurator);

			doChildren(override.children(), dataNodeConfigurator);

			return (DataNode.Effective<T>) configurator.create().children().get(0).effective();
		}

		private <C extends SchemaNodeConfigurator<?, ?>> C next(Function<ChildBuilder, C> next) {
			return next.apply(configuratorStack.peek().addChild());
		}

		private <N extends SchemaNode<N, ?>> N doChildren(List<? extends ChildNode<?, ?>> children,
				SchemaNodeConfigurator<?, ? extends N> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode<?, ?> child : children) {
				child.effective().process(this);
			}

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends ChildNode<N, ?>, C extends ChildNodeConfigurator<C, N>> N processChildNode(N node, C c) {
			c = tryProperty(node, ChildNode::abstractness, C::abstractness, c);
			c = tryProperty(node, ChildNode::occurrences, C::occurrences, c);
			c = tryProperty(node, ChildNode::ordered, C::ordered, c);

			return doChildren(node.children(), c.name(node.name()));
		}

		@SuppressWarnings("unchecked")
		public <U, C extends BindingNodeConfigurator<C, ?, U>> C processBindingNode(BindingNode<U, ?, ?> node, C c) {
			c = tryProperty(node, n -> n.effective().dataType(), (cc, a) -> (C) cc.dataType(a), c);
			c = tryProperty(node, BindingNode::bindingType, (cc, t) -> cc.bindingType(t), c);
			c = tryProperty(node, BindingNode::bindingStrategy, C::bindingStrategy, c);
			c = tryProperty(node, BindingNode::unbindingType, (cc, t) -> cc.unbindingType(t), c);
			c = tryProperty(node, BindingNode::unbindingFactoryType, (cc, t) -> cc.unbindingFactoryType(t), c);
			c = tryProperty(node, BindingNode::unbindingMethodName, C::unbindingMethod, c);
			c = tryProperty(node, BindingNode::unbindingMethodUnchecked, C::unbindingMethodUnchecked, c);
			c = tryProperty(node, BindingNode::unbindingStrategy, C::unbindingStrategy, c);
			c = tryProperty(node, BindingNode::providedUnbindingMethodParameterNames,
					(cc, m) -> cc.providedUnbindingMethodParameters(m), c);

			return c;
		}

		public <U, C extends BindingChildNodeConfigurator<C, ?, ? extends U>> C processBindingChildNode(
				BindingChildNode<U, ?, ?> node, C c) {
			c = tryProperty(node, BindingChildNode::outMethodName, C::outMethod, c);
			c = tryProperty(node, BindingChildNode::outMethodIterable, C::outMethodIterable, c);
			c = tryProperty(node, BindingChildNode::outMethodUnchecked, C::outMethodUnchecked, c);
			c = tryProperty(node, BindingChildNode::outMethodCast, C::outMethodCast, c);

			return processInputNode(node, c);
		}

		public <C extends InputNodeConfigurator<C, ?>> C processInputNode(InputNode<?, ?> node, C c) {
			c = tryProperty(node, InputNode::inMethodCast, C::inMethodCast, c);
			c = tryProperty(node, InputNode::inMethodUnchecked, C::inMethodUnchecked, c);
			c = tryProperty(node, InputNode::inMethodName, C::inMethod, c);
			c = tryProperty(node, InputNode::inMethodChained, C::inMethodChained, c);
			c = tryProperty(node, InputNode::postInputType, (cc, t) -> cc.postInputType(t), c);

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
			ComplexNodeConfigurator<U> c = next(ChildBuilder::complex).extensible(node.extensible())
					.model(node.source().model());

			c = tryProperty(source, ComplexNode::inline, ComplexNodeConfigurator::inline, c);

			processChildNode(source, processBindingNode(source, processBindingChildNode(source, c)));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <U> void accept(DataNode.Effective<U> node) {
			DataNode<U> source = node.source();

			DataNodeConfigurator<Object> c = next(ChildBuilder::data);

			c = tryProperty(source, DataNode::format, DataNodeConfigurator::format, c);
			c = tryProperty(source, DataNode::providedValueBuffer, DataNodeConfigurator::provideValue, c);
			c = tryProperty(source, DataNode::valueResolution, DataNodeConfigurator::valueResolution, c);
			c = tryProperty(source, DataNode::extensible, DataNodeConfigurator::extensible, c);

			currentProvidedValue = node.providedValues();

			DataNodeConfigurator<U> cu;
			if (source.type() == null) {
				cu = (DataNodeConfigurator<U>) c;
			} else {
				cu = (DataNodeConfigurator<U>) c.type(source.type());
			}

			processChildNode(source, processBindingNode(source, processBindingChildNode(source, cu)));
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			InputSequenceNode source = node.source();

			processChildNode(source, processInputNode(source, next(ChildBuilder::inputSequence)));
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

		private <N extends SchemaNode<?, ?>, U, C extends SchemaNodeConfigurator<?, ?>> C tryProperty(N node,
				Function<N, U> property, BiFunction<C, U, C> consumer, C c) {
			U value = property.apply(node);

			if (value != null) {
				try {
					return consumer.apply(c, value);
				} catch (Exception e) {
					throw new ModabiException(
							t -> t.cannotOverrideIncompatibleProperty(property::apply, node, configuratorStack, value), e);
				}
			} else {
				return c;
			}
		}
	}
}

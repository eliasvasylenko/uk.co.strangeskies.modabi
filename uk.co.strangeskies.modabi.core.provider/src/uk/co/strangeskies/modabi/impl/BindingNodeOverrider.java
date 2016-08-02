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

import static java.util.stream.Collectors.toList;

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
	public <T> ComplexNode<? extends T> override(ComplexNode<T> node, Model<?> override) {
		if (isDirectOverridePossible(node, override)) {
			return ComplexNodeWrapper.wrapNodeWithOverrideType(node, override);
		} else {
			return new OverridingProcessor().process(builder, ComplexNodeWrapper.wrapNodeWithOverrideType(node, override));
		}
	}

	private <T> boolean isDirectOverridePossible(ComplexNode<? super T> node, Model<?> override) {
		return node.children().isEmpty() && (node.concrete() || override.concrete());
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
	public <T> DataNode<? extends T> override(DataNode<T> node, DataType<?> override) {
		if (isDirectOverridePossible(node, override))
			return DataNodeWrapper.wrapNodeWithOverrideType(node, override);
		else
			return new OverridingProcessor().process(builder, DataNodeWrapper.wrapNodeWithOverrideType(node, override));
	}

	private <T> boolean isDirectOverridePossible(DataNode<? super T> node, DataType<?> override) {
		return node.children().isEmpty() && (node.concrete() || override.concrete());
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
				BindingChildNodeWrapper<?, ?, ?> node) {
			configurator = configurator.name(new QualifiedName("base")).concrete(false);

			IdentityProperty<TypeToken<?>> parentUnbindingType = new IdentityProperty<>();
			IdentityProperty<TypeToken<?>> parentBindingType = new IdentityProperty<>();
			IdentityProperty<InputBindingStrategy> parentBindingStrategy = new IdentityProperty<>();
			SchemaNode<?> parent = node.parent();
			parent.process(new NodeProcessor() {
				@Override
				public <U> void accept(DataNode<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(ComplexNode<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(DataType<U> node) {
					acceptParent(node);
				}

				@Override
				public <U> void accept(Model<U> node) {
					acceptParent(node);
				}

				@Override
				public void accept(ChoiceNode node) {
					node.parent().process(this);
				}

				@Override
				public void accept(InputSequenceNode node) {
					node.parent().process(this);
				}

				@Override
				public void accept(SequenceNode node) {
					node.parent().process(this);
				}

				private void acceptParent(BindingNode<?, ?> parent) {
					parentUnbindingType.set(parent.outputBindingType());

					if (node.getBase() == parent.children().get(0)) {
						parentBindingStrategy.set(parent.inputBindingStrategy());

						if (parent.inputBindingType() != null) {
							parentBindingType.set(parent.inputBindingType());
						} else {
							parentBindingType.set(parent.dataType());
						}
					}
				}
			});

			if (parentBindingType.get() != null) {
				configurator = configurator.inputBindingType(parentBindingType.get());
			} else if (node.preInputType() != null) {
				configurator = configurator.inputBindingType(node.preInputType());
			}

			if (parentBindingStrategy.get() != null) {
				configurator = configurator.inputBindingStrategy(parentBindingStrategy.get());
			}

			if (node.outputMethod().getDeclaringType() != null) {
				configurator = configurator.outputBindingType(node.outputMethod().getDeclaringType());
			} else {
				configurator = configurator.outputBindingType(parentUnbindingType.get());
			}

			return configurator;
		}

		@SuppressWarnings("unchecked")
		public <T> ComplexNode<T> process(SchemaBuilder builder, ComplexNodeWrapper<T> node) {
			Model<? super T> override = node.model().get(0);

			ModelConfigurator<Object> configurator = configureParentNode(builder.configure(getDataLoader()).addModel(), node);

			List<Model<? super T>> models = new ArrayList<>(override.baseModel());
			models.add(0, new ModelWrapper<>(node));

			ComplexNodeConfigurator<T> elementConfigurator = configurator.addChild().complex().name(override.name())
					.castOutput(true).model(models);

			elementConfigurator = processBindingNode(node, elementConfigurator);
			elementConfigurator = processBindingChildNode(node, elementConfigurator);

			doChildren(override.children(), elementConfigurator);

			return (ComplexNode<T>) configurator.create().children().get(0);
		}

		@SuppressWarnings("unchecked")
		public <T> DataNode<T> process(SchemaBuilder builder, DataNodeWrapper<T> node) {
			DataType<? super T> override = node.type();

			DataTypeConfigurator<Object> configurator = configureParentNode(builder.configure(getDataLoader()).addDataType(),
					node);

			DataNodeConfigurator<T> dataNodeConfigurator = (DataNodeConfigurator<T>) configurator.addChild().data()
					.name(override.name()).castOutput(true).type(override.baseType()).nullIfOmitted(node.nullIfOmitted());

			dataNodeConfigurator = tryProperty(node, DataNode::format, DataNodeConfigurator::format, dataNodeConfigurator);

			dataNodeConfigurator = processBindingNode(node, dataNodeConfigurator);
			dataNodeConfigurator = processBindingChildNode(node, dataNodeConfigurator);

			doChildren(override.children(), dataNodeConfigurator);

			return (DataNode<T>) configurator.create().children().get(0);
		}

		private <C extends SchemaNodeConfigurator<?, ?>> C next(Function<ChildBuilder, C> next) {
			return next.apply(configuratorStack.peek().addChild());
		}

		private <N extends SchemaNode<N>> N doChildren(List<? extends ChildNode<?>> children,
				SchemaNodeConfigurator<?, ? extends N> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode<?> child : children) {
				child.process(this);
			}

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends ChildNode<N>, C extends ChildNodeConfigurator<C, N>> N processChildNode(N node, C c) {
			c = tryProperty(node, ChildNode::concrete, C::concrete, c);
			c = tryProperty(node, ChildNode::occurrences, C::occurrences, c);
			c = tryProperty(node, ChildNode::ordered, C::ordered, c);

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
		public <U> void accept(DataNode<U> node) {
			DataNodeConfigurator<Object> c = next(ChildBuilder::data);

			c = tryProperty(node, DataNode::format, DataNodeConfigurator::format, c);
			c = tryProperty(node, DataNode::providedValueBuffer, DataNodeConfigurator::provideValue, c);
			c = tryProperty(node, DataNode::valueResolution, DataNodeConfigurator::valueResolution, c);
			c = tryProperty(node, DataNode::extensible, DataNodeConfigurator::extensible, c);

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
							t -> t.cannotOverrideIncompatibleProperty(property::apply, nodeClass, configuratorStack, value), e);
				}
			} else {
				return c;
			}
		}
	}
}

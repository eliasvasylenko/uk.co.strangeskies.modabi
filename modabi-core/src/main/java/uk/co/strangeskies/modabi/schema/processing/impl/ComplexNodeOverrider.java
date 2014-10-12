package uk.co.strangeskies.modabi.schema.processing.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.BindingChildNode;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.ChoiceNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.ComplexNode;
import uk.co.strangeskies.modabi.schema.node.InputNode;
import uk.co.strangeskies.modabi.schema.node.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.SequenceNode;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.building.configuration.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.model.ModelBuilder;
import uk.co.strangeskies.modabi.schema.node.model.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.node.wrapping.impl.ModelWrapper;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ComplexNodeOverrider {
	private final ModelBuilder builder;

	public ComplexNodeOverrider(ModelBuilder builder) {
		this.builder = builder;
	}

	public <T> ComplexNode.Effective<T> override(
			ComplexNode.Effective<? super T> element, Model.Effective<T> override) {
		try {
			return new OverridingProcessor().process(element, override);
		} catch (SchemaException e) {
			throw e;
		} catch (Exception e) {
			throw new SchemaException(e);
		}
	}

	private <T> Model.Effective<T> wrapElement(ComplexNode.Effective<T> element) {
		return new ModelWrapper<>(element);
	}

	private class OverridingProcessor implements SchemaProcessingContext {
		private final Deque<SchemaNodeConfigurator<?, ?, ?, ?>> configuratorStack;
		private List<?> currentProvidedValue;

		public OverridingProcessor() {
			configuratorStack = new ArrayDeque<>();
			currentProvidedValue = null;
		}

		@SuppressWarnings("unchecked")
		public <T> ComplexNode.Effective<T> process(
				ComplexNode.Effective<? super T> element, Model.Effective<T> override) {
			List<Model<? super T>> baseModel = new ArrayList<>(override.source()
					.baseModel());
			baseModel.add(wrapElement(element));

			DataLoader loader = new DataLoader() {
				@Override
				public <V> List<V> loadData(DataNode<V> node, DataSource data) {
					return (List<V>) currentProvidedValue;
				}
			};

			ModelConfigurator<Object> configurator = builder.configure(loader)
					.name(new QualifiedName("base"))
					.bindingClass(element.getPreInputClass())
					.unbindingClass(element.getOutMethod().getDeclaringClass())
					.isAbstract(true);

			ComplexNodeConfigurator<T> elementConfigurator = configurator.addChild()
					.complex().name(override.getName())
					.dataClass(override.getDataClass()).baseModel(baseModel);

			elementConfigurator = processBindingNode(override, elementConfigurator);
			elementConfigurator = processBindingChildNode(element,
					elementConfigurator);

			doChildren(override.children(), elementConfigurator);

			return (ComplexNode.Effective<T>) configurator.create().children().get(0)
					.effective();
		}

		private <C extends SchemaNodeConfigurator<?, ?, ?, ?>> C next(
				Function<ChildBuilder<?, ?>, C> next) {
			return next.apply(configuratorStack.peek().addChild());
		}

		private <N extends SchemaNode<N, ?>> N doChildren(
				List<? extends ChildNode<?, ?>> children,
				SchemaNodeConfigurator<?, ? extends N, ?, ?> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode<?, ?> child : children)
				child.effective().process(this);

			configuratorStack.pop();
			return configurator.create();
		}

		private <N extends SchemaNode<N, ?>> N doChildren(N node,
				SchemaNodeConfigurator<?, N, ?, ?> c) {
			if (node.isAbstract() != null)
				c = c.isAbstract(node.isAbstract());

			return doChildren(node.children(), c.name(node.getName()));
		}

		public <U, C extends BindingNodeConfigurator<C, ?, U, ?, ?>> C processBindingNode(
				BindingNode<U, ?, ?> node, C c) {
			c = tryProperty(node.getBindingClass(), c::bindingClass, c);
			c = tryProperty(node.getBindingStrategy(), c::bindingStrategy, c);
			c = tryProperty(node.getUnbindingClass(), c::unbindingClass, c);
			c = tryProperty(node.getUnbindingFactoryClass(),
					c::unbindingFactoryClass, c);
			c = tryProperty(node.getUnbindingMethodName(), c::unbindingMethod, c);
			c = tryProperty(node.getUnbindingStrategy(), c::unbindingStrategy, c);
			c = tryProperty(node.getProvidedUnbindingMethodParameterNames(),
					c::providedUnbindingMethodParameters, c);

			return c;
		}

		public <U, C extends BindingChildNodeConfigurator<C, ?, ? extends U, ?, ?>> C processBindingChildNode(
				BindingChildNode<U, ?, ?> node, C c) {
			c = tryProperty(node.getOutMethodName(), c::outMethod, c);
			c = tryProperty(node.isOutMethodIterable(), c::outMethodIterable, c);
			c = tryProperty(node.occurrences(), c::occurrences, c);
			c = tryProperty(node.isOrdered(), c::ordered, c);

			return processInputNode(node, c);
		}

		public <U, C extends InputNodeConfigurator<C, ?, ?, ?>> C processInputNode(
				InputNode<?, ?> node, C c) {
			c = tryProperty(node.isInMethodCast(), c::isInMethodCast, c);
			c = tryProperty(node.getInMethodName(), c::inMethod, c);
			c = tryProperty(node.isInMethodChained(), c::inMethodChained, c);
			c = tryProperty(node.getPostInputClass(), c::postInputClass, c);

			return c;
		}

		@Override
		public <U> void accept(ComplexNode.Effective<U> node) {
			ComplexNode<U> source = node.source();

			ComplexNodeConfigurator<U> c = next(ChildBuilder::complex)
					.dataClass(source.getDataClass()).baseModel(source.baseModel())
					.extensible(source.isExtensible());

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
				if (source.getDataClass() == null) {
					cu = (DataNodeConfigurator<U>) c;
				} else {
					cu = c.dataClass(source.getDataClass());
				}
			} else {
				cu = c.type(source.type());
				cu = tryProperty(source.getDataClass(), cu::dataClass, cu);
			}

			doChildren(source,
					processBindingNode(source, processBindingChildNode(source, cu)));
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			InputSequenceNode source = node.source();

			InputSequenceNodeConfigurator<?> configurator = next(ChildBuilder::inputSequence);
			doChildren(source, processInputNode(source, configurator));
		}

		@Override
		public void accept(SequenceNode.Effective node) {
			SequenceNode source = node.source();

			doChildren(source, next(ChildBuilder::sequence));
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
			ChoiceNode source = node.source();

			ChoiceNodeConfigurator<?, ?> c = next(ChildBuilder::choice);

			doChildren(source,
					tryProperty(source.isMandatory(), p -> c.mandatory(p), c));
		}

		private <U, C extends SchemaNodeConfigurator<? extends C, ?, ?, ?>> C tryProperty(
				U property, Function<U, C> consumer, C c) {
			if (property != null)
				return consumer.apply(property);
			else
				return c;
		}
	}
}

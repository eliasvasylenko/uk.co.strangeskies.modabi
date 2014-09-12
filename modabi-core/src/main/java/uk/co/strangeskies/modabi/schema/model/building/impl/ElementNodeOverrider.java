package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ChoiceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ElementNodeOverrider {
	private final ModelBuilder builder;
	private final DataLoader loader;

	public ElementNodeOverrider(ModelBuilder builder, DataLoader loader) {
		this.builder = builder;
		this.loader = loader;
	}

	public <T> Model<T> override(
			AbstractModel.Effective<? super T, ?, ?> element,
			Model.Effective<T> override) {
		@SuppressWarnings("unchecked")
		ModelConfigurator<?> configurator = builder.configure(loader);

		new OverridingProcessor(configurator, override);

		return configurator.create();
	}

	private class OverridingProcessor implements SchemaProcessingContext {
		private final Deque<SchemaNodeConfigurator<?, ?, ?, ?>> configuratorStack;

		public OverridingProcessor(SchemaNodeConfigurator<?, ?, ?, ?> configurator,
				Model.Effective<?> override) {
			configuratorStack = new ArrayDeque<>();
			configuratorStack.push(configurator);
			for (ChildNode.Effective<?, ?> node : override)
				node.process(this);
		}

		private <C extends SchemaNodeConfigurator<?, ?, ?, ?>> C push(
				Function<ChildBuilder<?, ?>, C> next) {
			C configurator = next.apply(configuratorStack.peek().addChild());
			configuratorStack.push(configurator);
			return configurator;
		}

		private void pop() {
			configuratorStack.pop().create();
		}

		@Override
		public <U> void accept(ElementNode.Effective<U> node) {
			ElementNodeConfigurator<Object> configurator = push(ChildBuilder::element);

			configurator
					.baseModel(node.baseModel())
					.dataClass(node.getDataClass())
					.bindingClass(node.getBindingClass())
					.bindingStrategy(node.getBindingStrategy())
					.unbindingClass(node.getUnbindingClass())
					.unbindingFactoryClass(node.getUnbindingFactoryClass())
					.unbindingMethod(node.getUnbindingMethodName())
					.unbindingStrategy(node.getUnbindingStrategy())
					.providedUnbindingMethodParameters(
							node.getProvidedUnbindingMethodParameterNames())
					.inMethod(node.getInMethodName())
					.inMethodChained(node.isInMethodChained())
					.postInputClass(node.getPostInputClass())
					.allowInMethodResultCast(node.allowInMethodResultCast())
					.outMethod(node.getOutMethodName())
					.outMethodIterable(node.isOutMethodIterable())
					.occurances(node.occurances()).ordered(node.isOrdered())
					.extensible(node.isExtensible()).isAbstract(node.isAbstract())
					.allowInMethodResultCast(node.allowInMethodResultCast());

			pop();
		}

		@Override
		public <U> void accept(DataNode.Effective<U> node) {
			DataNodeConfigurator<Object> configurator = push(ChildBuilder::data);

			pop();
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
			InputSequenceNodeConfigurator<?> configurator = push(ChildBuilder::inputSequence);

			pop();
		}

		@Override
		public void accept(SequenceNode.Effective node) {
			SequenceNodeConfigurator<?, ?> configurator = push(ChildBuilder::sequence);

			pop();
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
			ChoiceNodeConfigurator<?, ?> configurator = push(ChildBuilder::choice);

			pop();
		}
	}
}

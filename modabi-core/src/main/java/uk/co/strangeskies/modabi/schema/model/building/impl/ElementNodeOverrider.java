package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ElementNodeOverrider {
	private final ModelBuilder builder;
	private final DataLoader loader;

	public ElementNodeOverrider(ModelBuilder builder, DataLoader loader) {
		this.builder = builder;
		this.loader = loader;
	}

	@SuppressWarnings("unchecked")
	public <T> Model<T> override(ElementNode.Effective<? super T> element,
			Model.Effective<T> override) {
		return new OverridingProcessor().process(override, builder
				.configure(loader).baseModel(wrapElement(element)));
	}

	private <T> Model.Effective<T> wrapElement(ElementNode.Effective<T> element) {
		return null;
	}

	private class OverridingProcessor implements SchemaProcessingContext {
		private final Deque<SchemaNodeConfigurator<?, ?, ?, ?>> configuratorStack;

		public OverridingProcessor() {
			configuratorStack = new ArrayDeque<>();
		}

		public <T extends U, U> Model.Effective<T> process(
				Model.Effective<T> override, ModelConfigurator<U> configurator) {
			return doChildren(
					override,
					processBindingNode(override,
							configurator.dataClass(override.getDataClass()))).effective();
		}

		private <C extends SchemaNodeConfigurator<?, ?, ?, ?>> C next(
				Function<ChildBuilder<?, ?>, C> next) {
			return next.apply(configuratorStack.peek().addChild());
		}

		private <N extends SchemaNode<N, ?>> N doChildren(N node,
				SchemaNodeConfigurator<?, N, ?, ?> configurator) {
			configuratorStack.push(configurator);

			for (ChildNode.Effective<?, ?> child : node.effective().children())
				child.process(this);

			configuratorStack.pop();
			return configurator.create();
		}

		public <U, C extends BindingNodeConfigurator<C, ?, U, ?, ?>> C processBindingNode(
				BindingNode.Effective<U, ?, ?> node, C configutor) {
			return configutor
					.bindingClass(node.getBindingClass())
					.bindingStrategy(node.getBindingStrategy())
					.unbindingClass(node.getUnbindingClass())
					.unbindingFactoryClass(node.getUnbindingFactoryClass())
					.unbindingMethod(node.getUnbindingMethodName())
					.unbindingStrategy(node.getUnbindingStrategy())
					.providedUnbindingMethodParameters(
							node.getProvidedUnbindingMethodParameterNames());
		}

		@Override
		public <U> void accept(ElementNode.Effective<U> node) {
			doChildren(
					node,
					processBindingNode(
							node,
							next(ChildBuilder::element).dataClass(node.getDataClass())
									.baseModel(node.baseModel()).inMethod(node.getInMethodName())
									.inMethodChained(node.isInMethodChained())
									.postInputClass(node.getPostInputClass())
									.allowInMethodResultCast(node.allowInMethodResultCast())
									.outMethod(node.getOutMethodName())
									.outMethodIterable(node.isOutMethodIterable())
									.occurances(node.occurances()).ordered(node.isOrdered())
									.extensible(node.isExtensible())
									.isAbstract(node.isAbstract())
									.allowInMethodResultCast(node.allowInMethodResultCast())));
		}

		@Override
		public <U> void accept(DataNode.Effective<U> node) {
		}

		@Override
		public void accept(InputSequenceNode.Effective node) {
		}

		@Override
		public void accept(SequenceNode.Effective node) {
		}

		@Override
		public void accept(ChoiceNode.Effective node) {
		}
	}
}

package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.proxy.ProxyFactory;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.AbstractModel;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.ChildBuilder;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.building.ModelBuilder;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ElementNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.model.building.configurators.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;

public class ElementNodeOverrider {
	private final ModelBuilder builder;

	public ElementNodeOverrider(ModelBuilder builder) {
		this.builder = builder;
	}

	public <T> ElementNode.Effective<T> override(
			ElementNode.Effective<? super T> element, Model.Effective<T> override) {
		try {
			return new OverridingProcessor().process(element, override);
		} catch (SchemaException e) {
			throw e;
		} catch (Exception e) {
			throw new SchemaException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Model.Effective<T> wrapElement(ElementNode.Effective<T> element) {
		return (Model.Effective<T>) new ProxyFactory().createInterceptorProxy(
				wrapObject(element, AbstractModel.Effective.class,
						Model.Effective.class), i -> {
					String method = i.getMethod().getName();
					switch (method) {
					case "source":
					case "effective":
						return wrapElement(element);
					default:
						return i.proceed();
					}
				}, new Class[] { Model.Effective.class });
	}

	@SuppressWarnings("unchecked")
	private <I, O extends I, W extends I> W wrapObject(O element,
			Class<I> interfaceClass, Class<W> wrapperClass) {
		return (W) new ProxyFactory().createInvokerProxy(
				(proxy, method, parameters) -> {
					Class<?>[] parameterClasses;

					if (parameters == null)
						parameterClasses = new Class[] {};
					else
						parameterClasses = Arrays.stream(parameters).map(Object::getClass)
								.toArray(Class[]::new);

					method = Methods.tryFindMethod(Arrays.asList(method.getName()),
							interfaceClass, method.getReturnType(), false, parameterClasses);

					if (method != null)
						return method.invoke(element, parameters);

					return null;
				}, new Class[] { wrapperClass });
	}

	private class OverridingProcessor implements SchemaProcessingContext {
		private final Deque<SchemaNodeConfigurator<?, ?, ?, ?>> configuratorStack;
		private List<?> currentProvidedValue;

		public OverridingProcessor() {
			configuratorStack = new ArrayDeque<>();
			currentProvidedValue = null;
		}

		@SuppressWarnings("unchecked")
		public <T> ElementNode.Effective<T> process(
				ElementNode.Effective<? super T> element, Model.Effective<T> override) {
			List<Model<? super T>> baseModel = new ArrayList<>(override.baseModel());
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

			ElementNodeConfigurator<T> elementConfigurator = configurator.addChild()
					.element().name(override.getName())
					.dataClass(override.getDataClass()).baseModel(baseModel);

			elementConfigurator = processBindingNode(override, elementConfigurator);
			elementConfigurator = processBindingChildNode(element,
					elementConfigurator);

			doChildren(override.children(), elementConfigurator);

			return (ElementNode.Effective<T>) configurator.create().children().get(0)
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
			c = tryProperty(node.occurances(), c::occurances, c);
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
		public <U> void accept(ElementNode.Effective<U> node) {
			ElementNode<U> source = node.source();

			doChildren(
					source,
					processBindingNode(
							source,
							processBindingChildNode(
									source,
									next(ChildBuilder::element).dataClass(source.getDataClass())
											.baseModel(source.baseModel())
											.extensible(source.isExtensible()))));
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

			doChildren(source, next(ChildBuilder::choice));
		}

		private <U, C extends SchemaNodeConfigurator<C, ?, ?, ?>> C tryProperty(
				U property, Function<U, C> consumer, C c) {
			if (property != null)
				return consumer.apply(property);
			else
				return c;
		}
	}
}

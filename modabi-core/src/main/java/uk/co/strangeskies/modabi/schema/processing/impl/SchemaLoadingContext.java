package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.model.Model;
import uk.co.strangeskies.modabi.model.nodes.BindingChildNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode;
import uk.co.strangeskies.modabi.model.nodes.BindingNode.Effective;
import uk.co.strangeskies.modabi.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.model.nodes.DataNode;
import uk.co.strangeskies.modabi.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.model.nodes.InputNode;
import uk.co.strangeskies.modabi.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.utilities.function.collection.ListTransformationFunction;

class SchemaLoadingContext<T> implements SchemaProcessingContext {
	private final SchemaBinderImpl schemaBinderImpl;
	private final BindingNode<T, ?, ?> bindingNode;
	private final StructuredDataSource input;

	private final Deque<Object> bindingStack;
	private final Deque<SchemaNode<?, ?>> nodeStack;

	private SchemaLoadingContext(SchemaBinderImpl schemaBinderImpl,
			BindingNode<T, ?, ?> bindingNode, StructuredDataSource input,
			Deque<Object> bindingStack, Deque<SchemaNode<?, ?>> nodeStack) {
		this.schemaBinderImpl = schemaBinderImpl;
		this.bindingNode = bindingNode;
		this.input = input;

		this.bindingStack = bindingStack;
		this.nodeStack = nodeStack;

		try {
			bind(bindingNode.effective());
		} catch (SchemaException e) {
			throw new SchemaException("Problem at node '"
					+ nodeStack.stream().map(n -> n.getName().toString())
							.collect(Collectors.joining(" < "))
					+ "' binding data with node '" + bindingNode.getName() + "'.", e);
		} catch (Exception e) {
			throw new SchemaException("Unexpected problem at node '"
					+ getNodeStackString() + "' binding data with node '"
					+ bindingNode.getName() + "'.", e);
		}
	}

	private SchemaLoadingContext(SchemaBinderImpl schemaBinderImpl,
			Model<T> model, StructuredDataSource input) {
		this(schemaBinderImpl, model, input, new ArrayDeque<>(), new ArrayDeque<>());
	}

	private SchemaLoadingContext(SchemaLoadingContext<?> parent,
			BindingChildNode<T, ?, ?> bindingNode) {
		this(parent.schemaBinderImpl, bindingNode, parent.input, new ArrayDeque<>(
				parent.bindingStack), new ArrayDeque<>(parent.nodeStack));
	}

	private <U> U provideInstance(Class<U> builderClass) {
		return null;
	}

	private String getNodeStackString() {
		return nodeStack.stream().map(n -> n.getName().toString())
				.collect(Collectors.joining(" < "));
	}

	public static <T> BindingFuture<T> load(SchemaBinderImpl schemaBinderImpl,
			Model<T> model, StructuredDataSource input) {
		QualifiedName name = input.startNextChild();
		if (!name.equals(model.getName()))
			throw new SchemaException("Input root name '" + name
					+ "' does not match model name '" + model.getName() + "'.");
		return new SchemaLoadingContext<>(schemaBinderImpl, model, input).load();
	}

	public static BindingFuture<?> load(SchemaBinderImpl schemaBinderImpl,
			StructuredDataSource input) {
		Model<?> model = schemaBinderImpl.registeredModels.get(input
				.startNextChild());
		return new SchemaLoadingContext<>(schemaBinderImpl, model, input).load();
	}

	private BindingFuture<T> load() {
		return new BindingFuture<T>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isDone() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Binding<T> get() throws InterruptedException, ExecutionException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Binding<T> get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public QualifiedName getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Model<T> getModel() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<BindingFuture<?>> getBlockingBindings() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	@Override
	public void accept(ChoiceNode.Effective node) {
	}

	@Override
	public void accept(InputSequenceNode.Effective node) {
		processChildren(node);
	}

	public <U> U bind(BindingNode.Effective<U, ?, ?> node) {
		// String name = input.nextChild();
		// String namespace = input.getProperty("xmlns", null);

		bindingStack.push(bindData(node));
		processChildren(node);
		@SuppressWarnings("unchecked")
		U boundObject = (U) bindingStack.pop();
		return boundObject;
	}

	public Object bindData(BindingNode.Effective<?, ?, ?> node) {
		Object binding;

		if (node.getBindingStrategy() != null) {
			switch (node.getBindingStrategy()) {
			case PROVIDED:
				Class<?> providedClass = node.getBindingClass() != null ? node
						.getBindingClass() : node.getDataClass();
				binding = provideInstance(providedClass);

				break;
			case CONSTRUCTOR:
				binding = null;
				/*-
				try {
					List<Binding<?>> input = getInput();
					Class<?> a = null;
					a.getc
					Constructor<?> c = node.getBindingClass().getConstructor();
					return c.newInstance(prepareUnbingingParameterList(node, u));
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new SchemaException("Cannot invoke constructor " + c + " on "
							+ node.getUnbindingClass(), e);
				}
				 */
				break;
			case IMPLEMENT_IN_PLACE:
				// TODO some proxy magic with simple bean-like interfaces
				binding = null;

				break;
			case SOURCE_ADAPTOR:
				binding = null;
				break;
			case STATIC_FACTORY:
				binding = null;
				/*-
				try {
					List<Binding<?>> input = getInput();
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new SchemaException("Cannot invoke constructor " + c + " on "
							+ node.getUnbindingClass(), e);
				}*/
				break;
			case TARGET_ADAPTOR:
				bindingStack.peek();
				break;
			}
		}

		return binding;
	}

	private List<Binding<?>> getInput(Effective<?, ?, ?> node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> void accept(ElementNode.Effective<U> node) {
		invokeInMethod(node, (Object) bind(node));
	}

	private void invokeInMethod(InputNode.Effective<?, ?> node,
			Object... parameters) {
		try {
			Object object = bindingStack
					.peek()
					.getClass()
					.getMethod(node.getInMethodName(),
							ListTransformationFunction.apply(parameters, Object::getClass))
					.invoke(bindingStack.peek(), parameters);
			if (node.isInMethodChained()) {
				bindingStack.pop();
				bindingStack.push(object);
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	protected void processChildren(SchemaNode.Effective<?, ?> node,
			boolean skipFirst) {
		Iterator<ChildNode.Effective<?, ?>> children = node.children().iterator();

		if (skipFirst)
			children.next();

		while (children.hasNext())
			children.next().process(this);
	}

	@Override
	public <U> void accept(DataNode.Effective<U> node) {
		// invokeInMethod(node, (Object) input.getData(node.type()));
	}

	@Override
	public void accept(SequenceNode.Effective node) {

	}
}
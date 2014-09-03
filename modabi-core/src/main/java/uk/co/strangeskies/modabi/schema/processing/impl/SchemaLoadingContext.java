package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.model.AbstractModel;
import uk.co.strangeskies.modabi.model.Model;
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
	private final Model<T> model;
	private final StructuredDataSource input;

	private final Deque<Object> bindingStack;

	private SchemaLoadingContext(SchemaBinderImpl schemaBinderImpl,
			Model<T> model, StructuredDataSource input) {
		this.schemaBinderImpl = schemaBinderImpl;
		this.model = model;
		this.input = input;

		bindingStack = new ArrayDeque<>();
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
		Model<?> model = schemaBinderImpl.registeredModels.get(input.startNextChild());
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

	public <U> U bind(AbstractModel.Effective<U, ?, ?> node) {
		// String name = input.nextChild();
		// String namespace = input.getProperty("xmlns", null);

		bindingStack.push(provideInstance(node.getBindingClass()));
		processChildren(node);
		@SuppressWarnings("unchecked")
		U boundObject = (U) bindingStack.pop();
		return boundObject;
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

	private <U> U provideInstance(Class<U> builderClass) {
		return null;
	}

	protected void processChildren(SchemaNode.Effective<?, ?> node) {
		for (ChildNode.Effective<?, ?> child : node.children())
			child.process(this);
	}

	@Override
	public <U> void accept(DataNode.Effective<U> node) {
		// invokeInMethod(node, (Object) input.getData(node.type()));
	}

	@Override
	public void accept(SequenceNode.Effective node) {

	}
}
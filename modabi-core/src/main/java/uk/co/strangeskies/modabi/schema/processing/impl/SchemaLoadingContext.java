package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.invoker.NullInvoker;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
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
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.BindingStrategy;
import uk.co.strangeskies.modabi.schema.processing.PartialSchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;
import uk.co.strangeskies.utilities.ResultWrapper;

class SchemaLoadingContext<T> implements SchemaProcessingContext {
	private final SchemaBinderImpl schemaBinderImpl;
	private final BindingNode<T, ?, ?> bindingNode;
	private final StructuredDataSource input;

	private Deque<Object> bindingStack;
	private Deque<SchemaNode<?, ?>> nodeStack;

	private DataSource dataSource;
	private ReferenceSource referenceSource;
	private IncludeTarget includeTarget;
	private ImportSource importSource;

	private final Bindings bindings;

	private SchemaLoadingContext(SchemaBinderImpl schemaBinderImpl,
			BindingNode<T, ?, ?> bindingNode, StructuredDataSource input) {
		this.schemaBinderImpl = schemaBinderImpl;
		this.bindingNode = bindingNode;
		this.input = input;

		bindings = new Bindings();

		referenceSource = new ReferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(model, bindings.get(model), idDomain, id);
			}
		};

		includeTarget = new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				bindings.add(model, object);
			}
		};

		importSource = new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(model, schemaBinderImpl.bindingFutures(model)
						.stream().filter(BindingFuture::isDone).map(BindingFuture::resolve)
						.map(Binding::getData).collect(Collectors.toSet()), idDomain, id);
			}
		};
	}

	private SchemaLoadingContext(SchemaBinderImpl schemaBinderImpl,
			Model<T> model, StructuredDataSource input) {
		this(schemaBinderImpl, (BindingNode<T, ?, ?>) model, input);

		bindingStack = new ArrayDeque<>();
		nodeStack = new ArrayDeque<>();

		bindingStack.push(model);
	}

	private SchemaLoadingContext(SchemaLoadingContext<?> parent,
			BindingChildNode.Effective<T, ?, ?> bindingNode) {
		this(parent.schemaBinderImpl, bindingNode, parent.input);

		this.bindingStack = new ArrayDeque<>(parent.bindingStack);
		this.nodeStack = new ArrayDeque<>(parent.nodeStack);
	}

	public <U> U matchBinding(Model<U> model, Set<U> bindingCandidates,
			QualifiedName idDomain, DataSource id) {
		DataNode.Effective<?> node = (DataNode.Effective<?>) model
				.effective()
				.children()
				.stream()
				.filter(
						c -> c.getName().equals(idDomain)
								&& c instanceof DataNode.Effective<?>)
				.findAny()
				.orElseThrow(
						() -> new SchemaException("Can't fine child '" + idDomain
								+ "' to target for model '" + model + "'."));

		for (U binding : bindingCandidates) {
			/*
			 * TODO unbind 'node' on each candidate 'binding' and compare with 'id',
			 * returning first match.
			 */
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <U> U provide(Class<U> clazz) {
		if (clazz.equals(DataSource.class))
			return (U) dataSource;
		if (clazz.equals(ReferenceSource.class))
			return (U) referenceSource;
		if (clazz.equals(IncludeTarget.class))
			return (U) includeTarget;
		if (clazz.equals(ImportSource.class))
			return (U) importSource;

		return this.schemaBinderImpl.provide(clazz);
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

	private Future<T> loadingFuture() {
		FutureTask<T> future = new FutureTask<>(() -> {
			try {
				return bind(bindingNode.effective());
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
		});
		future.run();
		return future;
	}

	private BindingFuture<T> load() {
		Future<T> future = loadingFuture();

		return new BindingFuture<T>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isCancelled() {
				return future.isCancelled();
			}

			@Override
			public boolean isDone() {
				return future.isDone();
			}

			@Override
			public Binding<T> get() throws InterruptedException, ExecutionException {
				return new Binding<T>(getModel(), future.get());
			}

			@Override
			public Binding<T> get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				return new Binding<T>(getModel(), future.get(timeout, unit));
			}

			@Override
			public QualifiedName getName() {
				return bindingNode.getName();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Model<T> getModel() {
				return (Model<T>) bindingNode;
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
		nodeStack.push(node);
		nodeStack.pop();
	}

	@Override
	public void accept(InputSequenceNode.Effective node) {
		nodeStack.push(node);
		for (ChildNode<?, ?> child : node.children())
			child.effective().process(this);
		nodeStack.pop();
	}

	public <U> U bind(BindingNode.Effective<U, ?, ?> node) {
		@SuppressWarnings("unchecked")
		U boundObject = (U) bindData(node);
		return boundObject;
	}

	public Object bindData(BindingNode.Effective<?, ?, ?> node) {
		Object binding = null;

		BindingStrategy strategy = node.getBindingStrategy();
		if (strategy == null)
			strategy = BindingStrategy.PROVIDED;
		switch (strategy) {
		case PROVIDED:
			Class<?> providedClass = node.getBindingClass() != null ? node
					.getBindingClass() : node.getDataClass();
			binding = provide(providedClass);

			for (ChildNode<?, ?> child : node.children())
				child.effective().process(this);

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
			// TODO some proxy magic with simple bean-like semantics
			binding = new ProxyFactory().createInvokerProxy(new NullInvoker(),
					new Class[] { node.getClass() });

			break;
		case SOURCE_ADAPTOR:
			binding = bind(node.children().get(0));
			break;
		case STATIC_FACTORY:
			Method inputMethod = getFirstChildInputMethod(node);
			List<Object> parameters = bindings(node.children().get(0));
			try {
				binding = inputMethod.invoke(null, parameters.toArray());
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new SchemaException("Cannot invoke static factory method '"
						+ input + "' on class '" + node.getUnbindingClass()
						+ "' with parameters '" + parameters + "'.", e);
			}
			break;
		case TARGET_ADAPTOR:
			binding = bindingStack.peek();
			break;
		}

		return binding;
	}

	private Method getFirstChildInputMethod(BindingNode.Effective<?, ?, ?> node) {
		ResultWrapper<Method> result = new ResultWrapper<>();
		node.children().get(0).process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				result.setResult(node.getInMethod());
			}

			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				result.setResult(node.getInMethod());
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.setResult(node.getInMethod());
			}
		});
		return result.getResult();
	}

	private List<Object> bindings(ChildNode.Effective<?, ?> node) {
		List<Object> parameters = new ArrayList<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					child.process(this);
			}

			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				parameters.add(bind(node));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				parameters.add(bind(node));
			}
		});
		return parameters;
	}

	private Object bind(ChildNode.Effective<?, ?> node) {
		List<Object> parameters = new ArrayList<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				parameters.add(bind(node));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				parameters.add(bind(node));
			}
		});
		return parameters;
	}

	protected <U> U bind(BindingChildNode.Effective<U, ?, ?> node) {
		try {
			return new SchemaLoadingContext<>(SchemaLoadingContext.this, node)
					.loadingFuture().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new SchemaException("Cannot bind node '" + node.getName() + "'.", e);
		}
	}

	@Override
	public <U> void accept(ElementNode.Effective<U> node) {
		nodeStack.push(node);
		invokeInMethod(node, (Object) bind(node));
		nodeStack.pop();
	}

	private void invokeInMethod(InputNode.Effective<?, ?> node,
			Object... parameters) {
		if (!"null".equals(node.getInMethodName())) {
			Object object;
			try {
				object = node.getInMethod().invoke(bindingStack.peek(), parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new SchemaException(e);
			}

			if (node.isInMethodChained()) {
				bindingStack.pop();
				bindingStack.push(object);
			}
		}
	}

	@Override
	public <U> void accept(DataNode.Effective<U> node) {
		nodeStack.push(node);
		switch (node.format()) {
		case CONTENT:
			dataSource = input.readContent();
			break;
		case PROPERTY:
			dataSource = input.readProperty(node.getName());
			break;
		case SIMPLE_ELEMENT:
			while (node.getName().equals(input.peekNextChild())) {
				input.startNextChild(node.getName());
				dataSource = input.readContent();
				input.endChild();
			}
		}
		nodeStack.pop();
	}

	@Override
	public void accept(SequenceNode.Effective node) {
		nodeStack.push(node);
		nodeStack.pop();
	}
}
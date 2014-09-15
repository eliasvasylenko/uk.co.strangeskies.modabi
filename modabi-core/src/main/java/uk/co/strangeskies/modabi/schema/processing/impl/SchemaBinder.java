package uk.co.strangeskies.modabi.schema.processing.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.nodes.ChildNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ChoiceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputNode;
import uk.co.strangeskies.modabi.schema.model.nodes.InputSequenceNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SequenceNode;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.PartialSchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessingContext;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;
import uk.co.strangeskies.utilities.ResultWrapper;

class SchemaBinder {
	private final SchemaManager manager;

	private final ImportSource importSource;
	private final DataLoader loader;

	public SchemaBinder(SchemaManager manager) {
		this.manager = manager;

		importSource = new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(
						model,
						manager.bindingFutures(model).stream()
								.filter(BindingFuture::isDone).map(BindingFuture::resolve)
								.map(Binding::getData).collect(Collectors.toSet()), idDomain,
						id);
			}
		};

		loader = new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				return null;
			}
		};
	}

	public <T> BindingFuture<T> bind(Model.Effective<T> model,
			StructuredDataSource input) {
		if (!input.peekNextChild().equals(model.getName()))
			throw new SchemaException("Model '" + model.getName()
					+ "' does not match root input node '" + input.peekNextChild() + "'.");

		Bindings bindings = new Bindings();

		ReferenceSource referenceSource = new ReferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(model, bindings.get(model), idDomain, id);
			}
		};

		IncludeTarget includeTarget = new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				bindings.add(model, object);
			}
		};

		List<SchemaNode.Effective<?, ?>> bindingNodeStack = Collections
				.unmodifiableList(Arrays.asList(model));

		BindingContext context = new BindingContext() {
			@Override
			@SuppressWarnings("unchecked")
			public <U> U provide(Class<U> clazz) {
				if (clazz.equals(ReferenceSource.class))
					return (U) referenceSource;
				if (clazz.equals(IncludeTarget.class))
					return (U) includeTarget;
				if (clazz.equals(ImportSource.class))
					return (U) importSource;
				if (clazz.equals(DataLoader.class))
					return (U) loader;

				return manager.provide(clazz);
			}

			@Override
			public Object bindingObject() {
				throw exception("Root node is not bound to an object.");
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				return bindingNodeStack;
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return manager.getModels().get(nextElement);
			}

			@Override
			public <U> Set<BindingFuture<U>> bindingFutures(Model<U> model) {
				return manager.bindingFutures(model);
			}

			@Override
			public StructuredDataSource input() {
				return input;
			}
		};

		FutureTask<T> future = new FutureTask<>(() -> {
			try {
				context.input().startNextChild();
				return new BindingNodeBinder(context).bind(model);
			} catch (Exception e) {
				throw context.exception("Unexpected problem during binding.", e);
			}
		});
		future.run();

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
				return model.getName();
			}

			@SuppressWarnings("unchecked")
			@Override
			public Model<T> getModel() {
				return model;
			}

			@Override
			public Set<BindingFuture<?>> getBlockingBindings() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	private static <U> U matchBinding(Model<U> model, Set<U> bindingCandidates,
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

		System.out.println("model: " + model);
		System.out.println("id: " + id.get().data());
		int i = 0;
		for (U binding : bindingCandidates) {
			System.out.println("choice " + ++i + ": " + binding);
		}

		return null;
	}

	private List<Object> tryGetBindings(ChildNode.Effective<?, ?> node) {
		List<Object> parameters = new ArrayList<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public void accept(InputSequenceNode.Effective node) {
				for (ChildNode.Effective<?, ?> child : node.children())
					parameters.add(tryGetBinding(child));
			}

			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				parameters.add(tryGetBinding(node));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				parameters.add(tryGetBinding(node));
			}
		});
		return parameters;
	}

	private Object tryGetBinding(ChildNode.Effective<?, ?> node) {
		ResultWrapper<Object> result = new ResultWrapper<>();
		node.process(new PartialSchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				result.setResult(new ElementNodeBinder(null).bind(node).get(0));
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				result.setResult(new DataNodeBinder(null).bind(node).get(0));
			}
		});
		return result.getResult();
	}

	private static void invokeInMethod(InputNode.Effective<?, ?> node,
			Object... parameters) {
		if (!"null".equals(node.getInMethodName())) {
			Object object;
			try {
				object = node.getInMethod().invoke(bindingStack.peek(), parameters);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new SchemaException("Unable to call method '"
						+ node.getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "'.", e);
			}

			if (node.isInMethodChained()) {
				bindingStack.pop();
				bindingStack.push(object);
			}
		}
	}

	static SchemaProcessingContext createProcessingContext(
			BindingContext bindingContenxt) {
		return new SchemaProcessingContext() {
			@Override
			public <U> void accept(ElementNode.Effective<U> node) {
				for (U item : bindElementNode(node))
					invokeInMethod(node, item);
			}

			@Override
			public <U> void accept(DataNode.Effective<U> node) {
				for (U item : new DataNodeBinder(bindingContenxt).bind(node))
					invokeInMethod(node, item);
			}

			@Override
			public void accept(InputSequenceNode.Effective node) {
				nodeStack.push(node);
				List<Object> parameters = tryGetBindings(node);
				invokeInMethod(node, parameters.toArray());
				nodeStack.pop();
			}

			@Override
			public void accept(SequenceNode.Effective node) {
				nodeStack.push(node);
				for (ChildNode<?, ?> child : node.children())
					child.effective().process(this);
				nodeStack.pop();
			}

			@Override
			public void accept(ChoiceNode.Effective node) {
				nodeStack.push(node);
				nodeStack.pop();
			}
		};
	}
}

package uk.co.strangeskies.modabi.schema.processing.impl.binding;

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
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;

public class SchemaBinder {
	private final BindingContext context;

	public SchemaBinder(SchemaManager manager) {
		Bindings bindings = new Bindings();

		ImportSource importSource = new ImportSource() {
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

		DataLoader loader = new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				return null;
			}
		};

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

		context = new BindingContext() {
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
			public Object bindingTarget() {
				throw exception("Root node is not bound to an object.");
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> bindingNodeStack() {
				return Collections.emptyList();
			}

			@Override
			public Model.Effective<?> getModel(QualifiedName nextElement) {
				return manager.registeredModels().get(nextElement).effective();
			}

			@Override
			public StructuredDataSource input() {
				return null;
			}

			@Override
			public Bindings bindings() {
				return bindings;
			}
		};
	}

	public <T> BindingFuture<T> bind(Model.Effective<T> model,
			StructuredDataSource input) {
		BindingContext context = this.context.withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw context.exception("Model '" + model.getName()
					+ "' does not match root input node '" + inputRoot + "'.");

		FutureTask<T> future = new FutureTask<>(() -> {
			try {
				return new BindingNodeBinder(context).bind(model);
			} catch (SchemaException e) {
				throw e;
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
}

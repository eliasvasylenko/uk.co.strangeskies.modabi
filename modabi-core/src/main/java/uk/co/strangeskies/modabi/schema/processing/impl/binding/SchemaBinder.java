package uk.co.strangeskies.modabi.schema.processing.impl.binding;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.data.DataBindingType;
import uk.co.strangeskies.modabi.data.io.DataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.data.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.Model;
import uk.co.strangeskies.modabi.schema.model.building.DataLoader;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode;
import uk.co.strangeskies.modabi.schema.model.nodes.DataNode.Effective;
import uk.co.strangeskies.modabi.schema.model.nodes.ElementNode;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.unbinding.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.processing.impl.unbinding.DataNodeUnbinder;
import uk.co.strangeskies.modabi.schema.processing.impl.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.reference.ReferenceSource;

public class SchemaBinder {
	private final BindingContext context;

	public SchemaBinder(SchemaManager manager) {
		Bindings bindings = new Bindings();

		Function<BindingContext, ImportSource> importSource = context -> new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(context, model, manager.bindingFutures(model)
						.stream().filter(BindingFuture::isDone).map(BindingFuture::resolve)
						.map(Binding::getData).collect(Collectors.toSet()), idDomain, id);
			}

			@Override
			public String toString() {
				return "importSource";
			}
		};

		Function<BindingContext, DataLoader> loader = context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				// return new DataNodeBinder(context).bind(node); TODO
				return null;
			}

			@Override
			public String toString() {
				return "dataLoader";
			}
		};

		Function<BindingContext, ReferenceSource> referenceSource = context -> new ReferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(context, model, context.bindings().get(model),
						idDomain, id);
			}

			@Override
			public String toString() {
				return "referenceSource";
			}
		};

		Function<BindingContext, IncludeTarget> includeTarget = context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				System.out.println("             ~~~~ mode: " + model + " obj: "
						+ object);
				context.bindings().add(model, object);
			}

			@Override
			public String toString() {
				return "includeTarget";
			}
		};

		context = new BindingContext() {
			@Override
			@SuppressWarnings("unchecked")
			public <U> U provide(Class<U> clazz, BindingContext context) {
				if (clazz.equals(ReferenceSource.class))
					return (U) referenceSource.apply(context);
				if (clazz.equals(IncludeTarget.class))
					return (U) includeTarget.apply(context);
				if (clazz.equals(ImportSource.class))
					return (U) importSource.apply(context);
				if (clazz.equals(DataLoader.class))
					return (U) loader.apply(context);
				if (clazz.equals(BindingContext.class))
					return (U) context;

				return manager.provide(clazz);
			}

			@Override
			public String toString() {
				return "bindingContext";
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(ReferenceSource.class)
						|| clazz.equals(IncludeTarget.class)
						|| clazz.equals(ImportSource.class)
						|| clazz.equals(DataLoader.class)
						|| clazz.equals(BindingContext.class) || manager.isProvided(clazz);
			}

			@Override
			public List<Object> bindingTargetStack() {
				return Collections.emptyList();
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

			@Override
			public <T> List<DataBindingType<? extends T>> getMatchingTypes(
					Effective<T> node, Class<?> dataClass) {
				return manager.registeredTypes().getMatchingTypes(node, dataClass);
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

	private static <U> U matchBinding(BindingContext context, Model<U> model,
			Set<U> bindingCandidates, QualifiedName idDomain, DataSource id) {
		DataNode.Effective<?> node = (DataNode.Effective<?>) model
				.effective()
				.children()
				.stream()
				.filter(
						c -> c.getName().equals(idDomain)
								&& c instanceof DataNode.Effective<?>)
				.findAny()
				.orElseThrow(
						() -> context.exception("Can't find child '" + idDomain
								+ "' to target for model '" + model + "'."));

		for (U binding : bindingCandidates) {
			DataSource candidateId = unbindDataNode(context, node, binding);
			if (candidateId.equals(id)) {
				return binding;
			}
		}

		throw context.exception("Can't find any bindings matching '" + id
				+ "' in domain '" + idDomain + "' for model '" + model + "'.");
	}

	private static <V> DataSource unbindDataNode(BindingContext context,
			DataNode.Effective<V> node, Object source) {
		UnbindingContext unbindingContext = new UnbindingContext() {
			@Override
			public Object unbindingSource() {
				return source;
			}

			@Override
			public List<SchemaNode.Effective<?, ?>> unbindingNodeStack() {
				return Collections.emptyList();
			}

			@Override
			public <T> T provide(Class<T> clazz, UnbindingContext context) {
				return context.provide(clazz);
			}

			@Override
			public boolean isProvided(Class<?> clazz) {
				return context.isProvided(clazz);
			}

			@Override
			public StructuredDataTarget output() {
				return null;
			}

			@Override
			public <T> List<Model<? extends T>> getMatchingModels(
					ElementNode.Effective<T> element, Class<?> dataClass) {
				return Collections.emptyList();
			}

			@Override
			public <T> List<DataBindingType<? extends T>> getMatchingTypes(
					DataNode.Effective<T> node, Class<?> dataClass) {
				return context.getMatchingTypes(node, dataClass);
			}

			@Override
			public Bindings bindings() {
				return context.bindings();
			}
		};

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}
}

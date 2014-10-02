package uk.co.strangeskies.modabi.schema.processing.binding.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.Bindings;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Effective;
import uk.co.strangeskies.modabi.schema.node.ElementNode;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceSource;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.DataNodeUnbinder;

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
		};

		Function<BindingContext, DataLoader> loader = context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				// return new DataNodeBinder(context).bind(node); TODO loadData
				return null;
			}
		};

		Function<BindingContext, DereferenceSource> referenceSource = context -> new DereferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(context, model, context.bindings().get(model),
						idDomain, id);
			}
		};

		Function<BindingContext, IncludeTarget> includeTarget = context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);
			}
		};

		context = new BindingContext() {
			private final Map<Class<?>, List<? extends DataBindingType.Effective<?>>> attemptedMatchingTypes = new HashMap<>();

			@Override
			@SuppressWarnings("unchecked")
			public <U> U provide(Class<U> clazz, BindingContext context) {
				if (clazz.equals(DereferenceSource.class))
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
			public boolean isProvided(Class<?> clazz) {
				return clazz.equals(DereferenceSource.class)
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
			public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
					Effective<T> node, Class<?> dataClass) {
				@SuppressWarnings("unchecked")
				List<DataBindingType.Effective<? extends T>> cached = (List<DataBindingType.Effective<? extends T>>) attemptedMatchingTypes
						.get(dataClass);

				if (cached == null) {
					cached = manager.registeredTypes().getMatchingTypes(node, dataClass)
							.stream().map(n -> n.effective())
							.collect(Collectors.toCollection(ArrayList::new));
					attemptedMatchingTypes.put(dataClass, cached);
				}

				return cached;
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
			Set<U> bindingCandidates, QualifiedName idDomain, DataSource idSource) {
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

		for (U bindingCandidate : bindingCandidates) {
			DataSource candidateId = unbindDataNode(context, node, bindingCandidate);
			DataSource bufferedIdSource = idSource.copy();

			if (bufferedIdSource.size() - bufferedIdSource.index() < candidateId
					.size())
				continue;

			boolean match = true;
			for (int i = 0; i < candidateId.size() && match; i++) {
				DataItem<?> candidateData = candidateId.get();
				match = bufferedIdSource.get(candidateData.type()).equals(
						candidateData.data());
			}

			if (match) {
				for (int i = 0; i < candidateId.size(); i++)
					idSource.get();

				return bindingCandidate;
			}
		}

		throw context.exception("Can't find any bindings matching '" + idSource
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
			public <T> List<Model.Effective<T>> getMatchingModels(Class<T> dataClass) {
				return Collections.emptyList();
			}

			@Override
			public <U> List<Model.Effective<? extends U>> getMatchingModels(
					ElementNode.Effective<U> element, Class<? extends U> dataClass) {
				return Collections.emptyList();
			}

			@Override
			public <T> List<DataBindingType.Effective<? extends T>> getMatchingTypes(
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

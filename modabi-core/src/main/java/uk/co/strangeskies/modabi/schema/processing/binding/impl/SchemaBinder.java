package uk.co.strangeskies.modabi.schema.processing.binding.impl;

import java.util.List;
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
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.Model;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingException;
import uk.co.strangeskies.modabi.schema.processing.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.processing.reference.DereferenceSource;
import uk.co.strangeskies.modabi.schema.processing.reference.ImportSource;
import uk.co.strangeskies.modabi.schema.processing.reference.IncludeTarget;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.DataNodeUnbinder;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.UnbindingContextImpl;

public class SchemaBinder {
	private final BindingContextImpl context;

	public SchemaBinder(SchemaManager manager) {
		Function<BindingContextImpl, ImportSource> importSource = context -> new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(
						manager,
						context,
						model,
						manager.bindingFutures(model).stream()
								.filter(BindingFuture::isDone).map(BindingFuture::resolve)
								.map(Binding::getData).collect(Collectors.toSet()), idDomain,
						id);
			}
		};

		Function<BindingContextImpl, DataLoader> loader = context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				// return new DataNodeBinder(context).bind(node); TODO loadData
				return null;
			}
		};

		Function<BindingContextImpl, DereferenceSource> dereferenceSource = context -> new DereferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(manager, context, model,
						context.bindings().get(model), idDomain, id);
			}
		};

		Function<BindingContextImpl, IncludeTarget> includeTarget = context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);
			}
		};

		context = new BindingContextImpl(manager)
				.withProvision(DereferenceSource.class, dereferenceSource)
				.withProvision(IncludeTarget.class, includeTarget)
				.withProvision(ImportSource.class, importSource)
				.withProvision(DataLoader.class, loader)
				.withProvision(BindingContext.class, c -> c);
	}

	public <T> BindingFuture<T> bind(Model.Effective<T> model,
			StructuredDataSource input) {
		BindingContextImpl context = this.context.withInput(input);

		QualifiedName inputRoot = input.startNextChild();
		if (!inputRoot.equals(model.getName()))
			throw new BindingException("Model '" + model.getName()
					+ "' does not match root input node '" + inputRoot + "'.", context);

		FutureTask<T> future = new FutureTask<>(() -> {
			try {
				return new BindingNodeBinder(context).bind(model);
			} catch (SchemaException e) {
				throw e;
			} catch (Exception e) {
				throw new BindingException("Unexpected problem during binding.",
						context, e);
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

	private static <U> U matchBinding(SchemaManager manager,
			BindingContext context, Model<U> model, Set<U> bindingCandidates,
			QualifiedName idDomain, DataSource idSource) {
		DataNode.Effective<?> node = (DataNode.Effective<?>) model
				.effective()
				.children()
				.stream()
				.filter(
						c -> c.getName().equals(idDomain)
								&& c instanceof DataNode.Effective<?>)
				.findAny()
				.orElseThrow(
						() -> new BindingException("Can't find child '" + idDomain
								+ "' to target for model '" + model + "'.", context));

		for (U bindingCandidate : bindingCandidates) {
			DataSource candidateId = unbindDataNode(manager, node, bindingCandidate);
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

		throw new BindingException("Can't find any bindings matching '" + idSource
				+ "' in domain '" + idDomain + "' for model '" + model + "'.", context);
	}

	private static <V> DataSource unbindDataNode(SchemaManager manager,
			DataNode.Effective<V> node, Object source) {
		UnbindingContextImpl unbindingContext = new UnbindingContextImpl(manager)
				.withUnbindingSource(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}
}

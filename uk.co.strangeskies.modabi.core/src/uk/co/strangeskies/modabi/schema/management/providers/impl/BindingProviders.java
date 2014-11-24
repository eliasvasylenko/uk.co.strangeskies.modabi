package uk.co.strangeskies.modabi.schema.management.providers.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.management.providers.DereferenceSource;
import uk.co.strangeskies.modabi.schema.management.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeParser;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.DataNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.UnbindingContextImpl;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;
import uk.co.strangeskies.modabi.schema.node.model.Model;

public class BindingProviders {
	private final SchemaManager manager;

	public BindingProviders(SchemaManager manager) {
		this.manager = manager;
	}

	public Function<BindingContext, ImportSource> importSource() {
		return context -> new ImportSource() {
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
	}

	public Function<BindingContext, DataLoader> dataLoader() {
		return context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				// return new DataNodeBinder(context).bind(node); TODO loadData
				return null;
			}
		};
	}

	public Function<BindingContext, DereferenceSource> dereferenceSource() {
		return context -> new DereferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(manager, context, model,
						context.bindings().get(model), idDomain, id);
			}
		};
	}

	public Function<BindingContext, IncludeTarget> includeTarget() {
		return context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, U object) {
				context.bindings().add(model, object);
			}
		};
	}

	public Function<BindingContext, TypeParser> typeParser() {
		return context -> string -> {
			Class<?> raw = null;
			List<Class<?>> parameters = null;

			return TypeUtils.parameterize(raw,
					parameters.toArray(new Type[parameters.size()]));
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

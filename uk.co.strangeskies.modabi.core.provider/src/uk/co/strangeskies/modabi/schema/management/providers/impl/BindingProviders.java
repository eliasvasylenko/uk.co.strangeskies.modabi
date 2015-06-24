/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema.management.providers.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.proxy.ObjectProvider;
import org.apache.commons.proxy.ProxyFactory;
import org.apache.commons.proxy.provider.SingletonProvider;

import uk.co.strangeskies.modabi.io.DataItem;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.DataStreamState;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.Binding;
import uk.co.strangeskies.modabi.schema.management.SchemaManager;
import uk.co.strangeskies.modabi.schema.management.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.management.binding.BindingException;
import uk.co.strangeskies.modabi.schema.management.binding.BindingFuture;
import uk.co.strangeskies.modabi.schema.management.binding.impl.BindingContextImpl;
import uk.co.strangeskies.modabi.schema.management.binding.impl.DataNodeBinder;
import uk.co.strangeskies.modabi.schema.management.providers.DereferenceSource;
import uk.co.strangeskies.modabi.schema.management.providers.ImportSource;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeParser;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.DataNodeUnbinder;
import uk.co.strangeskies.modabi.schema.management.unbinding.impl.UnbindingContextImpl;
import uk.co.strangeskies.modabi.schema.node.ChildNode;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Effective;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.modabi.schema.node.building.DataLoader;

public class BindingProviders {
	private interface ModelBindingProvider {
		<T> Set<T> get(Model<T> model);
	}

	private final SchemaManager manager;

	public BindingProviders(SchemaManager manager) {
		this.manager = manager;
	}

	public Function<BindingContext, ImportSource> importSource() {
		return context -> new ImportSource() {
			@Override
			public <U> U importObject(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(manager, context, model,
						new ModelBindingProvider() {
							@Override
							public <T> Set<T> get(Model<T> model) {
								return manager.bindingFutures(model).stream()
										.filter(BindingFuture::isDone).map(BindingFuture::resolve)
										.map(Binding::getData).collect(Collectors.toSet());
							}
						}, idDomain, id);
			}
		};
	}

	/*
	 * TODO from BindingContext not BindingContextImpl
	 */
	public Function<BindingContextImpl, DataLoader> dataLoader() {
		return context -> new DataLoader() {
			@Override
			public <U> List<U> loadData(DataNode<U> node, DataSource data) {
				return new DataNodeBinder(context).bind(node.effective());
			}
		};
	}

	public Function<BindingContext, DereferenceSource> dereferenceSource() {
		return context -> new DereferenceSource() {
			@Override
			public <U> U reference(Model<U> model, QualifiedName idDomain,
					DataSource id) {
				return matchBinding(manager, context, model, context.bindings()::get,
						idDomain, id);
			}
		};
	}

	public Function<BindingContext, IncludeTarget> includeTarget() {
		return context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, Collection<? extends U> objects) {
				for (U object : objects)
					context.bindings().add(model, object);
			}
		};
	}

	public Function<BindingContext, TypeParser> typeParser() {
		return context -> string -> null;
		/*-{
			Class<?> raw = null;
			List<Class<?>> parameters = null;

			return TypeUtils.parameterize(raw,
					parameters.toArray(new Type[parameters.size()]));
		};
		 */
	}

	@SuppressWarnings("unchecked")
	private static <U> U matchBinding(SchemaManager manager,
			BindingContext context, Model<U> model, ModelBindingProvider bindings,
			QualifiedName idDomain, DataSource idSource) {
		if (idSource.currentState() == DataStreamState.TERMINATED)
			throw new BindingException("No further id data to match in domain '"
					+ idDomain + "' for model '" + model + "'.", context);

		DataItem<?> id = idSource.get();

		ObjectProvider objectProvider = () -> {
			Set<U> bindingCandidates = bindings.get(model);

			ChildNode<?, ?> child = (DataNode.Effective<?>) model.effective().child(
					idDomain);
			if (!(child instanceof DataNode.Effective<?>))
				throw new BindingException("Can't find child '" + idDomain
						+ "' to target for model '" + model + "'.", context);
			DataNode.Effective<?> node = (Effective<?>) child;

			for (U bindingCandidate : bindingCandidates) {
				DataSource candidateId = unbindDataNode(manager, node, bindingCandidate);

				if (candidateId.size() != 1)
					continue;

				DataItem<?> candidateData = candidateId.get();

				if (id.data(candidateData.type()).equals(candidateData.data())) {
					return bindingCandidate;
				}
			}

			throw new BindingException("Can't find any bindings matching id '" + id
					+ "' in domain '" + idDomain + "' for model '" + model + "'.",
					context);
		};

		Set<? extends Class<?>> classes = model.effective().getDataType()
				.getRawTypes();
		return (U) new ProxyFactory().createDelegatorProxy(new SingletonProvider(
				objectProvider), classes.toArray(new Class<?>[classes.size()]));
	}

	private static <V> DataSource unbindDataNode(SchemaManager manager,
			DataNode.Effective<V> node, Object source) {
		UnbindingContextImpl unbindingContext = new UnbindingContextImpl(manager)
				.withUnbindingSource(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}
}

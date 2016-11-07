/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.processing.BindingProviders;
import uk.co.strangeskies.modabi.impl.processing.DataNodeBinder;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.UnbindingProviders;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;
import uk.co.strangeskies.utilities.collection.ObservableSet;
import uk.co.strangeskies.utilities.collection.ScopedObservableSet;
import uk.co.strangeskies.utilities.collection.ScopedObservableSet.ScopedObservableSetImpl;

@Component(immediate = true)
public class SchemaManagerImpl implements SchemaManager {
	private final SchemaManager parent;

	private final SchemaBuilder schemaBuilder;

	private final Map<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFutures;
	private final Map<QualifiedName, ScopedObservableSetImpl<Binding<?>>> bindings;

	private final CoreSchemata coreSchemata;

	private final Provisions provisions;

	/*
	 * Schemata, models, and data types registered to this manager.
	 */
	private final Schemata registeredSchemata;
	private final Models registeredModels;

	/*
	 * Data formats available for binding and unbinding
	 */
	private final DataFormats dataFormats;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl());
	}

	/*
	 * copy constructor
	 */
	protected SchemaManagerImpl(SchemaManager parent,

			Map<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFutures,
			Map<QualifiedName, ScopedObservableSetImpl<Binding<?>>> bindings,

			SchemaBuilder schemaBuilder, CoreSchemata coreSchemata,

			Schemata registeredSchemata, Models registeredModels, Provisions provisions, DataFormats dataFormats) {

		this.parent = parent;

		this.schemaBuilder = schemaBuilder;
		this.coreSchemata = coreSchemata;

		this.bindingFutures = bindingFutures;
		this.bindings = bindings;

		this.registeredSchemata = registeredSchemata;
		this.registeredSchemata.changes().addObserver(c -> {
			Set<Schema> added = new HashSet<>(c.added());
			registeredSchemata.getParentScope().ifPresent(p -> added.removeAll(p));
			registerSchemata(added);
		});

		this.registeredModels = registeredModels;

		this.provisions = provisions;

		this.dataFormats = dataFormats;
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder /* TODO , Log log */) {
		this(schemaBuilder, new CoreSchemata(schemaBuilder));
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder,
			CoreSchemata coreSchemata /* TODO , Log log */) {
		parent = null;

		this.schemaBuilder = schemaBuilder;
		this.coreSchemata = coreSchemata;

		bindingFutures = new ConcurrentHashMap<>();
		bindings = new ConcurrentHashMap<>();

		registeredSchemata = new Schemata();
		registeredSchemata.changes().addObserver(c -> registerSchemata(c.added()));

		registeredModels = new Models();

		provisions = new ProvisionsImpl();

		dataFormats = new DataFormats();

		/*
		 * Register schema builder provider
		 */
		provisions().add(Provider.over(SchemaBuilder.class, c -> c.manager().getSchemaBuilder()));

		/*
		 * Register collection providers
		 */
		provisions().add(Provider.over(ProcessingContext.class, c -> c));
		provisions().add(Provider.over(new @Infer TypeToken<SortedSet<?>>() {}, () -> new TreeSet<>()));
		provisions().add(Provider.over(new @Infer TypeToken<Set<?>>() {}, () -> new HashSet<>()));
		provisions().add(Provider.over(new @Infer TypeToken<LinkedHashSet<?>>() {}, () -> new LinkedHashSet<>()));
		provisions().add(Provider.over(new @Infer TypeToken<List<?>>() {}, () -> new ArrayList<>()));
		provisions().add(Provider.over(new @Infer TypeToken<Map<?, ?>>() {}, () -> new HashMap<>()));

		new BindingProviders().registerProviders(provisions());
		new UnbindingProviders().registerProviders(provisions());

		QualifiedName schemaModelName = coreSchemata.metaSchema().getSchemaModel().name();
		bindingFutures.put(schemaModelName, ScopedObservableSet.over(HashSet::new));
		bindings.put(schemaModelName, ScopedObservableSet.over(HashSet::new));
		registeredSchemata().add(coreSchemata.metaSchema());
	}

	public ProcessingContextImpl getProcessingContext() {
		return new ProcessingContextImpl(this);
	}

	@Override
	public SchemaConfigurator getSchemaConfigurator() {
		ProcessingContextImpl context = getProcessingContext();

		for (Model<?> schemaModel : getMetaSchema().models()) {
			context.bindings().add(context.manager().getMetaSchema().getMetaModel(), schemaModel);
		}

		return getSchemaBuilder().configure(DataNodeBinder.dataLoader(context));
	}

	@Override
	public SchemaBuilder getSchemaBuilder() {
		return new SchemaBuilder() {
			@Override
			public SchemaConfigurator configure(DataLoader loader) {
				return new SchemaConfiguratorDecorator(schemaBuilder.configure(loader)) {
					@Override
					public Schema create() {
						Schema schema = super.create();
						registeredSchemata().add(schema);
						return schema;
					}
				};
			}
		};
	}

	private void registerSchemata(Set<Schema> added) {
		for (Schema schema : added) {
			for (Model<?> model : schema.models())
				registerModel(model);

			for (Schema dependency : schema.dependencies())
				registeredSchemata.add(dependency);

			registerBindingImpl(new Binding<>(coreSchemata.metaSchema().getSchemaModel(), schema));
		}
	}

	private void registerModel(Model<?> model) {
		synchronized (registeredModels) {
			if (registeredModels.add(model)) {

				/*
				 * TODO add/fetch scope on parent first if we have a parent, then add
				 * nested one here
				 */
				bindingFutures.put(model.name(), ScopedObservableSet.over(HashSet::new));
				bindings.put(model.name(), ScopedObservableSet.over(HashSet::new));
			}
		}
	}

	protected <T> BindingFuture<T> registerBindingImpl(Binding<T> binding) {
		BindingFuture<T> future = BindingFuture.forBinding(binding);
		bindingFutures.get(binding.getNode().name()).add(future);
		bindings.get(binding.getNode().name()).add(binding);
		return future;
	}

	@Override
	public MetaSchema getMetaSchema() {
		return coreSchemata.metaSchema();
	}

	@Override
	public BaseSchema getBaseSchema() {
		return coreSchemata.baseSchema();
	}

	<T> BindingFuture<T> addBindingFuture(BindingFuture<T> bindingFuture) {
		new Thread(() -> {
			try {
				Model<T> model = bindingFuture.getModelFuture().get();
				QualifiedName modelName = model.name();

				bindingFutures.get(modelName).add(bindingFuture);

				try {
					bindings.get(model).add(bindingFuture.get());
				} catch (Exception e) {
					bindingFutures.get(modelName).remove(bindingFuture);
				}
			} catch (Exception e) {}
		}).start();

		return bindingFuture;
	}

	@Override
	public InputBinder<?> bindInput() {
		return InputBinderImpl.bind(getProcessingContext(), registeredFormats(), this::addBindingFuture);
	}

	@Override
	public <T> OutputBinder<T> bindOutput(T data) {
		return OutputBinderImpl.bind(getProcessingContext(), registeredFormats(), data);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, BindingFuture<T>> getBindingFutures(Model<T> model) {
		synchronized (bindingFutures.get(model.name())) {
			return (ObservableSet) bindingFutures.get(model.name());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, Binding<T>> getBindings(Model<T> model) {
		synchronized (bindings.get(model.name())) {
			return (ObservableSet) bindings.get(model.name());
		}
	}

	@Override
	public Schemata registeredSchemata() {
		return registeredSchemata;
	}

	@Override
	public Models registeredModels() {
		return registeredModels;
	}

	@Override
	public DataFormats registeredFormats() {
		return dataFormats;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unregisterDataInterface")
	void registerDataInterface(StructuredDataFormat loader) {
		registeredFormats().add(loader);
	}

	void unregisterDataInterface(StructuredDataFormat loader) {
		registeredFormats().remove(loader);
	}

	@Override
	public Provisions provisions() {
		return provisions;
	}

	@Override
	public Optional<SchemaManager> getParentScope() {
		return Optional.ofNullable(parent);
	}

	@Override
	public SchemaManager nestChildScope() {
		Map<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFutures = new ConcurrentHashMap<>();
		Map<QualifiedName, ScopedObservableSetImpl<Binding<?>>> bindings = new ConcurrentHashMap<>();

		for (Map.Entry<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFuture : this.bindingFutures
				.entrySet()) {
			bindingFutures.put(bindingFuture.getKey(), bindingFuture.getValue().nestChildScope());
		}

		for (Map.Entry<QualifiedName, ScopedObservableSetImpl<Binding<?>>> binding : this.bindings.entrySet()) {
			bindings.put(binding.getKey(), binding.getValue().nestChildScope());
		}

		return new SchemaManagerImpl(this, bindingFutures, bindings, schemaBuilder, coreSchemata,
				registeredSchemata.nestChildScope(), registeredModels.nestChildScope(), provisions.nestChildScope(),
				dataFormats.nestChildScope());
	}

	@Override
	public void collapseIntoParentScope() {
		registeredModels.collapseIntoParentScope();
		registeredSchemata.collapseIntoParentScope();
		provisions.collapseIntoParentScope();
	}

	@Override
	public SchemaManager copy() {
		Map<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFutures = new ConcurrentHashMap<>();
		Map<QualifiedName, ScopedObservableSetImpl<Binding<?>>> bindings = new ConcurrentHashMap<>();

		for (Map.Entry<QualifiedName, ScopedObservableSetImpl<BindingFuture<?>>> bindingFuture : this.bindingFutures
				.entrySet()) {
			bindingFutures.put(bindingFuture.getKey(), bindingFuture.getValue().copy());
		}

		for (Map.Entry<QualifiedName, ScopedObservableSetImpl<Binding<?>>> binding : this.bindings.entrySet()) {
			bindings.put(binding.getKey(), binding.getValue().copy());
		}

		return new SchemaManagerImpl(parent, bindingFutures, bindings, schemaBuilder, coreSchemata,
				registeredSchemata.copy(), registeredModels.copy(), provisions.copy(), dataFormats.copy());
	}
}

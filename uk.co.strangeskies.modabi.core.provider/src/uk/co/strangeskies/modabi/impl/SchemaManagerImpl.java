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
import java.util.Arrays;
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
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.Unbinder;
import uk.co.strangeskies.modabi.impl.processing.BindingProviders;
import uk.co.strangeskies.modabi.impl.processing.DataNodeBinder;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.UnbindingProviders;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypeToken.Infer;
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
	private final DataTypes registeredTypes;

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

			Schemata registeredSchemata, Models registeredModels, DataTypes registeredTypes, Provisions provisions,
			DataFormats dataFormats) {

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
		this.registeredTypes = registeredTypes;

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
		registeredTypes = new DataTypes();

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

		for (Model<?> schemaModel : getMetaSchema().getModels()) {
			context.bindings().add(context.manager().getMetaSchema().getMetaModel(), schemaModel);
		}
		for (DataType<?> schemaDataType : getMetaSchema().getDataTypes()) {
			context.bindings().add(context.manager().getMetaSchema().getDataTypeModel(), schemaDataType);
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
			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataType<?> type : schema.getDataTypes())
				registerDataType(type);

			for (Schema dependency : schema.getDependencies())
				registeredSchemata.add(dependency);

			registerBindingImpl(new Binding<>(coreSchemata.metaSchema().getSchemaModel().effective(), schema));
		}
	}

	private void registerModel(Model<?> model) {
		synchronized (registeredModels) {
			if (registeredModels.add(model)) {
				registeredModels.notifyAll();

				/*
				 * TODO add/fetch scope on parent first if we have a parent, then add
				 * nested one here
				 */
				bindingFutures.put(model.name(), ScopedObservableSet.over(HashSet::new));
				bindings.put(model.name(), ScopedObservableSet.over(HashSet::new));
			}
		}
	}

	private void registerDataType(DataType<?> type) {
		registeredTypes.add(type);
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
				Model.Effective<T> model = bindingFuture.getModelFuture().get().effective();
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
	public <T> Binder<T> bind(Model<T> model) {
		return new BinderImpl<>(this, input -> model.effective(), this::addBindingFuture);
	}

	private Model<?> waitForModel(QualifiedName modelName) {
		synchronized (registeredModels) {
			Model<?> model;
			while ((model = registeredModels.get(modelName)) == null) {
				try {
					registeredModels.wait();
				} catch (InterruptedException e) {
					throw new SchemaException("No model found to match the root element '" + modelName + "'", e);
				}
			}
			return model;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Binder<T> bind(TypeToken<T> dataClass) {
		return new BinderImpl<>(this, input -> {
			Model<?> model = waitForModel(input.peekNextChild());

			List<Model<T>> models = registeredModels.getModelsWithType(dataClass);

			if (!models.contains(model)) {
				throw new IllegalArgumentException("None of the models '" + models + "' compatible with the class '" + dataClass
						+ "' match the root element '" + input.peekNextChild() + "'");
			}

			/*
			 * TODO model adapter to enforce possibly more specific generic type.
			 */

			return (Model<T>) model;
		}, this::addBindingFuture);
	}

	@Override
	public Binder<?> bind() {
		return new BinderImpl<>(this, input -> waitForModel(input.peekNextChild()).effective(), this::addBindingFuture);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, BindingFuture<T>> getBindingFutures(Model<T> model) {
		synchronized (bindingFutures.get(model.effective().name())) {
			return (ObservableSet) bindingFutures.get(model.effective().name());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, Binding<T>> getBindings(Model<T> model) {
		synchronized (bindings.get(model.effective().name())) {
			return (ObservableSet) bindings.get(model.effective().name());
		}
	}

	@Override
	public <T> Unbinder<T> unbind(Model<T> model, T data) {
		return new UnbinderImpl<>(this, data, context -> Arrays.asList(model.effective()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Unbinder<T> unbind(T data) {
		return unbind((TypeToken<T>) TypeToken.over(data.getClass()), data);
	}

	@Override
	public <T> Unbinder<T> unbind(TypeToken<T> dataType, T data) {
		return new UnbinderImpl<>(this, data, context -> registeredModels().getModelsWithType(dataType).stream()
				.map(n -> n.effective()).collect(Collectors.toList()));
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
	public DataTypes registeredTypes() {
		return registeredTypes;
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
				registeredSchemata.nestChildScope(), registeredModels.nestChildScope(), registeredTypes.nestChildScope(),
				provisions.nestChildScope(), dataFormats.nestChildScope());
	}

	@Override
	public void collapseIntoParentScope() {
		registeredModels.collapseIntoParentScope();
		registeredTypes.collapseIntoParentScope();
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
				registeredSchemata.copy(), registeredModels.copy(), registeredTypes.copy(), provisions.copy(),
				dataFormats.copy());
	}
}

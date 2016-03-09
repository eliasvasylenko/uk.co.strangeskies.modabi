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

@Component(immediate = true)
public class SchemaManagerImpl implements SchemaManager {
	private final SchemaBuilder schemaBuilder;

	private final Map<QualifiedName, ObservableSet<?, BindingFuture<?>>> bindingFutures;
	private final Map<QualifiedName, ObservableSet<?, Binding<?>>> bindings;

	private final CoreSchemata coreSchemata;

	private final Provisions provisions;

	private final Models registeredModels;
	private final DataTypes registeredTypes;
	private final Schemata registeredSchemata;

	private final DataFormats dataFormats;

	public SchemaManagerImpl() {
		this(new SchemaBuilderImpl());
	}

	public SchemaManagerImpl(SchemaBuilder schemaBuilder /* TODO , Log log */) {
		this.schemaBuilder = schemaBuilder;

		bindingFutures = new ConcurrentHashMap<>();
		bindings = new ConcurrentHashMap<>();

		coreSchemata = new CoreSchemata(schemaBuilder);

		registeredSchemata = new Schemata();
		registeredModels = new Models();
		registeredTypes = new DataTypes();

		provisions = new ProvisionsImpl();

		/*
		 * Register schema builder provider
		 */
		provisions().registerProvider(SchemaBuilder.class, this::getSchemaBuilder);

		/*
		 * Register collection providers
		 */
		provisions().registerProvider(ProcessingContext.class, c -> c);
		provisions().registerProvider(new @Infer TypeToken<SortedSet<?>>() {}, () -> new TreeSet<>());
		provisions().registerProvider(new @Infer TypeToken<Set<?>>() {}, () -> new HashSet<>());
		provisions().registerProvider(new @Infer TypeToken<LinkedHashSet<?>>() {}, () -> new LinkedHashSet<>());
		provisions().registerProvider(new @Infer TypeToken<List<?>>() {}, () -> new ArrayList<>());
		provisions().registerProvider(new @Infer TypeToken<Map<?, ?>>() {}, () -> new HashMap<>());

		new BindingProviders(this).registerProviders(provisions());
		new UnbindingProviders(this).registerProviders(provisions());

		dataFormats = new DataFormats();

		QualifiedName schemaModelName = coreSchemata.metaSchema().getSchemaModel().getName();
		bindingFutures.put(schemaModelName, ObservableSet.ofElements());
		bindings.put(schemaModelName, ObservableSet.ofElements());
		registerSchema(coreSchemata.metaSchema());
	}

	public ProcessingContextImpl getProcessingContext() {
		return new ProcessingContextImpl(this);
	}

	@Override
	public SchemaConfigurator getSchemaConfigurator() {
		return getSchemaBuilder().configure(DataNodeBinder.dataLoader(getProcessingContext()));
	}

	private SchemaBuilder getSchemaBuilder() {
		return new SchemaBuilder() {
			@Override
			public SchemaConfigurator configure(DataLoader loader) {
				return new SchemaConfiguratorDecorator(schemaBuilder.configure(loader)) {
					@Override
					public Schema create() {
						Schema schema = super.create();
						registerSchema(schema);
						return schema;
					}
				};
			}
		};
	}

	private boolean registerSchemaImpl(Schema schema) {
		if (registeredSchemata.add(schema)) {
			for (Model<?> model : schema.getModels())
				registerModel(model);

			for (DataType<?> type : schema.getDataTypes())
				registerDataType(type);

			for (Schema dependency : schema.getDependencies())
				registerSchema(dependency);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean registerSchema(Schema schema) {
		if (registerSchemaImpl(schema)) {
			registerBinding(coreSchemata.metaSchema().getSchemaModel(), schema);

			return true;
		} else {
			return false;
		}
	}

	void registerModel(Model<?> model) {
		synchronized (registeredModels) {
			if (registeredModels.add(model)) {
				registeredModels.notifyAll();

				bindingFutures.put(model.getName(), ObservableSet.ofElements());
				bindings.put(model.getName(), ObservableSet.ofElements());
			}
		}
	}

	void registerDataType(DataType<?> type) {
		registeredTypes.add(type);
	}

	@Override
	public <T> BindingFuture<T> registerBinding(Model<T> model, T data) {
		return registerBindingImpl(new Binding<T>() {
			@Override
			public Model<T> getModel() {
				return model;
			}

			@Override
			public T getData() {
				return data;
			}
			
			@Override
			public String toString() {
				return data + " : " + model;
			}
		});
	}

	protected <T> BindingFuture<T> registerBindingImpl(Binding<T> binding) {
		BindingFuture<T> future = BindingFuture.forBinding(binding);
		bindingFutures.get(binding.getModel().getName()).add(future);
		bindings.get(binding.getModel().getName()).add(binding);
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
				QualifiedName modelName = model.getName();

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

			List<Model<T>> models = registeredModels.getModelsWithClass(dataClass);

			if (!models.contains(model)) {
				throw new IllegalArgumentException("None of the models '" + models + "' compatible with the class '" + dataClass
						+ "' match the root element '" + input.peekNextChild() + "'");
			}

			/*
			 * TODO model adapter to enforce possibly more specific generic type.
			 */

			return (Model<T>) model;
		} , this::addBindingFuture);
	}

	@Override
	public Binder<?> bind() {
		return new BinderImpl<>(this, input -> waitForModel(input.peekNextChild()).effective(), this::addBindingFuture);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, BindingFuture<T>> getBindingFutures(Model<T> model) {
		synchronized (bindingFutures.get(model.effective().getName())) {
			return (ObservableSet) bindingFutures.get(model.effective().getName());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> ObservableSet<?, Binding<T>> getBindings(Model<T> model) {
		synchronized (bindings.get(model.effective().getName())) {
			return (ObservableSet) bindings.get(model.effective().getName());
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
		return new UnbinderImpl<>(this, data, context -> registeredModels().getModelsWithClass(dataType).stream()
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
	public DataFormats dataFormats() {
		return dataFormats;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unregisterDataInterface")
	void registerDataInterface(StructuredDataFormat loader) {
		dataFormats().registerDataFormat(loader);
	}

	void unregisterDataInterface(StructuredDataFormat loader) {
		dataFormats().unregisterDataFormat(loader);
	}

	@Override
	public Provisions provisions() {
		return provisions;
	}
}

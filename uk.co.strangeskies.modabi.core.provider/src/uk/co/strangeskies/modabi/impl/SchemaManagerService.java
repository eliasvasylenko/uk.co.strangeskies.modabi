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

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.collection.observable.ScopedObservableSet;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Providers;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.processing.BindingProviders;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.processing.UnbindingProviders;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

/**
 * A schema manager implementation for an OSGi context.
 * <p>
 * Any bundle can add schemata / models / data formats into the manager, but
 * they will only be visible to from the adding bundle ... With the exception of
 * schemata, which will be made available to any other bundle which explicitly
 * makes requirements on the appropriate capability.
 * <p>
 * Data formats and providers should typically be contributed via the service
 * whiteboard model. They will be available to all bundles which have visibility
 * to those services. There's not really much point in limiting visibility
 * further than this... Providers might cause conflicts with one another in odd
 * cases, but it should be reasonable to consider that the problem of the person
 * designing a deployment. All conflicts are resolved by service ranking!
 * 
 * @author Elias N Vasylenko
 */
@Component(immediate = true)
public class SchemaManagerService implements SchemaManager {
  private final SchemaBuilder schemaBuilder;
  private final CoreSchemata coreSchemata;
  private final Providers providers;

  /*
   * Schemata, models, and data types registered to this manager.
   */
  private final Schemata registeredSchemata;
  private final Models registeredModels;
  
  /*
   * Data formats available for binding and unbinding
   */
  private final DataFormats dataFormats;

  public SchemaManagerService() {
    this(new SchemaBuilderImpl());
  }

  public SchemaManagerService(SchemaBuilder schemaBuilder /* TODO , Log log */) {
    this(schemaBuilder, new CoreSchemata(schemaBuilder));
  }

  public SchemaManagerService(
      SchemaBuilder schemaBuilder,
      CoreSchemata coreSchemata /* TODO , Log log */) {
    this.schemaBuilder = schemaBuilder;
    this.coreSchemata = coreSchemata;

    registeredSchemata = new Schemata();
    registeredSchemata.changes().weakReference(this).observe(
        m -> m.owner().registerSchemata(m.message().added()));

    registeredModels = new Models();

    providers = new ProvidersImpl();

    dataFormats = new DataFormats();

    /*
     * Register schema builder provider
     */
    providers().add(Provider.over(SchemaBuilder.class, c -> c.manager().getSchemaBuilder()));

    /*
     * Register collection providers
     */
    providers().add(Provider.over(ProcessingContext.class, c -> c));
    providers().add(Provider.over(new @Infer TypeToken<SortedSet<?>>() {}, () -> new TreeSet<>()));
    providers().add(Provider.over(new @Infer TypeToken<Set<?>>() {}, () -> new HashSet<>()));
    providers().add(
        Provider.over(new @Infer TypeToken<LinkedHashSet<?>>() {}, () -> new LinkedHashSet<>()));
    providers().add(Provider.over(new @Infer TypeToken<List<?>>() {}, () -> new ArrayList<>()));
    providers().add(Provider.over(new @Infer TypeToken<Map<?, ?>>() {}, () -> new HashMap<>()));

    new BindingProviders().registerProviders(providers());
    new UnbindingProviders().registerProviders(providers());

    registeredSchemata().add(coreSchemata.metaSchema());
  }

  public ProcessingContextImpl getProcessingContext() {
    return new ProcessingContextImpl(this);
  }

  @Override
  public SchemaConfigurator getSchemaConfigurator() {
    return getSchemaBuilder().configure(DataNodeBinder.dataLoader(getProcessingContext()));
  }

  @Override
  public SchemaBuilder getSchemaBuilder() {
    return new SchemaBuilder() {
      @Override
      public SchemaConfigurator configure(DataLoader loader) {
        return new SchemaConfiguratorDecorator() {
          private SchemaConfigurator component = schemaBuilder.configure(loader);

          @Override
          public SchemaConfigurator getComponent() {
            return component;
          }

          @Override
          public Schema create() {
            Schema schema = SchemaConfiguratorDecorator.super.create();
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

      Model<Schema> schemaModel = coreSchemata.metaSchema().getSchemaModel();
      RootBindingPoint<Schema> bindingPoint = new RootBindingPoint<>(schemaModel);
      registerBindingImpl(new Binding<>(bindingPoint, schemaModel, forClass(Schema.class), schema));
    }
  }

  private void registerModel(Model<?> model) {
    synchronized (registeredModels) {
      if (registeredModels.add(model)) {

        /*
         * TODO add/fetch scope on parent first if we have a parent, then add nested one
         * here
         */
        bindingFutures.put(model.name(), ScopedObservableSet.over(HashSet::new));
        bindings.put(model.name(), ScopedObservableSet.over(HashSet::new));
      }
    }
  }

  @Override
  public MetaSchema getMetaSchema() {
    return coreSchemata.metaSchema();
  }

  @Override
  public BaseSchema getBaseSchema() {
    return coreSchemata.baseSchema();
  }

  @Override
  public InputBinder<?> bindInput() {
    return InputBinderImpl.bind(getProcessingContext(), registeredFormats());
  }

  @Override
  public <T> OutputBinder<T> bindOutput(T data) {
    return OutputBinderImpl.bind(getProcessingContext(), registeredFormats(), data);
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

  @Reference(
      cardinality = ReferenceCardinality.MULTIPLE,
      policy = ReferencePolicy.DYNAMIC,
      unbind = "unregisterDataInterface")
  void registerDataInterface(StructuredDataFormat loader) {
    registeredFormats().add(loader);
  }

  void unregisterDataInterface(StructuredDataFormat loader) {
    registeredFormats().remove(loader);
  }

  @Override
  public Providers providers() {
    return providers;
  }
}

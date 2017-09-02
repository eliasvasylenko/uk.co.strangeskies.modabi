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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.impl.processing.CoreProviders;
import uk.co.strangeskies.modabi.impl.processing.InputBinderImpl;
import uk.co.strangeskies.modabi.impl.processing.InputProviders;
import uk.co.strangeskies.modabi.impl.processing.OutputBinderImpl;
import uk.co.strangeskies.modabi.impl.processing.OutputProviders;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.impl.schema.SchemaBuilderDecorator;
import uk.co.strangeskies.modabi.impl.schema.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.schema.Model;

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
  class ModelsImpl extends Models {
    @Override
    public void add(Model<?> element) {
      super.add(element);
    }
  }

  class DataFormatsImpl extends DataFormats {
    @Override
    public void add(DataFormat element) {
      super.add(element);
    }

    @Override
    public void remove(DataFormat element) {
      super.remove(element);
    }
  }

  private final Supplier<SchemaBuilder> schemaBuilder;
  private final CoreSchemata coreSchemata;
  private final Set<Provider> providers;

  /*
   * Schemata, models, and data types registered to this manager.
   */
  private final Schemata registeredSchemata;
  private final ModelsImpl registeredModels;

  /*
   * Data formats available for binding and unbinding
   */
  private final DataFormatsImpl dataFormats;

  public SchemaManagerService() {
    this(SchemaBuilderImpl::new);
  }

  public SchemaManagerService(Supplier<SchemaBuilder> schemaBuilder /* TODO , Log log */) {
    this(schemaBuilder, new CoreSchemata(schemaBuilder));
  }

  public SchemaManagerService(
      Supplier<SchemaBuilder> schemaBuilder,
      CoreSchemata coreSchemata /* TODO , Log log */) {
    this.schemaBuilder = schemaBuilder;
    this.coreSchemata = coreSchemata;

    registeredSchemata = new Schemata();
    registeredModels = new ModelsImpl();
    dataFormats = new DataFormatsImpl();
    providers = new HashSet<>();

    registeredSchemata.getAllFuture().weakReference(this).observe(
        m -> m.owner().registerSchema(m.message()));

    /*
     * Register schema builder provider
     */
    providers.add(Provider.over(SchemaBuilder.class, c -> getSchemaBuilder()));

    /*
     * Register collection providers
     */
    new CoreProviders().getProviders().forEach(providers::add);
    new InputProviders().getProviders().forEach(providers::add);
    new OutputProviders().getProviders().forEach(providers::add);

    registeredSchemata.add(coreSchemata.metaSchema());
  }

  public ProcessingContextImpl getProcessingContext() {
    return new ProcessingContextImpl(this);
  }

  protected SchemaBuilder getSchemaBuilder() {
    return new SchemaBuilderDecorator() {
      private SchemaBuilder component = schemaBuilder.get();

      @Override
      public SchemaBuilder getComponent() {
        return component;
      }

      @Override
      public void setComponent(SchemaBuilder component) {
        this.component = component;
      }

      @Override
      public Schema create() {
        Schema schema = SchemaBuilderDecorator.super.create();
        registeredSchemata.add(schema);
        return schema;
      }
    };
  }

  private void registerSchema(Schema schema) {
    schema.dependencies().forEach(this::registerSchema);
    schema.models().forEach(this::registerModel);
  }

  private void registerModel(Model<?> model) {
    synchronized (registeredModels) {
      registeredModels.add(model);
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
    return InputBinderImpl
        .bind(getProcessingContext(), registeredFormats(), getBaseSchema().rootModel());
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
  void registerDataInterface(DataFormat loader) {
    dataFormats.add(loader);
  }

  void unregisterDataInterface(DataFormat loader) {
    dataFormats.remove(loader);
  }

  @Override
  public Stream<Provider> getProviders() {
    return providers.stream();
  }
}

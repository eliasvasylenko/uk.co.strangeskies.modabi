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

import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.binding.impl.BindingContextImpl;
import uk.co.strangeskies.modabi.binding.impl.CoreProviders;
import uk.co.strangeskies.modabi.binding.impl.InputBinderImpl;
import uk.co.strangeskies.modabi.binding.impl.InputProviders;
import uk.co.strangeskies.modabi.binding.impl.OutputBinderImpl;
import uk.co.strangeskies.modabi.binding.impl.OutputProviders;
import uk.co.strangeskies.modabi.expression.impl.FunctionalExpressionCompilerImpl;
import uk.co.strangeskies.modabi.io.structured.DataFormat;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;

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
  private final Schemata schemata;

  /*
   * Data formats available for binding and unbinding
   */
  private final DataFormatsImpl dataFormats;

  // TODO constructor injection
  public SchemaManagerService() {
    this(new SchemaBuilderImpl(new FunctionalExpressionCompilerImpl()));
  }

  private SchemaManagerService(SchemaBuilderImpl schemaBuilder) {
    this(() -> schemaBuilder);
  }

  public SchemaManagerService(Supplier<SchemaBuilder> schemaBuilder /* TODO , Log log */) {
    this(schemaBuilder, new CoreSchemata(schemaBuilder));
  }

  public SchemaManagerService(
      Supplier<SchemaBuilder> schemaBuilder,
      CoreSchemata coreSchemata /* TODO , Log log */) {
    this.schemaBuilder = schemaBuilder;
    this.coreSchemata = coreSchemata;

    schemata = new Schemata(coreSchemata.baseSchema());
    dataFormats = new DataFormatsImpl();
    providers = new HashSet<>();

    /*
     * Register schema builder provider
     */
    providers.add(Provider.over(SchemaBuilder.class, c -> schemaBuilder.get()));

    /*
     * Register collection providers
     */
    new CoreProviders().getProviders().forEach(providers::add);
    new InputProviders().getProviders().forEach(providers::add);
    new OutputProviders().getProviders().forEach(providers::add);

    schemata.add(coreSchemata.metaSchema());
  }

  public BindingContextImpl getProcessingContext() {
    return new BindingContextImpl(this);
  }

  @Override
  public InputBinder<?> bindInput() {
    return InputBinderImpl.bind(getProcessingContext(), dataFormats());
  }

  @Override
  public <T> OutputBinder<? super T> bindOutput(T data) {
    return OutputBinderImpl.bind(getProcessingContext(), dataFormats(), data);
  }

  @Override
  public Schemata schemata() {
    return schemata;
  }

  @Override
  public DataFormats dataFormats() {
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

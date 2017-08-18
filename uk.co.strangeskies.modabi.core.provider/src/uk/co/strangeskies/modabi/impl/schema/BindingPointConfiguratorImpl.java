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
package uk.co.strangeskies.modabi.impl.schema;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.Methods.findMethod;
import static uk.co.strangeskies.utilities.collection.StreamUtilities.streamOptional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.schema.utilities.OverrideBuilder;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.BindingPointConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.StructuralNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.token.TypeToken;

public abstract class BindingPointConfiguratorImpl<S extends BindingPointConfigurator<S>>
    implements BindingPointConfigurator<S> {
  private RuntimeException instantiationException;

  private QualifiedName name;
  private Boolean concrete;
  private Boolean export;
  private TypeToken<T> dataType;
  private List<Model<?>> baseModel;

  private BindingPoint<T> result;

  public BindingPointConfiguratorImpl() {
    baseModel = emptyList();
  }

  protected BindingPointConfiguratorImpl(BindingPointConfigurator<S> copy) {
    name = copy.getName().orElse(null);
    concrete = copy.getConcrete().orElse(null);
  }

  @Override
  public final S name(QualifiedName name) {
    this.name = name;

    return getThis();
  }

  @Override
  public final Optional<QualifiedName> getName() {
    return ofNullable(name);
  }

  @Override
  public final S concrete(boolean concrete) {
    this.concrete = concrete;

    return getThis();
  }

  @Override
  public final Optional<Boolean> getConcrete() {
    return ofNullable(concrete);
  }

  @Override
  public final S export(boolean export) {
    this.export = export;

    return getThis();
  }

  @Override
  public Optional<Boolean> getExport() {
    return ofNullable(export);
  }

  protected boolean isChildContextAbstract() {
    return getConcrete().orElse(false);
  }

  @Override
  public Optional<TypeToken<T>> getDataType() {
    return ofNullable(dataType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> BindingPointConfigurator<V, ?> dataType(TypeToken<V> dataType) {
    this.dataType = (TypeToken<T>) dataType;
    return (BindingPointConfigurator<V, ?>) getThis();
  }

  @Override
  public BindingPointConfigurator<?, ?> baseModel(Collection<? extends Model<?>> baseModel) {
    this.baseModel = new ArrayList<>(baseModel);
    return getThis();
  }

  @Override
  public Stream<Model<?>> getBaseModel() {
    return baseModel.stream();
  }

  @Override
  public SchemaNodeConfigurator node() {
    return new SchemaNodeConfiguratorImpl(this);
  }

  @Override
  public StructuralNode getNode() {
    // TODO Auto-generated method stub
    return null;
  }

  protected abstract Stream<? extends BindingPoint<?>> getOverriddenBindingPoints();

  public <U> OverrideBuilder<U> override(
      Function<? super BindingPoint<?>, ? extends U> overriddenValues,
      Function<? super BindingPointConfigurator<T, ?>, Optional<? extends U>> overridingValue) {
    return new OverrideBuilder<>(
        getOverriddenBindingPoints()
            .flatMap(p -> streamOptional(ofNullable(overriddenValues.apply(p))))
            .collect(toList()),
        overridingValue.apply(this),
        () -> findMethod(BindingPoint.class, overriddenValues::apply).getName());
  }
}

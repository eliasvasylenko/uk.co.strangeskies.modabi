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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.provisions.ImportWriter;
import uk.co.strangeskies.modabi.processing.provisions.IncludeWriter;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceWriter;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;

public class OutputProviders {
  public Function<ProcessingContext, IncludeWriter> includeWriter() {
    return context -> new IncludeWriter() {
      @Override
      public void include(Collection<? extends Binding<?>> bindings) {
        for (Binding<?> binding : bindings) {
          context.bindings().add(binding);

          // TODO try figure out if/why this was necessary...
          context.output().ifPresent(
              o -> o.registerNamespaceHint(binding.getModel().name().getNamespace()));
        }
      }
    };
  }

  public Function<ProcessingContext, ImportWriter> importWriter() {
    return context -> new ImportWriter() {
      @Override
      public <U> String referenceImport(Model<U> model, List<QualifiedName> idDomain, U object) {
        List<ChildBindingPoint<?>> node = model.descendents(idDomain);

        return null; // TODO
      }
    };
  }

  public Function<ProcessingContext, ReferenceWriter> referenceWriter() {
    return context -> new ReferenceWriter() {
      @Override
      public <U> String reference(Model<U> model, List<QualifiedName> idDomain, U object) {
        if (!context.bindings().getModelBindings(model).contains(object))
          throw new ModabiException(
              "Cannot find any instance '" + object + "' bound to model '" + model.name()
                  + "' from '" + context.bindings().getModelBindings(model) + "'");

        return importWriter().apply(context).referenceImport(model, idDomain, object);
      }
    };
  }

  public Stream<Provider> getProviders() {
    return Stream.of(
        Provider.over(ReferenceWriter.class, referenceWriter()),
        Provider.over(ImportWriter.class, importWriter()),
        Provider.over(IncludeWriter.class, includeWriter()));
  }
}

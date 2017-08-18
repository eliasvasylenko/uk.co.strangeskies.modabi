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
package uk.co.strangeskies.modabi.impl.processing;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Providers;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.provisions.ImportTarget;
import uk.co.strangeskies.modabi.processing.provisions.IncludeTarget;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;

public class UnbindingProviders {
  public Function<ProcessingContext, IncludeTarget> includeTarget() {
    return context -> new IncludeTarget() {
      @Override
      public <U> void include(Model<U> model, Collection<? extends U> objects) {
        for (U object : objects)
          context.bindings().add(model, object);

        context.output().ifPresent(o -> o.registerNamespaceHint(model.name().getNamespace()));
      }
    };
  }

  public Function<ProcessingContext, ImportTarget> importTarget() {
    return context -> new ImportTarget() {
      @Override
      public <U> DataSource referenceImport(
          Model<U> model,
          List<QualifiedName> idDomain,
          U object) {
        List<ChildBindingPoint<?>> node = model.descendents(idDomain);

        return new BindingNodeUnbinder(context, model, object).unbindToDataBuffer(node);
      }
    };
  }

  public Function<ProcessingContext, ReferenceTarget> referenceTarget() {
    return context -> new ReferenceTarget() {
      @Override
      public <U> DataSource reference(Model<U> model, List<QualifiedName> idDomain, U object) {
        if (!context.bindings().getModelBindings(model).contains(object))
          throw new ModabiException(
              "Cannot find any instance '" + object + "' bound to model '" + model.name()
                  + "' from '" + context.bindings().getModelBindings(model) + "'");

        return importTarget().apply(context).referenceImport(model, idDomain, object);
      }
    };
  }

  public void registerProviders(Providers providers) {
    providers.add(Provider.over(ReferenceTarget.class, referenceTarget()));
    providers.add(Provider.over(ImportTarget.class, importTarget()));
    providers.add(Provider.over(IncludeTarget.class, includeTarget()));
  }
}

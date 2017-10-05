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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.Bindings;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.Blocks;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.processing.provisions.ImportReader;
import uk.co.strangeskies.modabi.processing.provisions.ReferenceReader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.utility.IdentityProperty;

public class InputProviders {
  public Function<ProcessingContext, ImportReader> importReader() {
    return context -> new ImportReader() {
      @Override
      public <U> Binding<U> dereferenceImport(
          Model<U> model,
          List<QualifiedName> idDomain,
          String id) {
        return matchBinding(context, model, context.globalBindings(), idDomain, id, true);
      }
    };
  }

  public Function<ProcessingContext, ReferenceReader> referenceReader() {
    return context -> new ReferenceReader() {
      @Override
      public <U> Binding<U> dereference(Model<U> model, List<QualifiedName> idDomain, String id) {
        return matchBinding(context, model, context.localBindings(), idDomain, id, false);
      }
    };
  }

  private <U> Binding<U> matchBinding(
      ProcessingContext context,
      Model<U> model,
      Bindings bindings,
      List<QualifiedName> idDomain,
      String idSource,
      boolean externalDependency) {
    /*
     * Create a validation function for the parameters of this dependency
     */

    DataItem<?> id = idSource.get();

    List<ChildNode<?>> childStack = model.children(idDomain);
    if (!(childStack.get(childStack.size() - 1) instanceof SimpleNode<?>))
      throw new ProcessingException(
          "Can't find child '" + idDomain + "' to target for model '" + model + "'",
          context);

    /*
     * Resolve dependency!
     */
    Property<U, U> objectProperty = new IdentityProperty<>();
    Property<Blocks, Blocks> blockProperty = new IdentityProperty<>();

    Function<U, Boolean> validate = bindingCandidate -> {
      boolean success = validateBindingCandidate(context, bindingCandidate, model, childStack, id);
      if (success) {
        objectProperty.set(bindingCandidate);
      }
      return success;
    };

    synchronized (objectProperty) {
      Set<U> existingCandidates = bindings.getAndListen(model, objectCandidate -> {
        synchronized (objectProperty) {
          if (objectProperty.get() == null) {
            if (validate.apply(objectCandidate)) {
              objectProperty.set(objectCandidate);
              try {
                blockProperty.get().complete();
              } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
              }
              return false;
            } else {
              return true;
            }
          } else {
            return false;
          }
        }
      });

      /*
       * Check existing candidates to fulfill dependency
       */
      for (U objectCandidate : existingCandidates) {
        if (validate.apply(objectCandidate)) {
          return objectCandidate;
        }
      }

      /*
       * No existing candidates found, so block to wait for new ones
       */
      Blocks block = context.bindingBlocker().block(model.name(), id, !externalDependency);
      blockProperty.set(block);
    }

    return getProxiedBinding(model, blockProperty.get(), objectProperty::get);
  }

  private <U> boolean validateBindingCandidate(
      ProcessingContext context,
      U bindingCandidate,
      ComplexNode<U> model,
      List<ChildNode<?>> idNode,
      DataItem<?> id) {
    Objects.requireNonNull(bindingCandidate);

    DataSource candidateId = new BindingNodeUnbinder(context, model, bindingCandidate)
        .unbindToDataBuffer(idNode);

    if (candidateId.size() == 1) {
      DataItem<?> candidateData = candidateId.get();

      if (id.data(candidateData.type()).equals(candidateData.data())) {
        return true;
      }
    }

    return false;
  }

  public Stream<Provider> getProviders() {
    return Stream.of(
        Provider.over(ReferenceReader.class, referenceReader()),
        Provider.over(ImportReader.class, importReader()));
  }
}

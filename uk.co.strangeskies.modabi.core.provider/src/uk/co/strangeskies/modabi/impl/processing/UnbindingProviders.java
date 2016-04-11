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
import java.util.function.Function;

import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.Provisions;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.providers.ImportTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public class UnbindingProviders {
	public Function<ProcessingContext, IncludeTarget> includeTarget() {
		return context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, Collection<? extends U> objects) {
				for (U object : objects)
					context.bindings().add(model, object);

				context.output().ifPresent(o -> o.registerNamespaceHint(model.getName().getNamespace()));
			}
		};
	}

	public Function<ProcessingContext, ImportTarget> importTarget() {
		return context -> new ImportTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model, QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model.effective().children().stream()
						.filter(c -> c.getName().equals(idDomain) && c instanceof DataNode.Effective<?>).findAny().orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain + "' to target for model '" + model + "'"));

				return unbindDataNode(context.manager(), node, new TypedObject<>(model.getDataType(), object));
			}
		};
	}

	public Function<ProcessingContext, ReferenceTarget> referenceTarget() {
		return context -> new ReferenceTarget() {
			@Override
			public <U> DataSource reference(Model<U> model, QualifiedName idDomain, U object) {
				if (!context.bindings().get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object + "' bound to model '" + model.getName()
							+ "' from '" + context.bindings().get(model) + "'");

				return importTarget().apply(context).dereferenceImport(model, idDomain, object);
			}
		};
	}

	private <V> DataSource unbindDataNode(SchemaManager manager, DataNode.Effective<V> node, TypedObject<?> source) {
		ProcessingContextImpl unbindingContext = new ProcessingContextImpl(manager).withBindingObject(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}

	public void registerProviders(Provisions provisions) {
		provisions.add(Provider.over(new TypeToken<ReferenceTarget>() {}, referenceTarget()));
		provisions.add(Provider.over(new TypeToken<ImportTarget>() {}, importTarget()));
		provisions.add(Provider.over(new TypeToken<IncludeTarget>() {}, includeTarget()));
	}
}

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

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.processing.UnbindingContext;
import uk.co.strangeskies.modabi.processing.providers.ImportTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypedObject;

public class UnbindingProviders {
	private final SchemaManager manager;

	public UnbindingProviders(SchemaManager manager) {
		this.manager = manager;
	}

	public Function<UnbindingContext, IncludeTarget> includeTarget() {
		return context -> new IncludeTarget() {
			@Override
			public <U> void include(Model<U> model, Collection<? extends U> objects) {
				for (U object : objects)
					context.bindings().add(model, object);

				context.output().registerNamespaceHint(model.getName().getNamespace());
			}
		};
	}

	public Function<UnbindingContext, ImportTarget> importTarget() {
		return context -> new ImportTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model, QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model.effective().children().stream()
						.filter(c -> c.getName().equals(idDomain) && c instanceof DataNode.Effective<?>).findAny().orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain + "' to target for model '" + model + "'"));

				return unbindDataNode(node, new TypedObject<>(model.getDataType(), object));
			}
		};
	}

	public Function<UnbindingContext, ReferenceTarget> referenceTarget() {
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

	private <V> DataSource unbindDataNode(DataNode.Effective<V> node, TypedObject<?> source) {
		UnbindingContextImpl unbindingContext = new UnbindingContextImpl(manager).withUnbindingSource(source);

		return new DataNodeUnbinder(unbindingContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, unbindingContext));
	}
}
/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.schema.management.providers.impl;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.IncludeTarget;
import uk.co.strangeskies.modabi.schema.management.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.schema.management.providers.TypeComposer;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingContext;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.Model;

public class UnbindingProviders {
	private final BiFunction<DataNode.Effective<?>, Object, DataSource> unbindDataNode;

	public UnbindingProviders(
			BiFunction<DataNode.Effective<?>, Object, DataSource> unbindDataNode) {
		this.unbindDataNode = unbindDataNode;
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

	public Function<UnbindingContext, ImportReferenceTarget> importTarget() {
		return context -> new ImportReferenceTarget() {
			@Override
			public <U> DataSource dereferenceImport(Model<U> model,
					QualifiedName idDomain, U object) {
				DataNode.Effective<?> node = (DataNode.Effective<?>) model
						.effective()
						.children()
						.stream()
						.filter(
								c -> c.getName().equals(idDomain)
										&& c instanceof DataNode.Effective<?>)
						.findAny()
						.orElseThrow(
								() -> new SchemaException("Can't fine child '" + idDomain
										+ "' to target for model '" + model + "'."));

				return unbindDataNode.apply(node, object);
			}
		};
	}

	public Function<UnbindingContext, ReferenceTarget> referenceTarget() {
		return context -> new ReferenceTarget() {
			@Override
			public <U> DataSource dereference(Model<U> model, QualifiedName idDomain,
					U object) {
				if (!context.bindings().get(model).contains(object))
					throw new SchemaException("Cannot find any instance '" + object
							+ "' bound to model '" + model.getName() + "'.");

				return importTarget().apply(context).dereferenceImport(model, idDomain,
						object);
			}
		};
	}

	public Function<UnbindingContext, TypeComposer> typeComposer() {
		return context -> Object::toString;
	}
}

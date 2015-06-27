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
package uk.co.strangeskies.modabi.impl.processing;

import java.util.List;

import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.UnbindingContext;
import uk.co.strangeskies.modabi.processing.UnbindingException;
import uk.co.strangeskies.modabi.processing.providers.ImportReferenceTarget;
import uk.co.strangeskies.modabi.processing.providers.IncludeTarget;
import uk.co.strangeskies.modabi.processing.providers.ReferenceTarget;
import uk.co.strangeskies.modabi.processing.providers.TypeComposer;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public class SchemaUnbinder {
	private final UnbindingContextImpl context;

	public SchemaUnbinder(SchemaManager manager) {
		UnbindingProviders providers = new UnbindingProviders((n, s) -> unbindData(
				n, s));

		context = new UnbindingContextImpl(manager)
				.withProvision(new TypeToken<ReferenceTarget>() {},
						providers.referenceTarget())
				.withProvision(new TypeToken<ImportReferenceTarget>() {},
						providers.importTarget())
				.withProvision(new TypeToken<IncludeTarget>() {},
						providers.includeTarget())
				.withProvision(new TypeToken<TypeComposer>() {},
						providers.typeComposer())
				.withProvision(new TypeToken<UnbindingContext>() {}, c -> c);
	}

	public <U> DataSource unbindData(DataNode.Effective<U> node, Object source) {
		UnbindingContextImpl finalContext = context.withUnbindingSource(source);
		return new DataNodeUnbinder(finalContext).unbindToDataBuffer(node,
				BindingNodeUnbinder.getData(node, finalContext));
	}

	public <T> void unbind(Model.Effective<T> model, StructuredDataTarget output,
			T data) {
		UnbindingContextImpl context = this.context.withOutput(output);

		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			context.output().nextChild(model.getName());
			new BindingNodeUnbinder(context).unbind(model, data);
			context.output().endChild();
		} catch (UnbindingException e) {
			throw e;
		} catch (Exception e) {
			throw new UnbindingException("Unexpected problem during uninding of '"
					+ data + "' according to '" + model + "'", context, e);
		}
	}

	public <T> void unbind(StructuredDataTarget output,
			TypeToken<? extends T> dataClass, T data) {
		castingUnbind(output, dataClass, data);
	}

	@SuppressWarnings("unchecked")
	private <T, U extends T> void castingUnbind(StructuredDataTarget output,
			TypeToken<U> dataClass, T data) {
		UnbindingContextImpl context = this.context.withOutput(output);

		List<? extends Model.Effective<U>> models = context
				.getMatchingModels(dataClass);

		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
			c.output().registerDefaultNamespaceHint(m.getName().getNamespace());

			try {
				c.output().nextChild(m.getName());

				U castData = (U) data;

				new BindingNodeUnbinder(c).unbind(m, castData);
				c.output().endChild();
			} catch (UnbindingException e) {
				throw e;
			} catch (Exception e) {
				throw new UnbindingException("Unexpected problem during uninding.", c,
						e);
			}
		}, e -> new UnbindingException("Cannot unbind data '" + data
				+ "' of class '" + dataClass + "' with models '" + models + "'",
				context, e));
	}

	public <T> void unbind(StructuredDataTarget output, T data) {
		unbind(output, TypeToken.over(data.getClass()), data);
	}
}

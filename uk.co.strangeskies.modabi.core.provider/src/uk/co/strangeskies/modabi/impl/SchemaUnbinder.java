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

import java.util.List;

import uk.co.strangeskies.modabi.impl.processing.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.impl.processing.UnbindingContextImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.UnbindingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;

public class SchemaUnbinder {
	private final SchemaManagerImpl manager;

	public SchemaUnbinder(SchemaManagerImpl manager) {
		this.manager = manager;
	}

	public <T> void unbind(Model.Effective<T> model, StructuredDataTarget output, T data) {
		UnbindingContextImpl context = manager.getUnbindingContext().withOutput(output);

		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			context.output().addChild(model.getName());
			new BindingNodeUnbinder(context).unbind(model, data);
			context.output().endChild();
		} catch (UnbindingException e) {
			throw e;
		} catch (Exception e) {
			throw new UnbindingException("Unexpected problem during uninding of '" + data + "' according to '" + model + "'",
					context, e);
		}
	}

	public <T> void unbind(StructuredDataTarget output, TypeToken<? extends T> dataClass, T data) {
		castingUnbind(output, dataClass, data);
	}

	@SuppressWarnings("unchecked")
	private <T, U extends T> void castingUnbind(StructuredDataTarget output, TypeToken<U> dataClass, T data) {
		UnbindingContextImpl context = manager.getUnbindingContext().withOutput(output);

		List<? extends Model.Effective<U>> models = context.getMatchingModels(dataClass);

		if (models.isEmpty())
			throw new UnbindingException("Cannot find any model of type '" + dataClass + "' to unbind '" + data + "'",
					context);

		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
			c.output().registerDefaultNamespaceHint(m.getName().getNamespace());

			try {
				c.output().addChild(m.getName());

				U castData = (U) data;

				new BindingNodeUnbinder(c).unbind(m, castData);
				c.output().endChild();
			} catch (UnbindingException e) {
				throw e;
			} catch (Exception e) {
				throw new UnbindingException("Unexpected problem during uninding.", c, e);
			}
		}, e -> new UnbindingException(
				"Cannot unbind data '" + data + "' of class '" + dataClass + "' with models '" + models + "'", context, e));
	}

	public <T> void unbind(StructuredDataTarget output, T data) {
		unbind(output, TypeToken.over(data.getClass()), data);
	}
}

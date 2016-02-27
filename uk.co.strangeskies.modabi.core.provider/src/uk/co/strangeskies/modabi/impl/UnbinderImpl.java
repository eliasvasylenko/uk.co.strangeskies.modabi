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

import java.io.OutputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Unbinder;
import uk.co.strangeskies.modabi.impl.processing.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.impl.processing.UnbindingContextImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.UnbindingContext;
import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.Model;

public class UnbinderImpl<T> implements Unbinder<T> {
	private final SchemaManagerImpl manager;
	private final T data;

	private final Function<UnbindingContext, List<Model.Effective<T>>> unbindingFunction;

	public UnbinderImpl(SchemaManagerImpl manager, T data,
			Function<UnbindingContext, List<Model.Effective<T>>> unbindingFunction) {
		this.manager = manager;
		this.data = data;
		this.unbindingFunction = unbindingFunction;
	}

	@Override
	public BindingFuture<T> to(String extension, OutputStream output) {
		to(manager.dataFormats().getDataFormat(extension).saveData(output));

		return null;
	}

	@Override
	public <U extends StructuredDataTarget> U to(U output) {
		UnbindingContextImpl context = manager.getUnbindingContext().withOutput(output);

		List<? extends Model.Effective<? extends T>> models = unbindingFunction.apply(context);

		if (models.isEmpty()) {
			throw new BindingException("Cannot find any model to unbind '" + data + "'", context);
		}

		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
			unbindImpl(c, m, output);
		}, e -> new BindingException("Cannot unbind data '" + data + "' with models '" + models + "'", context, e));

		return output;
	}

	@SuppressWarnings("unchecked")
	private <U extends T> void unbindImpl(UnbindingContextImpl context, Model.Effective<U> model,
			StructuredDataTarget output) {
		output.registerDefaultNamespaceHint(model.getName().getNamespace());

		try {
			context.output().addChild(model.getName());

			new BindingNodeUnbinder(context).unbind(model, (U) data);

			context.output().endChild();
		} catch (BindingException e) {
			throw e;
		} catch (Exception e) {
			throw new BindingException("Unexpected problem during uninding of '" + data + "' according to '" + model + "'",
					context, e);
		}
	}

	@Override
	public Unbinder<T> with(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}
}

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.DataFormats;
import uk.co.strangeskies.modabi.OutputBinder;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.impl.processing.BindingNodeUnbinder;
import uk.co.strangeskies.modabi.impl.processing.ProcessingContextImpl;
import uk.co.strangeskies.modabi.io.structured.StructuredDataFormat;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;
import uk.co.strangeskies.utilities.classpath.ManifestUtilities;
import uk.co.strangeskies.utilities.function.ThrowingSupplier;

public class OutputBinderImpl<T> implements OutputBinder<T> {
	private final ProcessingContextImpl context;
	private final DataFormats formats;
	private final T data;

	private final Supplier<List<Model.Effective<? super T>>> unbindingFunction;

	private final Set<Provider> providers;
	private ClassLoader classLoader;

	protected OutputBinderImpl(ProcessingContextImpl context, DataFormats formats, T data,
			Supplier<List<Model.Effective<? super T>>> unbindingFunction) {
		this.context = context;
		this.formats = formats;
		this.data = data;
		this.unbindingFunction = unbindingFunction;

		providers = new HashSet<>();
	}

	@SuppressWarnings("unchecked")
	public static <T> OutputBinder<T> bind(ProcessingContextImpl context, DataFormats formats, T data) {
		return new OutputBinderImpl<>(context, formats, data, null).with((Class<T>) data.getClass());
	}

	protected OutputBinder<T> with(Supplier<List<Model.Effective<? super T>>> unbindingFunction) {
		return new OutputBinderImpl<>(context, formats, data, unbindingFunction);
	}

	@Override
	public OutputBinder<T> with(TypeToken<? super T> dataType) {
		return with(() -> context.registeredModels().getModelsWithType(dataType).stream().map(n -> n.effective())
				.collect(Collectors.toList()));
	}

	@Override
	public OutputBinder<T> with(Model<? super T> model) {
		return with(() -> Arrays.asList(model.effective()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public OutputBinder<T> with(QualifiedName modelName) {
		return with(modelName, (Class<T>) data.getClass());
	}

	@Override
	public OutputBinder<T> with(QualifiedName modelName, TypeToken<? super T> type) {
		return with(() -> Arrays.asList(context.registeredModels().get(modelName, type).effective()));
	}

	@Override
	public BindingFuture<T> to(File output) {
		return toResource(output.getName(), () -> new FileOutputStream(output));
	}

	@Override
	public BindingFuture<T> to(URL output) {
		try {
			return toResource(output.getQuery(), output.openConnection()::getOutputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public BindingFuture<T> toResource(String resourceName, ThrowingSupplier<OutputStream, ?> output) {
		String extension = ManifestUtilities.getResourceExtension(resourceName);

		if (extension == null) {
			throw new ProcessingException(t -> t.noFormatFoundFor(resourceName), prepareContext());
		}

		return to(extension, output);
	}

	@Override
	public BindingFuture<T> to(String extension, ThrowingSupplier<OutputStream, ?> output) {
		/*
		 * TODO blocking waitandget???
		 */
		StructuredDataFormat format = formats.get(extension);

		if (format == null) {
			throw new ProcessingException(t -> t.noFormatFoundFor(extension), prepareContext());
		}

		try (OutputStream stream = output.get()) {
			to(format.saveData(stream));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	@Override
	public <U extends StructuredDataTarget> U to(U output) {
		ProcessingContextImpl context = prepareContext();
		context = context.withOutput(output);

		List<? extends Model.Effective<? super T>> models = unbindingFunction.get();

		if (models.isEmpty()) {
			throw new ProcessingException("Cannot find any model to unbind '" + data + "'", context);
		}

		ProcessingContextImpl finalContext = context;
		context.attemptUnbindingUntilSuccessful(models, (c, m) -> {
			unbindImpl(c, m, output);
		}, e -> new ProcessingException("Cannot unbind data '" + data + "' with models '" + models + "'", finalContext, e));

		return output;
	}

	private ProcessingContextImpl prepareContext() {
		ProcessingContextImpl context = this.context.withNestedProvisionScope();
		context.provisions().addAll(providers);

		return context;
	}

	@SuppressWarnings("unchecked")
	private <U extends T> void unbindImpl(ProcessingContext context, Model.Effective<? super U> model,
			StructuredDataTarget output) {
		output.registerDefaultNamespaceHint(model.name().getNamespace());

		try {
			context.output().get().addChild(model.name());

			ClassLoader classLoader = this.classLoader != null ? this.classLoader
					: Thread.currentThread().getContextClassLoader();

			new ContextClassLoaderRunner(classLoader).run(() -> new BindingNodeUnbinder(context).unbind(model, (U) data));

			context.output().get().endChild();
		} catch (ProcessingException e) {
			throw e;
		} catch (Exception e) {
			throw new ProcessingException(t -> t.unexpectedProblemProcessing(data, model), context, e);
		}
	}

	@Override
	public OutputBinder<T> withClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	@Override
	public OutputBinder<T> withProvider(Provider provider) {
		providers.add(provider);
		return this;
	}

	@Override
	public OutputBinder<T> withErrorHandler(Consumer<Exception> errorHandler) {
		// TODO Auto-generated method stub
		return null;
	}
}

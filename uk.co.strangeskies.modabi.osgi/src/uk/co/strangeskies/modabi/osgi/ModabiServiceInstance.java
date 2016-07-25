/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.osgi.
 *
 * uk.co.strangeskies.modabi.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.osgi.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.osgi;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.InputBinder;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Provider;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ModabiServiceInstance<T> {
	@Reference
	SchemaManager manager;

	private final TypeToken<T> type;
	private final URL location;
	private final QualifiedName model;

	public ModabiServiceInstance(URL location) {
		this(location, null);
	}

	public ModabiServiceInstance(String name, String extension) {
		this(name, extension, null);
	}

	public ModabiServiceInstance(String name, String extension, QualifiedName schema) {
		String resourceLocation = getClass().getPackage().getName().replaceAll(".", "/") + '/';
		this.location = getClass().getResource(resourceLocation + name + '.' + extension);

		this.model = schema;

		type = findType();
	}

	public ModabiServiceInstance(URL location, QualifiedName schema) {
		this.location = location;
		this.model = schema;

		type = findType();
	}

	private TypeToken<T> findType() {
		TypeToken<T> type = TypeToken.over(getClass()).resolveSupertypeParameters(ModabiServiceInstance.class)
				.resolveType(new TypeParameter<T>() {});

		if (!type.isProper()) {
			throw new ModabiException(
					"Class " + getClass() + " cannot be bound with service, as type parameter of superclass "
							+ ModabiServiceInstance.class + " is not proper");
		}

		if (!type.isAssignableFrom(getClass())) {
			throw new ModabiException("Type " + type + " must be assignable from providing service class " + getClass());
		}

		return type;
	}

	@SuppressWarnings("unchecked")
	@Activate
	public void initialise() {
		SchemaManager manager = this.manager;
		this.manager = null;

		InputBinder<T> binder = (model != null)

				? manager.bindInput().with(model, type)

				: manager.bindInput().with(type);

		BindingFuture<T> future = binder
				.withProvider(Provider.over(type, c -> c.getBindingNodeStack().size() == 1 ? (T) this : null)).from(location);
		T binding = future.resolve();

		if (binding != this) {
			try {
				throw new ModabiException(
						"Could not bind to provided instance '" + this + "' for model '" + future.getModelFuture().get() + "'");
			} catch (InterruptedException | ExecutionException e) {
				throw new ModabiException("Could not bind to provided instance '" + this + "'");
			}
		}
	}
}

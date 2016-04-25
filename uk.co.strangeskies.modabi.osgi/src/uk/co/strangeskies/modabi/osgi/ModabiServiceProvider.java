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
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.Binder;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public abstract class ModabiServiceProvider<T> {
	@Reference
	SchemaManager manager;

	private final TypeToken<T> type;
	private final URL location;
	private final QualifiedName schema;
	private final Hashtable<String, ?> serviceProperties;

	public ModabiServiceProvider(URL location) {
		this(location, null);
	}

	public ModabiServiceProvider(URL location, QualifiedName schema) {
		this(location, schema, new Hashtable<>());
	}

	public ModabiServiceProvider(URL location, QualifiedName schema, Hashtable<String, ?> serviceProperties) {
		this.location = location;
		this.schema = schema;
		this.serviceProperties = serviceProperties;

		type = TypeToken.over(getClass()).resolveSupertypeParameters(ModabiServiceInstance.class)
				.resolveType(new TypeParameter<T>() {});

		if (!type.isProper()) {
			throw new SchemaException(
					"Class " + getClass() + " cannot be bound with service, as type parameter of superclass "
							+ ModabiServiceInstance.class + " is not proper");
		}
	}

	@Activate
	public void initialise(BundleContext context) {
		SchemaManager manager = this.manager;
		this.manager = null;

		Binder<T> binder = (schema != null)

				? manager.bind(schema, type)

				: manager.bind(type);

		T service = binder.from(location).resolve();

		context.registerService(type.getRawType(), service, serviceProperties);
	}
}

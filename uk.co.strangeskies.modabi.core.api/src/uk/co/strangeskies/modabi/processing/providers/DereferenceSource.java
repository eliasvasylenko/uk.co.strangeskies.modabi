/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.processing.providers;

import java.util.Objects;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.Model;

public interface DereferenceSource {
	<T> T dereference(Model<T> model, QualifiedName idDomain, DataSource id);

	/*
	 * TODO if we restructure like the following, the supplier can be bound at
	 * registration time, which means the type will be known during registration
	 * and we can have better type safety, maybe!
	 */
	default <T> Function<DataSource, T> dereference(Model<T> model, QualifiedName idDomain) {
		Objects.requireNonNull(model);
		Objects.requireNonNull(idDomain);
		return id -> dereference(model, idDomain, id);
	}

	default <T> Function<QualifiedName, Function<DataSource, T>> dereference(Model<T> model) {
		return idDomain -> dereference(model, idDomain);
	}
}

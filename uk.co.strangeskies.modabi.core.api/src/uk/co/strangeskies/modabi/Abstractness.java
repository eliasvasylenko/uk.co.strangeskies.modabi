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
package uk.co.strangeskies.modabi;

public enum Abstractness {
	/**
	 * The type is inferred, all input / output / binding / unbinding methods are
	 * resolved, and the unspecified properties of the type are instantiated with
	 * defaults.
	 */
	CONCRETE,

	/**
	 * The type is inferred, all input / output / binding / unbinding methods are
	 * resolved, and the unspecified properties of the type are instantiated with
	 * defaults.
	 */
	UNINFERRED,

	EXTENSIBLE,

	/**
	 * As with {@link #ABSTRACT}, except input / output methods are resolved
	 */
	RESOLVED,

	/**
	 * The node is completely abstract,
	 */
	ABSTRACT;

	public boolean isAtLeast(Abstractness abstractness) {
		return this.ordinal() >= abstractness.ordinal();
	}

	public boolean isAtMost(Abstractness abstractness) {
		return this.ordinal() <= abstractness.ordinal();
	}

	public boolean isLessThan(Abstractness abstractness) {
		return this.ordinal() < abstractness.ordinal();
	}

	public boolean isMoreThan(Abstractness abstractness) {
		return this.ordinal() > abstractness.ordinal();
	}
}

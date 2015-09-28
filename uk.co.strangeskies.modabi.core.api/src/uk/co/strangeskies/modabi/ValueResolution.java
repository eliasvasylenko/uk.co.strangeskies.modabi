/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

public enum ValueResolution {
	/**
	 * Delay resolution of the provided value buffer until the schema node is
	 * processed as part of its binding, then bind individually for each bound
	 * instance.
	 */
	PROCESSING_TIME,

	/**
	 * Resolve the provided value buffer during initial registration of this node
	 * as part of its containing schema.
	 */
	REGISTRATION_TIME,

	/**
	 * Create a proxy instance for the resolved value during initial registration
	 * of this node as part of its containing schema. Resolution will then be
	 * performed as soon as any attempt is made to invoke methods on this proxy.
	 */
	POST_REGISTRATION

	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * TODO:
	 * 
	 * create special "providedType" node under "complex" node in MetaSchema which
	 * expects a REGISTRATION_TIME provided value. Use this to perform more
	 * complex type checking/inference for complex nodes upon build &
	 * registration.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
}

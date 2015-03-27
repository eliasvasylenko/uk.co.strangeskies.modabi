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
package uk.co.strangeskies.modabi.schema.management.unbinding;

public enum UnbindingStrategy {
	/**
	 * The schema binder should attempt to retrieve an reference to an instance of
	 * the data class directly from the result of the output method. No unbinding
	 * class or unbinding method should be specified. This is the default
	 * unbinding behaviour.
	 */
	SIMPLE,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The provided object should be passed as a parameter to the unbinding
	 * method, invoked on the object being unbound through the output method.
	 * 
	 * Any return value from the unbinding method will be ignored, and the
	 * provided object will be used as the output target for child nodes.
	 */
	ACCEPT_PROVIDED,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The object which is being unbound through the output method should be
	 * passed as a parameter to the unbinding method, invoked on the provided
	 * instance of the unbinding class.
	 * 
	 * Any return value from the unbinding method will be ignored, and the
	 * provided object will be used as the output target for child nodes.
	 */
	PASS_TO_PROVIDED,

	/**
	 * The schema binder should attempt to find an implementation of the unbinding
	 * class for which a factory has been provided externally, for example
	 * programmatically, through dependency injection, or as an OSGI service.
	 *
	 * The object which is being unbound through the output method should be
	 * passed as a parameter to the unbinding method, invoked on the provided
	 * instance of the unbinding class.
	 * 
	 * The return value from the unbinding method will be used as the output
	 * target for child nodes, and should be of the type of the unbinding class,
	 * if given, else the data class.
	 */
	PROVIDED_FACTORY,

	/**
	 * The schema binder should attempt to find the static factory method to call
	 * on the requested unbinding class named by the unbinding method. The object
	 * being unbound should be passed to the factory method as a parameter.
	 * 
	 * The class of the static method should be provided through
	 * staticFactoryClass, else is assumed to be the unbinding class.
	 */
	STATIC_FACTORY,

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested unbinding class. The object being unbound should be passed to the
	 * constructor as a parameter. No unbinding method should be specified.
	 * 
	 * The constructed object will be used as the output target for child nodes.
	 */
	CONSTRUCTOR;
}

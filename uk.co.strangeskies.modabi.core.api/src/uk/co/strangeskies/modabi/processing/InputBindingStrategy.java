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
package uk.co.strangeskies.modabi.processing;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.schema.BindingChildNode;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.modabi.schema.InputSequenceNode;

/**
 * This enumeration describes the different ways a {@link Schema} can request a
 * {@link SchemaManager} should provide implementations of classes and
 * interfaces to bind to. All binding strategies can be applied when binding to
 * concrete classes, abstract classes, or interfaces, unless otherwise
 * specified.
 *
 * @author Elias N Vasylenko
 *
 */
public enum InputBindingStrategy {
	/**
	 * The schema binder should attempt to find an implementation for which a
	 * factory has been provided externally, for example programmatically, through
	 * dependency injection, or as an OSGI service. This is the default binding
	 * behavior.
	 */
	PROVIDED,

	/**
	 * The schema binder should attempt to create a simple proxy implementation of
	 * an interface.
	 * <p>
	 * This binding strategy is only valid when binding to interfaces.
	 */
	IMPLEMENT_IN_PLACE,

	/**
	 * The schema binder should attempt to find a constructor to call on the
	 * requested class.
	 * <p>
	 * The arguments passed to the constructor will be determined by way of the
	 * first child node which is an input node, and which has an
	 * {@link InputNode.Effective#getInMethod() in method} other than an explicit
	 * {@code "void"}. The only valid in method string for this node is
	 * {@code "this"}, though it may be omitted. No input may be bound to the
	 * target by any node, or the child of any node, appearing before this one.
	 * <p>
	 * This node may be an {@link InputSequenceNode input sequence}, in which case
	 * any data its children bind will be passed to the constructor as parameters
	 * in the order of those nodes. In the case of a {@link BindingChildNode
	 * binding node}, the bound value will be passed to a single argument
	 * constructor of that type.
	 * <p>
	 * The constructor will be resolved as per the normal java overload resolution
	 * rules, given the determined arguments.
	 * <p>
	 * This binding strategy is only valid when binding to concrete classes.
	 */
	CONSTRUCTOR,

	/**
	 * The schema binder should attempt to find a static factory method to call on
	 * the requested binding class.
	 * <p>
	 * The arguments passed to the method will be determined by way of the first
	 * child node which is an input node, and which has an
	 * {@link InputNode.Effective#getInMethod() in method} other than an explicit
	 * {@code "void"}. No input may be bound to the target by any node, or the
	 * child of any node, appearing before this one.
	 * <p>
	 * This node may be an {@link InputSequenceNode input sequence}, in which case
	 * any data its children bind will be passed to the method as parameters in
	 * the order of those nodes. In the case of a {@link BindingChildNode binding
	 * node}, the bound value will be passed to a single argument method of that
	 * type.
	 * <p>
	 * The static method will be resolved as per the normal java overload
	 * resolution rules, given the determined arguments.
	 */
	STATIC_FACTORY,

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the result of the first of any child nodes. This child
	 * must be an input node binding to a single class. No in method name should
	 * be specified on this child node.
	 */
	SOURCE_ADAPTOR,

	/**
	 * The schema binder should attempt to retrieve an implementation of the
	 * requested class from the object being bound by the parent node. This is
	 * useful when the parent object is a factory implementation for the child
	 * object being bound. It is possible that objects produced from the parent
	 * don't need to be 'added' after, so it is optional to omit the'inMethod'
	 * property by setting it to 'void'.
	 * <p>
	 * If a data node binds with this strategy and is set to resolve a provided
	 * value at registration time, the object being bound by the parent node
	 * during processing will not be available yet. In this case, an instance of
	 * the node being bound to will be provided.
	 */
	TARGET_ADAPTOR;
}

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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlocks;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.Methods;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

public interface ModabiProperties extends Properties<ModabiProperties> {
	enum ExecutableType {
		METHOD, STATIC_METHOD, CONSTRUCTOR
	}

	Localized<String> noTypeFoundForType(QualifiedName dataType, Type type);

	Localized<String> noModelFoundForType(QualifiedName model, Type type);

	Localized<String> unexpectedNodeType();

	Localized<String> missingDependencies(BindingBlocks blockingBindings);

	Localized<String> missingDependencies(Collection<? extends BindingFuture<?>> futures,
			Collection<? extends BindingBlock> missingDependencies);

	Localized<String> cannotProvideSingleValue(QualifiedName name, Range<Integer> occurrences);

	default Localized<String> noChildFound(List<QualifiedName> child, QualifiedName parent,
			List<? extends ChildNode<?>> children) {
		return noChildFound(child, parent,
				children.stream().map(SchemaNode::name).map(Objects::toString).collect(Collectors.joining(", ")));
	}

	Localized<String> noChildFound(List<QualifiedName> child, QualifiedName parent, String children);

	Localized<String> invalidDataSource(Object source);

	Localized<String> incompatibleTypes(Type dataClass, Type dataClass2);

	Localized<String> invalidNamespace(String namespace);

	Localized<String> unknownBlockingError(BindingBlock block);

	Localized<String> unresolvableDependencies(Collection<? extends BindingBlock> values);

	Localized<String> cancelled(BindingFuture<?> binding);

	Localized<String> noBootstrapModelFound(QualifiedName name);

	Localized<String> noBootstrapValueFound(QualifiedName name);

	/*
	 * Property overriding
	 */
	default <N extends SchemaNode<?>> String getPropertyName(Consumer<? super N> propertyGetter, Class<N> node) {
		return "#" + Methods.findMethod(node, propertyGetter).getName();
	}

	default <N extends SchemaNode<?>, T> Localized<String> cannotOverrideIncompatibleProperty(
			Consumer<? super N> propertyGetter, Class<N> node, T base, T override) {
		return cannotOverrideIncompatibleProperty(getPropertyName(propertyGetter, node), node, base, override);
	}

	<T> Localized<String> cannotOverrideIncompatibleProperty(String propertyName, Object target, T base, T override);

	default <N extends SchemaNode<?>> Localized<String> cannotMergeIncompatibleProperties(
			Consumer<? super N> propertyGetter, Class<N> node, Collection<?> values) {
		return cannotMergeIncompatibleProperties(getPropertyName(propertyGetter, node), node, values);
	}

	Localized<String> cannotMergeIncompatibleProperties(String propertyName, Object target, Collection<?> values);

	default <N extends SchemaNode<?>> Localized<String> mustOverrideIncompatibleProperties(
			Consumer<? super N> propertyGetter, Class<N> node, Collection<?> values) {
		return mustOverrideIncompatibleProperties(getPropertyName(propertyGetter, node), node, values);
	}

	Localized<String> mustOverrideIncompatibleProperties(String propertyName, Object target, Collection<?> values);

	default <N extends SchemaNode<?>> Localized<String> mustProvideValueForNonAbstract(Consumer<? super N> propertyGetter,
			Class<N> node) {
		return mustProvideValueForNonAbstract(getPropertyName(propertyGetter, node), node);
	}

	Localized<String> mustProvideValueForNonAbstract(String propertyName, Object target);

	Localized<String> unexpectedOverrideError(BindingNode<?, ?> base);

	/*
	 * Executables
	 */
	default Localized<String> executableType(Executable executable) {
		return executableType((executable instanceof Constructor<?>) ? ExecutableType.CONSTRUCTOR
				: ((Modifier.isStatic(executable.getModifiers()) ? ExecutableType.STATIC_METHOD : ExecutableType.METHOD)));
	}

	default Localized<String> executableType(ExecutableType type) {
		switch (type) {
		case CONSTRUCTOR:
			return executableTypeConstructor();
		case METHOD:
			return executableTypeMethod();
		case STATIC_METHOD:
			return executableTypeStaticMethod();
		}
		throw new AssertionError();
	}

	Localized<String> executableTypeStaticMethod();

	Localized<String> executableTypeMethod();

	Localized<String> executableTypeConstructor();

	Localized<String> noMethodCandidatesFoundForNames(Collection<String> names);

	default Localized<String> noMethodFound(TypeToken<?> receiver, List<TypeToken<?>> parameters, ExecutableType type) {
		return noMethodFound(executableType(type), receiver, parameters);
	}

	Localized<String> noMethodFound(Localized<String> type, TypeToken<?> receiver, List<TypeToken<?>> parameters);

	default Localized<String> inMethodMustBeChained(QualifiedName name, ExecutableType type) {
		return inMethodMustBeChained(executableType(type), name);
	}

	Localized<String> inMethodMustBeChained(Localized<String> type, QualifiedName name);

	/*
	 * Schema
	 */
	Localized<String> mustOverrideMultiplyInherited(QualifiedName overrideGroup);

	Localized<String> cannotAddInheritedNodeWhenOverridden(QualifiedName overrideGroup);

	Localized<String> cannotOverrideNodeWhenOverridden(QualifiedName overrideGroup);

	default Localized<String> mustOverrideAbstractNode(QualifiedName abstractNode, QualifiedName beforeThisNode) {
		return (beforeThisNode == null) ? mustOverrideAbstractNode(abstractNode)
				: mustOverrideAbstractNodeBefore(abstractNode, beforeThisNode);
	}

	Localized<String> mustOverrideAbstractNode(QualifiedName abstractNode);

	Localized<String> mustOverrideAbstractNodeBefore(QualifiedName abstractNode, QualifiedName beforeThisNode);

	Localized<String> cannotAddChild();

	Localized<String> cannotOverrideNodeWithClass(QualifiedName name, Class<?> nodeClass, Class<?> overrideClass);

	Localized<String> cannotOverrideNodeOutOfOrder(QualifiedName name, List<QualifiedName> nodesSoFar);

	Localized<String> mustOverrideDescendant(Collection<? extends SchemaNode<?>> nodeStack);

	Localized<String> cannotInvokeOnProxyNode(Method method, QualifiedName node);

	Localized<String> cannotDefineInputInContext(QualifiedName name);

	Localized<String> inMethodMustBeThis();

	Localized<String> cannotAcceptFormat(QualifiedName name);

	Localized<String> cannotBeInlineExtensible(QualifiedName name);

	Localized<String> cannotBeAbstract(SchemaNode<?> node);

	Localized<String> cannotFindOutMethodWithoutResultType(SchemaNode<?> node);

	Localized<String> cannotFindOutMethodWithoutTargetType(SchemaNode<?> node);

	Localized<String> cannotInferDataType(SchemaNode<?> effective, TypeToken<?> exactDataType);

	Localized<String> cannotFindUnbindingParameter(QualifiedName p);

	Localized<String> unbindingParameterMustBeDataNode(ChildNode<?> node, QualifiedName p);

	Localized<String> unbindingParameterMustOccurOnce(ChildNode<?> effective, QualifiedName p);

	Localized<String> unbindingParameterMustProvideValue(ChildNode<?> effective, QualifiedName p);
}

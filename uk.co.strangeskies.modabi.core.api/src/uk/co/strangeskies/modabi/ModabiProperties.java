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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.processing.BindingBlock;
import uk.co.strangeskies.modabi.processing.BindingBlocks;
import uk.co.strangeskies.modabi.processing.BindingFuture;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".", key = "%3$s")
public interface ModabiProperties {
	Localized<String> noTypeFoundForType(QualifiedName dataType, Type type);

	Localized<String> noModelFoundForType(QualifiedName model, Type type);

	Localized<String> unexpectedNodeType();

	Localized<String> missingDependencies(BindingBlocks blockingBindings);

	Localized<String> missingDependencies(
			Collection<? extends BindingFuture<?>> futures,
			Collection<? extends BindingBlock> missingDependencies);

	Localized<String> cannotProvideSingleValue(QualifiedName name, int valueCount);

	default Localized<String> noChildFound(
			List<QualifiedName> child,
			QualifiedName parent,
			List<? extends ChildBindingPoint<?>> children) {
		return noChildFound(
				child,
				parent,
				children.stream().map(BindingPoint::name).map(Objects::toString).collect(
						Collectors.joining(", ")));
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
	<T> Localized<String> cannotOverrideIncompatibleProperty(String propertyName, T base, T override);

	Localized<String> cannotMergeIncompatibleProperties(String propertyName, Collection<?> values);

	Localized<String> mustOverrideIncompatibleProperties(String propertyName, Collection<?> values);

	Localized<String> mustProvideValueForNonAbstract(String propertyName);

	Localized<String> unexpectedOverrideError(SchemaNode<?> base);

	Localized<String> executableTypeStaticMethod();

	Localized<String> executableTypeMethod();

	Localized<String> executableTypeConstructor();

	Localized<String> noMethodCandidatesFoundForNames(Collection<String> names);

	Localized<String> noConstructorFound(TypeToken<?> receiver, List<TypeToken<?>> parameters);

	Localized<String> noMethodFound(TypeToken<?> receiver, List<TypeToken<?>> parameters);

	Localized<String> inMethodMustBeChained(QualifiedName name);

	/*
	 * Schema
	 */
	Localized<String> mustOverrideMultiplyInherited(QualifiedName overrideGroup);

	Localized<String> cannotAddInheritedNodeWhenOverridden(QualifiedName overrideGroup);

	Localized<String> cannotOverrideNodeWhenOverridden(QualifiedName overrideGroup);

	default Localized<String> mustOverrideAbstractNode(
			QualifiedName abstractNode,
			QualifiedName beforeThisNode) {
		return (beforeThisNode == null) ? mustOverrideAbstractNode(abstractNode)
				: mustOverrideAbstractNodeBefore(abstractNode, beforeThisNode);
	}

	Localized<String> mustOverrideAbstractNode(QualifiedName abstractNode);

	Localized<String> mustOverrideAbstractNodeBefore(
			QualifiedName abstractNode,
			QualifiedName beforeThisNode);

	Localized<String> cannotAddChild();

	Localized<String> cannotOverrideNodeWithClass(
			QualifiedName name,
			Class<?> nodeClass,
			Class<?> overrideClass);

	Localized<String> cannotOverrideNodeOutOfOrder(
			QualifiedName name,
			List<QualifiedName> nodesSoFar);

	Localized<String> mustOverrideDescendant(Collection<? extends BindingPoint<?>> nodeStack);

	Localized<String> cannotInvokeOnProxyNode(Method method, QualifiedName node);

	Localized<String> cannotDefineInputInContext(QualifiedName name);

	Localized<String> cannotAcceptFormat(QualifiedName name);

	Localized<String> cannotBeInlineExtensible(QualifiedName name);

	Localized<String> cannotBeAbstract(SchemaNode<?> node);

	Localized<String> cannotFindOutMethodWithoutResultType(SchemaNode<?> node);

	Localized<String> cannotFindOutMethodWithoutTargetType(SchemaNode<?> node);

	Localized<String> cannotInferDataType(SchemaNode<?> effective, TypeToken<?> exactDataType);

	Localized<String> cannotFindUnbindingParameter(QualifiedName p);

	Localized<String> unbindingParameterMustBeDataNode(SchemaNode<?> node, QualifiedName p);

	Localized<String> unbindingParameterMustOccurOnce(SchemaNode<?> effective, QualifiedName p);

	Localized<String> unbindingParameterMustProvideValue(SchemaNode<?> effective, QualifiedName p);
}

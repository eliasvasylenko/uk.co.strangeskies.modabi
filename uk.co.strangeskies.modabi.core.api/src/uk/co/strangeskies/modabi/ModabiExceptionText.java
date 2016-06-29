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
import uk.co.strangeskies.utilities.text.AppendToLocalizationKey;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

public interface ModabiExceptionText extends LocalizedText<ModabiExceptionText> {
	enum ExecutableType {
		METHOD, STATIC_METHOD, CONSTRUCTOR
	}

	LocalizedString noTypeFoundForType(QualifiedName dataType, Type type);

	LocalizedString noModelFoundForType(QualifiedName model, Type type);

	LocalizedString unexpectedNodeType();

	LocalizedString missingDependencies(BindingBlocks blockingBindings);

	LocalizedString missingDependencies(Collection<? extends BindingFuture<?>> futures,
			Collection<? extends BindingBlock> missingDependencies);

	LocalizedString cannotProvideSingleValue(QualifiedName name, Range<Integer> occurrences);

	default LocalizedString noChildFound(List<QualifiedName> child, QualifiedName parent,
			List<? extends ChildNode<?, ?>> children) {
		return noChildFound(child, parent,
				children.stream().map(SchemaNode::name).map(Objects::toString).collect(Collectors.joining(", ")));
	}

	LocalizedString noChildFound(List<QualifiedName> child, QualifiedName parent, String children);

	LocalizedString invalidDataSource(Object source);

	LocalizedString incompatibleTypes(Type dataClass, Type dataClass2);

	LocalizedString invalidNamespace(String namespace);

	LocalizedString unknownBlockingError(BindingBlock block);

	LocalizedString unresolvableDependencies(Collection<? extends BindingBlock> values);

	LocalizedString cancelled(BindingFuture<?> binding);

	LocalizedString noBootstrapModelFound(QualifiedName name);

	LocalizedString noBootstrapValueFound(QualifiedName name);

	/*
	 * Property overriding
	 */
	@SuppressWarnings("unchecked")
	default <N extends SchemaNode<?, ?>> String getPropertyName(Consumer<? super N> propertyGetter, N node) {
		return "#" + Methods.findMethod((Class<N>) node.getThisType().getRawType(), propertyGetter).getName();
	}

	default <N extends SchemaNode<?, ?>, T> LocalizedString cannotOverrideIncompatibleProperty(
			Consumer<? super N> propertyGetter, N node, T base, T override) {
		return cannotOverrideIncompatibleProperty(getPropertyName(propertyGetter, node), node, base, override);
	}

	<T> LocalizedString cannotOverrideIncompatibleProperty(String propertyName, Object target, T base, T override);

	default <N extends SchemaNode<?, ?>> LocalizedString cannotMergeIncompatibleProperties(
			Consumer<? super N> propertyGetter, N node, Collection<?> values) {
		return cannotMergeIncompatibleProperties(getPropertyName(propertyGetter, node), node, values);
	}

	LocalizedString cannotMergeIncompatibleProperties(String propertyName, Object target, Collection<?> values);

	default <N extends SchemaNode<?, ?>> LocalizedString mustOverrideIncompatibleProperties(
			Consumer<? super N> propertyGetter, N node, Collection<?> values) {
		return mustOverrideIncompatibleProperties(getPropertyName(propertyGetter, node), node, values);
	}

	LocalizedString mustOverrideIncompatibleProperties(String propertyName, Object target, Collection<?> values);

	default <N extends SchemaNode<?, ?>> LocalizedString mustProvideValueForNonAbstract(
			Consumer<? super N> propertyGetter, N node) {
		return mustProvideValueForNonAbstract(getPropertyName(propertyGetter, node), node);
	}

	LocalizedString mustProvideValueForNonAbstract(String propertyName, Object target);

	LocalizedString unexpectedOverrideError(BindingNode<?, ?, ?> base);

	/*
	 * Executables
	 */
	default LocalizedString executableType(Executable executable) {
		return executableType((executable instanceof Constructor<?>) ? ExecutableType.CONSTRUCTOR
				: ((Modifier.isStatic(executable.getModifiers()) ? ExecutableType.STATIC_METHOD : ExecutableType.METHOD)));
	}

	LocalizedString executableType(@AppendToLocalizationKey ExecutableType type);

	LocalizedString noMethodCandidatesFoundForNames(Collection<String> names);

	default LocalizedString noMethodFound(TypeToken<?> receiver, List<TypeToken<?>> parameters, ExecutableType type) {
		return noMethodFound(executableType(type), receiver, parameters);
	}

	LocalizedString noMethodFound(LocalizedString type, TypeToken<?> receiver, List<TypeToken<?>> parameters);

	default LocalizedString inMethodMustBeChained(QualifiedName name, ExecutableType type) {
		return inMethodMustBeChained(executableType(type), name);
	}

	LocalizedString inMethodMustBeChained(LocalizedString type, QualifiedName name);

	/*
	 * Schema
	 */
	LocalizedString mustOverrideMultiplyInherited(QualifiedName overrideGroup);

	LocalizedString cannotAddInheritedNodeWhenOverridden(QualifiedName overrideGroup);

	LocalizedString cannotOverrideNodeWhenOverridden(QualifiedName overrideGroup);

	default LocalizedString mustOverrideAbstractNode(QualifiedName abstractNode, QualifiedName beforeThisNode) {
		return (beforeThisNode == null) ? mustOverrideAbstractNode(abstractNode)
				: mustOverrideAbstractNodeBefore(abstractNode, beforeThisNode);
	}

	LocalizedString mustOverrideAbstractNode(QualifiedName abstractNode);

	LocalizedString mustOverrideAbstractNodeBefore(QualifiedName abstractNode, QualifiedName beforeThisNode);

	LocalizedString cannotAddChild();

	LocalizedString cannotOverrideNodeWithClass(QualifiedName name, Class<?> nodeClass, Class<?> overrideClass);

	LocalizedString cannotOverrideNodeOutOfOrder(QualifiedName name, List<QualifiedName> nodesSoFar);

	LocalizedString mustOverrideDescendant(Collection<? extends SchemaNode<?, ?>> nodeStack);

	LocalizedString cannotInvokeOnProxyNode(Method method, QualifiedName node);

	LocalizedString cannotDefineInputInContext(QualifiedName name);

	LocalizedString inMethodMustBeThis();

	LocalizedString cannotAcceptFormat(QualifiedName name);

	LocalizedString cannotBeInlineExtensible(QualifiedName name);

	LocalizedString cannotBeAbstract(SchemaNode<?, ?> node);

	LocalizedString cannotFindOutMethodWithoutResultType(SchemaNode<?, ?> node);

	LocalizedString cannotFindOutMethodWithoutTargetType(SchemaNode<?, ?> node);

	LocalizedString cannotInferDataType(SchemaNode<?, ?> effective, TypeToken<?> exactDataType);

	LocalizedString cannotFindUnbindingParameter(QualifiedName p);

	LocalizedString unbindingParameterMustBeDataNode(ChildNode<?, ?> node, QualifiedName p);

	LocalizedString unbindingParameterMustOccurOnce(ChildNode<?, ?> effective, QualifiedName p);

	LocalizedString unbindingParameterMustProvideValue(ChildNode<?, ?> effective, QualifiedName p);
}

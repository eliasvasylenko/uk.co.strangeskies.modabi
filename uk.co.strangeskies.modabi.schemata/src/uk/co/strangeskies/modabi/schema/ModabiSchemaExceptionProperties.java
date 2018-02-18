package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface ModabiSchemaExceptionProperties {
  String invalidDataSource(Object source);

  String incompatibleTypes(Type a, Type b);

  String invalidNamespace(String namespace);

  String noModelFoundForType(QualifiedName model, Type type);

  String noBootstrapModelFound(QualifiedName name);

  String noBootstrapValueFound(QualifiedName name);

  String unexpectedNodeType();

  String cannotProvideSingleValue(QualifiedName name, int valueCount);

  default String noChildFound(
      List<QualifiedName> child,
      QualifiedName parent,
      List<? extends ChildBindingPoint<?>> children) {
    return noChildFound(
        child,
        parent,
        children.stream().map(Objects::toString).collect(Collectors.joining(", ")));
  }

  String noChildFound(List<QualifiedName> child, QualifiedName parent, String children);

  /*
   * Property overriding
   */
  <T> String cannotOverrideIncompatibleProperty(String propertyName, T base, T override);

  String mustProvideValueForNonAbstract(String propertyName);

  String unexpectedOverrideError(Node base);

  String executableTypeStaticMethod();

  String executableTypeMethod();

  String executableTypeConstructor();

  String noMethodCandidatesFoundForNames(Collection<String> names);

  String noConstructorFound(TypeToken<?> receiver, List<TypeToken<?>> parameters);

  String noMethodFound(TypeToken<?> receiver, List<TypeToken<?>> parameters);

  String inMethodMustBeChained(QualifiedName name);

  /*
   * Schema
   */
  String mustOverrideMultiplyInherited(QualifiedName overrideGroup);

  String cannotAddInheritedNodeWhenOverridden(QualifiedName overrideGroup);

  String cannotOverrideNodeWhenOverridden(QualifiedName overrideGroup);

  default String mustOverrideAbstractNode(
      QualifiedName abstractNode,
      QualifiedName beforeThisNode) {
    return (beforeThisNode == null)
        ? mustOverrideAbstractNode(abstractNode)
        : mustOverrideAbstractNodeBefore(abstractNode, beforeThisNode);
  }

  String mustOverrideAbstractNode(QualifiedName abstractNode);

  String mustOverrideAbstractNodeBefore(QualifiedName abstractNode, QualifiedName beforeThisNode);

  String cannotAddChild();

  String cannotOverrideNodeWithClass(
      QualifiedName name,
      Class<?> nodeClass,
      Class<?> overrideClass);

  String cannotOverrideNodeOutOfOrder(QualifiedName name, List<QualifiedName> nodesSoFar);

  String mustOverrideDescendant(Collection<? extends BindingPoint<?>> nodeStack);

  String cannotInvokeOnProxyNode(Method method, QualifiedName node);

  String cannotDefineInputInContext(QualifiedName name);

  String cannotAcceptFormat(QualifiedName name);

  String cannotBeInlineExtensible(QualifiedName name);

  String cannotBeAbstract(Node node);

  String cannotFindOutMethodWithoutResultType(Node node);

  String cannotFindOutMethodWithoutTargetType(Node node);

  String cannotInferDataType(Node effective, TypeToken<?> exactDataType);

  String cannotFindUnbindingParameter(QualifiedName p);

  String unbindingParameterMustBeDataNode(Node node, QualifiedName p);

  String unbindingParameterMustOccurOnce(Node effective, QualifiedName p);

  String unbindingParameterMustProvideValue(Node effective, QualifiedName p);

  String cannotAcceptDuplicate(Object name);

  String cannotResolveVariable(String name);

  String cannotPerformCast(TypeToken<?> to, TypeToken<?> from);

  String cannotPerformAssignment(TypeToken<?> to, TypeToken<?> from);

  String typeMustBeFunctionalInterface(TypeToken<?> implementationType);

  String noChildFound(QualifiedName qualifiedName);

  String cannotAssignToBoundObject();
}

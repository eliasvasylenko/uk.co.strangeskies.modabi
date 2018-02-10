package uk.co.strangeskies.modabi;

import java.lang.reflect.Type;

public interface ModabiExceptionProperties {
  String invalidNamespace(String namespace);

  String cannotAcceptDuplicate(Object name);

  String typeMustBeFunctionalInterface(Type implementationType);

  String cannotPerformCast(Type type, Type type2);

  String cannotPerformAssignment(Type type, Type type2);
}

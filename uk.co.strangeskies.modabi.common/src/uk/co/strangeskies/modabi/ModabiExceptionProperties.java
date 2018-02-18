package uk.co.strangeskies.modabi;

public interface ModabiExceptionProperties {
  String invalidNamespace(String namespace);

  String cannotAcceptDuplicate(Object name);
}

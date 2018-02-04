package uk.co.strangeskies.modabi.io;

import java.net.URI;

import uk.co.strangeskies.modabi.QualifiedName;

public interface ModabiIOExceptionMessages {
  String nextChildDoesNotExist();

  String overlappingDefaultNamespaceHints();

  String unexpectedInputItem(QualifiedName nextName, QualifiedName name);

  String invalidOperationOnProperty(QualifiedName name);

  String invalidOperationOnContent();

  String invalidLocation(URI location);

  String cannotModifyPropertiesAfterChildren();

  String missingFileExtension(String resourceName);

  String noFormatFound(String id);

  String cannotOpenResource();
}

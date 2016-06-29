package uk.co.strangeskies.modabi.io;

import java.net.URI;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.StructuredDataState;
import uk.co.strangeskies.utilities.text.LocalizedString;
import uk.co.strangeskies.utilities.text.LocalizedText;

public interface ModabiIoExceptionText extends LocalizedText<ModabiIoExceptionText> {
	LocalizedString nextChildDoesNotExist();

	LocalizedString overlappingDefaultNamespaceHints();

	LocalizedString unexpectedInputItem(QualifiedName nextName, QualifiedName name);

	LocalizedString illegalState(StructuredDataState state);

	LocalizedString illegalState(DataStreamState state);

	LocalizedString illegalStateTransition(StructuredDataState exitState, StructuredDataState entryState);

	LocalizedString illegalStateTransition(DataStreamState exitState, DataStreamState entryState);

	LocalizedString invalidOperationOnProperty(QualifiedName name);

	LocalizedString invalidOperationOnContent();

	LocalizedString invalidLocation(URI location);
}

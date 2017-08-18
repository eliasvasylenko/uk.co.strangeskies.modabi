package uk.co.strangeskies.modabi.io.xml;

import uk.co.strangeskies.modabi.io.ModabiIOExceptionMessages;

public interface ModabiXmlExceptionMessages {
  ModabiIOExceptionMessages modabiIoProperties();

  String problemReadingFromXmlDocument();

  String problemWritingToXmlDocument();
}

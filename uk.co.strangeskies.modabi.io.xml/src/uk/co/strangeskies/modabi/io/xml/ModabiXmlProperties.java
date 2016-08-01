package uk.co.strangeskies.modabi.io.xml;

import uk.co.strangeskies.modabi.io.IoProperties;
import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.Properties;

public interface ModabiXmlProperties extends Properties<ModabiXmlProperties> {
	IoProperties modabiIoProperties();

	Localized<String> problemReadingFromXmlDocument();

	Localized<String> problemWritingToXmlDocument();
}

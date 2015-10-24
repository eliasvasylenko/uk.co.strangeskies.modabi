package uk.co.strangeskies.modabi.io.xml;

import java.io.InputStream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.io.structured.FileLoader;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;

@Component(immediate = true, property = "id=xml")
public class XmlLoader implements FileLoader {
	@Override
	public boolean isValidForExtension(String extension) {
		return "xml".equals(extension);
	}

	@Override
	public StructuredDataSource loadFile(InputStream in) {
		return XmlSource.from(in);
	}
}

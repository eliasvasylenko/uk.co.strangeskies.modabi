package uk.co.strangeskies.modabi.eclipse.xml.editors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;

public class ModabiXmlContentTypeDescriber extends XMLContentDescriber {
	@Override
	public int describe(InputStream input, IContentDescription description) throws IOException {
		return describe(new InputStreamReader(input), description);
	}

	@Override
	public int describe(Reader input, IContentDescription description) throws IOException {
		int result = super.describe(input, description);
		if (result == VALID) {
			input.reset();
			
		}
		return result;
	}
}

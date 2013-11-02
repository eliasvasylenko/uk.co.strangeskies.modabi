package uk.co.strangeskies.modabi;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface DeclarationTreeProcessor {
	public <T> T processInput(ElementDeclarationNode rootNode, InputStream input);

	public <T> T processOutput(ElementDeclarationNode rootNode,
			OutputStream output);

	public String getFormatName();

	public List<String> getFileExtentions();
}

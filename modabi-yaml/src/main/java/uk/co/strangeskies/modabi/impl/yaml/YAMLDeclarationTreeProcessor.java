package uk.co.strangeskies.modabi.impl.yaml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.DeclarationTreeProcessor;
import uk.co.strangeskies.modabi.ElementDeclarationNode;

public class YAMLDeclarationTreeProcessor implements DeclarationTreeProcessor {
	@Override
	public <T> T processInput(ElementDeclarationNode rootNode, InputStream input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T processOutput(ElementDeclarationNode rootNode,
			OutputStream output) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFormatName() {
		return "YAML";
	}

	@Override
	public List<String> getFileExtentions() {
		return Arrays.asList("yaml");
	}
}

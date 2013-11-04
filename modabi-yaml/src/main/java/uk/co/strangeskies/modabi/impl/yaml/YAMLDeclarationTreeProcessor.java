package uk.co.strangeskies.modabi.impl.yaml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessor;

public class YAMLDeclarationTreeProcessor implements SchemaProcessor {
	@Override
	public <T> T processInput(ElementSchemaNode rootNode, InputStream input) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T processOutput(ElementSchemaNode rootNode,
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

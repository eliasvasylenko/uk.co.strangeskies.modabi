package uk.co.strangeskies.modabi.impl.xml;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.modabi.schema.BindingSchema;
import uk.co.strangeskies.modabi.schema.node.ElementSchemaNode;
import uk.co.strangeskies.modabi.schema.processing.SchemaProcessor;

public class XMLSchemaProcessor implements SchemaProcessor {
	@Override
	public String getFormatName() {
		return "XML";
	}

	@Override
	public List<String> getFileExtentions() {
		return Arrays.asList("xml");
	}

	@Override
	public <T> T processInput(BindingSchema<T> schema, InputStream input) {
		Set<ElementSchemaNode<?>> models = schema.getModelSet();

		ElementSchemaNode<T> root = schema.getRoot();

		return processInput(root, models);
	}

	private <T> T processInput(ElementSchemaNode<T> node,
			Set<ElementSchemaNode<?>> models) {

		return null;
	}

	@Override
	public <T> void processOutput(T data, BindingSchema<T> schema,
			OutputStream output) {
		// TODO Auto-generated method stub

	}
}

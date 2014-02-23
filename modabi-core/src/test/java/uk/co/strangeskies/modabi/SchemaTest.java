package uk.co.strangeskies.modabi;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.modabi.data.StructuredDataOutput;
import uk.co.strangeskies.modabi.data.TerminatingDataSink;
import uk.co.strangeskies.modabi.data.impl.DataTypeBuilderImpl;
import uk.co.strangeskies.modabi.impl.MetaSchemaImpl;
import uk.co.strangeskies.modabi.impl.SchemaBuilderImpl;
import uk.co.strangeskies.modabi.model.impl.ModelBuilderImpl;
import uk.co.strangeskies.modabi.processing.SchemaBinder;
import uk.co.strangeskies.modabi.processing.impl.SchemaBinderImpl;

public class SchemaTest {
	private void run() {
		MetaSchema metaSchema = new MetaSchemaImpl();
		metaSchema.setDataTypeBuilder(new DataTypeBuilderImpl());
		metaSchema.setModelBuilder(new ModelBuilderImpl());
		metaSchema.setSchemaBuilder(new SchemaBuilderImpl());
		metaSchema.initialise();

		SchemaBinder schemaBinder = new SchemaBinderImpl(metaSchema);
		schemaBinder.processOutput(metaSchema.getSchemaModel(),
				new StructuredDataOutput() {
					private String indent = "";

					@Override
					public void childElement(String name) {
						System.out.println(indent + "<" + name + ">");
						indent += "  ";
					}

					@Override
					public TerminatingDataSink property(String name) {
						return getDataSink();
					}

					@Override
					public TerminatingDataSink content() {
						System.out.print(indent);
						return getDataSink();
					}

					@Override
					public void endElement() {
						indent = indent.substring(2);
						System.out.println(indent + "</>");
					}

					private TerminatingDataSink getDataSink() {
						return new TerminatingDataSink() {
							boolean compound = false;

							private void next(Object value) {
								if (compound)
									System.out.print(", ");
								else
									compound = true;
								System.out.print(value);
							}

							@Override
							public TerminatingDataSink string(String value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink longValue(long value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink integer(BigInteger value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink intValue(int value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink floatValue(float value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink doubleValue(double value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink decimal(BigDecimal value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink booleanValue(boolean value) {
								next(value);
								return this;
							}

							@Override
							public TerminatingDataSink binary(byte[] value) {
								next(value);
								return this;
							}

							@Override
							public void end() {
								System.out.println(";");
							}
						};
					}
				}, metaSchema);
	}

	public static void main(String... args) {
		new SchemaTest().run();
	}
}

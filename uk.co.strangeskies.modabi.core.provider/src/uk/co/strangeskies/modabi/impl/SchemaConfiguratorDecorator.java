package uk.co.strangeskies.modabi.impl;

import java.util.Collection;
import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class SchemaConfiguratorDecorator implements SchemaConfigurator {
	private SchemaConfigurator component;

	public SchemaConfiguratorDecorator(SchemaConfigurator component) {
		this.component = component;
	}

	@Override
	public Schema create() {
		return component.create();
	}

	@Override
	public SchemaConfigurator qualifiedName(QualifiedName name) {
		component = component.qualifiedName(name);
		return this;
	}

	@Override
	public SchemaConfigurator imports(Collection<? extends Class<?>> imports) {
		component = component.imports(imports);
		return this;
	}

	@Override
	public SchemaConfigurator dependencies(Collection<? extends Schema> dependencies) {
		component = component.dependencies(dependencies);
		return this;
	}

	@Override
	public DataTypeConfigurator<Object> addDataType() {
		return component.addDataType();
	}

	@Override
	public ModelConfigurator<Object> addModel() {
		return component.addModel();
	}

	@Override
	public <T> Model<T> generateModel(TypeToken<T> type) {
		return component.generateModel(type);
	}

	@Override
	public <T> DataType<T> generateDataType(TypeToken<T> type) {
		return component.generateDataType(type);
	}

	@Override
	public SchemaConfigurator addDataType(String name,
			Function<DataTypeConfigurator<Object>, DataTypeConfigurator<?>> configuration) {
		component = component.addDataType(name, configuration);
		return this;
	}

	@Override
	public SchemaConfigurator addModel(String name,
			Function<ModelConfigurator<Object>, ModelConfigurator<?>> configuration) {
		component = component.addModel(name, configuration);
		return this;
	}
}

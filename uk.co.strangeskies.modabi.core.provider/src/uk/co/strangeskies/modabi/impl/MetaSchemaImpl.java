/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl;

import static uk.co.strangeskies.mathematics.Range.between;
import static uk.co.strangeskies.modabi.ValueResolution.REGISTRATION_TIME;
import static uk.co.strangeskies.modabi.io.Primitive.BOOLEAN;
import static uk.co.strangeskies.modabi.io.Primitive.STRING;
import static uk.co.strangeskies.modabi.processing.InputBindingStrategy.STATIC_FACTORY;
import static uk.co.strangeskies.modabi.processing.InputBindingStrategy.TARGET_ADAPTOR;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.CONTENT;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.PROPERTY;
import static uk.co.strangeskies.modabi.schema.DataNode.Format.SIMPLE;

import java.util.Arrays;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.DataTypes;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.BindingNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChildNodeConfigurator;
import uk.co.strangeskies.modabi.schema.ChoiceNode;
import uk.co.strangeskies.modabi.schema.ComplexNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.modabi.schema.InputNodeConfigurator;
import uk.co.strangeskies.modabi.schema.InputSequenceNodeConfigurator;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.modabi.schema.SequenceNode;
import uk.co.strangeskies.modabi.schema.building.DataLoader;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class MetaSchemaImpl implements MetaSchema {
	private interface TypeFactory {
		<T> DataType<T> apply(String name, Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> type);
	}

	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> type);
	}

	private final Schema metaSchema;
	private Model<Schema> schemaModel;
	private Model<Model<?>> metaModel;
	private Model<DataType<?>> dataTypeModel;

	public MetaSchemaImpl(SchemaBuilder schema, DataLoader loader, BaseSchema base) {
		QualifiedName name = QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Schema
		 */
		SchemaConfigurator schemaConfigurator = schema.configure(loader).qualifiedName(name)
				.dependencies(Arrays.asList(base));

		/*
		 * Types
		 */
		buildTypes(new TypeFactory() {
			@Override
			public <T> DataType<T> apply(String name,
					Function<DataTypeConfigurator<Object>, DataTypeConfigurator<T>> typeFunction) {
				return typeFunction.apply(schemaConfigurator.addDataType().name(name, namespace)).create();
			}
		}, base, namespace);

		/*
		 * Models
		 */
		buildModels(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<Object>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(name, namespace)).create();
			}
		}, base, namespace);

		metaSchema = schemaConfigurator.create();
	}

	private void buildTypes(TypeFactory factory, BaseSchema base, Namespace namespace) {}

	private void buildModels(ModelFactory factory, BaseSchema base, Namespace namespace) {
		/* Node Models */

		Model<SchemaNodeConfigurator<?, ?>> nodeModel = factory.apply("node",
				m -> m.concrete(false).dataType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {})
						.outputBindingStrategy(OutputBindingStrategy.SIMPLE)
						.addChild(n -> n.inputSequence().name("configure").concrete(false)
								.postInputType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}))
						.addChild(n -> n.data().format(PROPERTY).type(base.primitiveType(Primitive.QUALIFIED_NAME)).name("name")
								.inputMethod("name").optional(true))
						.addChild(n -> n.data().format(PROPERTY).type(base.primitiveType(Primitive.BOOLEAN)).name("concrete")
								.optional(true)));

		Model<ChildNodeConfigurator<?, ?>> childBaseModel = factory.apply("childBase",
				m -> m.concrete(false).dataType(new TypeToken<ChildNodeConfigurator<?, ?>>() {})
						.inputBindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}));

		Model<SchemaNodeConfigurator<?, ?>> branchModel = factory.apply("branch",
				m -> m.concrete(false).baseModel(nodeModel).addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().name("concrete"))
						.addChild(n -> n.complex().name("child").outputMethod("children").inputNone().concrete(false)
								.extensible(true).model(childBaseModel).occurrences(Range.between(0, null)))
						.addChild(n -> n.inputSequence().name("create").chainedInput(true)));

		@SuppressWarnings("unchecked")
		Model<ChildNodeConfigurator<?, ?>> childModel = factory.apply("child",
				m -> m.baseModel(branchModel, childBaseModel).concrete(false)
						.dataType(new TypeToken<ChildNodeConfigurator<?, ?>>() {}).inputBindingStrategy(TARGET_ADAPTOR)
						.inputBindingType(new TypeToken<SchemaNodeConfigurator<?, ?>>() {}.getType())
						.addChild(c -> c.inputSequence().name("addChild").chainedInput(true))
						.addChild(c -> c.inputSequence().name("configure").concrete(false).chainedInput(true)
								.postInputType(new TypeToken<ChildNodeConfigurator<?, ?>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().format(PROPERTY).name("ordered").type(base.primitiveType(BOOLEAN)).optional(true))
						.addChild(
								n -> n.data().format(PROPERTY).name("occurrences").type(base.derivedTypes().rangeType()).optional(true))
						.addChild(n -> n.data().format(PROPERTY).type(base.primitiveType(STRING)).name("postInputType")
								.optional(true).outputMethod("postInputTypeString")));

		Model<BindingNodeConfigurator<?, ?, ?>> bindingNodeModel = factory.apply("binding",
				m -> m.baseModel(branchModel).concrete(false).dataType(new TypeToken<BindingNodeConfigurator<?, ?, ?>>() {})
						.addChild(c -> c.inputSequence().name("configure").concrete(false)
								.postInputType(new TypeToken<BindingNodeConfigurator<?, ?, Object>>() {}))
						.addChild(n -> n.data().name("name"))
						.addChild(o -> o.data().format(PROPERTY).name("dataType").optional(true).type(base.primitiveType(STRING))
								.outputMethod("dataTypeString"))
						.addChild(o -> o.data().format(PROPERTY).name("bindingStrategy").type(base.derivedTypes().enumType())
								.dataType(InputBindingStrategy.class).optional(true))
						.addChild(n -> n.data().format(PROPERTY).name("bindingType").optional(true).type(base.primitiveType(STRING))
								.outputMethod("bindingTypeString"))
						.addChild(o -> o.data().format(PROPERTY).name("unbindingStrategy").type(base.derivedTypes().enumType())
								.dataType(OutputBindingStrategy.class).optional(true))
						.addChild(o -> o.data().format(PROPERTY).name("unbindingMethod").outputMethod("unbindingMethodName")
								.type(base.primitiveType(STRING)).optional(true))
						.addChild(n -> n.data().format(PROPERTY).name("unbindingType").optional(true)
								.type(base.primitiveType(STRING)).outputMethod("unbindingTypeString"))
						.addChild(n -> n.data().format(PROPERTY).name("providedUnbindingMethodParameters").optional(true)
								.outputMethod("providedUnbindingMethodParameterNames").inputMethod("providedUnbindingMethodParameters")
								.type(base.derivedTypes().listType())
								.addChild(o -> o.data().name("element").type(base.primitiveType(Primitive.QUALIFIED_NAME))))
						.addChild(n -> n.data().format(PROPERTY).name("unbindingFactoryType").optional(true)
								.type(base.primitiveType(STRING)).outputMethod("unbindingFactoryTypeString")));

		Model<InputNodeConfigurator<?, ?>> inputModel = factory.apply("input", m -> m.concrete(false).baseModel(childModel)
				.dataType(new TypeToken<InputNodeConfigurator<?, ?>>() {})
				.addChild(c -> c.inputSequence().name("configure").concrete(false)
						.postInputType(new TypeToken<InputNodeConfigurator<?, ?>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(PROPERTY).name("inMethod").outputMethod("inMethodName").optional(true)
						.type(base.primitiveType(STRING)))
				.addChild(
						n -> n.data().format(PROPERTY).name("inMethodChained").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("inMethodCast").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(
						n -> n.data().format(PROPERTY).name("inMethodUnchecked").optional(true).type(base.primitiveType(BOOLEAN))));

		@SuppressWarnings("unchecked")
		Model<BindingChildNodeConfigurator<?, ?, ?>> bindingChildNodeModel = factory.apply("bindingChild", m -> m
				.concrete(false).dataType(new TypeToken<BindingChildNodeConfigurator<?, ?, ?>>() {})
				.baseModel(inputModel, bindingNodeModel)
				.addChild(c -> c.inputSequence().name("configure").concrete(false)
						.postInputType(new TypeToken<BindingChildNodeConfigurator<?, ?, Object>>() {}))
				.addChild(n -> n.data().name("name"))
				.addChild(n -> n.data().format(PROPERTY).name("extensible").type(base.primitiveType(BOOLEAN)).optional(true))
				.addChild(n -> n.data().format(PROPERTY).name("synchronous").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethod").outputMethod("outMethodName").optional(true)
						.type(base.primitiveType(STRING)))
				.addChild(
						n -> n.data().format(PROPERTY).name("outMethodIterable").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethodCast").optional(true).type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("outMethodUnchecked").optional(true)
						.type(base.primitiveType(BOOLEAN))));

		factory.apply("choice", m -> m.dataType(ChoiceNode.class).baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inputMethod("choice")).addChild(n -> n.data().name("name")));

		factory.apply("sequence", m -> m.dataType(SequenceNode.class).baseModel(childModel)
				.addChild(c -> c.inputSequence().name("configure").inputMethod("sequence")));

		@SuppressWarnings({ "unchecked", "unused" })
		Model<InputSequenceNodeConfigurator> inputSequenceModel = factory.apply("inputSequence",
				m -> m.dataType(InputSequenceNodeConfigurator.class)
						.baseModel(inputModel, childModel).addChild(c -> c.inputSequence().name("configure")
								.inputMethod("inputSequence").postInputType(InputSequenceNodeConfigurator.class))
						.addChild(n -> n.data().name("name")));

		/*
		 * configure a node to bind a list of base models
		 */
		Function<DataNodeConfigurator<Object>, SchemaNodeConfigurator<?, ?>> baseModelConfiguration = n -> n
				.format(PROPERTY).optional(true).type(base.derivedTypes().listType()).uncheckedOutput(true).uncheckedInput(true)
				.addChild(o -> o.data().name("element").type(base.derivedTypes().referenceType())
						.dataType(new TypeToken<Model<?>>() {})
						.addChild(p -> p.data().name("targetModel").dataType(new TypeToken<Model<Model<?>>>() {})
								.provideValue(new BufferingDataTarget()
										.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer()))
						.addChild(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
								.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())));

		metaModel = factory.apply("model",
				m -> m.dataType(new TypeToken<Model<?>>() {})
						.addChild(o -> o.complex().name("configurator").inline(true).model(bindingNodeModel)
								.dataType(new TypeToken<ModelConfigurator<?>>() {}).inputBindingType(SchemaConfigurator.class)
								.inputBindingStrategy(TARGET_ADAPTOR)
								.addChild(c -> c.inputSequence().name("configure").chainedInput(true).inputMethod("addModel"))
								.addChild(n -> n.data().name("name").optional(false))
								.addChild(n -> baseModelConfiguration.apply(n.data().name("baseModel")))));

		Model<ComplexNodeConfigurator<?>> abstractComplexModel = factory.apply("abstractComplex",
				m -> m.concrete(false).dataType(new TypeToken<ComplexNodeConfigurator<?>>() {}).baseModel(bindingChildNodeModel)
						.addChild(c -> c.inputSequence().name("addChild"))
						.addChild(c -> c.inputSequence().name("configure").inputMethod("complex").chainedInput(true)
								.postInputType(new TypeToken<ComplexNodeConfigurator<Object>>() {}))
						.addChild(n -> n.data().name("name")).addChild(n -> baseModelConfiguration.apply(n.data().name("model")))
						.addChild(c -> c.data().name("inline").concrete(false).optional(true).valueResolution(REGISTRATION_TIME)
								.type(base.primitiveType(BOOLEAN))));

		factory.apply("complex", m -> m.baseModel(abstractComplexModel).addChild(c -> c.data().name("inline").optional(true)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, false).buffer())));

		factory.apply("inline", m -> m.baseModel(abstractComplexModel).addChild(c -> c.data().name("inline").optional(false)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, true).buffer())));

		Model<DataNodeConfigurator<?>> typedDataModel = factory.apply("typedData",
				m -> m.baseModel(bindingChildNodeModel).dataType(new TypeToken<DataNodeConfigurator<?>>() {}).concrete(false)
						.addChild(c -> c.inputSequence().name("addChild"))
						.addChild(c -> c.inputSequence().name("configure").inputMethod("data").chainedInput(true))
						.addChild(n -> n.data().format(PROPERTY).name("format").optional(true)
								.valueResolution(REGISTRATION_TIME).concrete(false).type(base.derivedTypes().enumType())
								.dataType(Format.class).postInputType(DataNodeConfigurator.class))
						.addChild(n -> n.data().name("name"))
						.addChild(n -> n.data().format(PROPERTY).name("type").optional(true)
								.type(base.derivedTypes().referenceType()).dataType(new TypeToken<DataType<?>>() {})
								.addChild(p -> p.data().name("targetModel").valueResolution(REGISTRATION_TIME)
										.provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer()))
								.addChild(p -> p.data().name("targetId").valueResolution(REGISTRATION_TIME)
										.provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
						.addChild(
								n -> n.data().format(PROPERTY).name("nullIfOmitted").optional(true).type(base.primitiveType(BOOLEAN)))
						.addChild(n -> n.data().format(PROPERTY).name("valueResolution").optional(true)
								.type(base.derivedTypes().enumType()).dataType(ValueResolution.class))
						.addChild(n -> n.complex().name("valueInline").inputMethod("provideValue")
								.outputMethod("providedValueBuffer").dataType(DataSource.class).outputBindingType(DataSource.class)
								.inputBindingType(Function.class).inputBindingStrategy(STATIC_FACTORY).inline(true).optional(true)
								.addChild(o -> o.inputSequence().name("identity"))
								.addChild(o -> o.choice().name("valueFormat")
										.addChild(p -> p.data().format(PROPERTY).name("value").chainedInput(true)
												.type(base.derivedTypes().bufferedDataType()).outputMethod("copy").inputMethod("apply"))
										.addChild(p -> p.data().format(CONTENT).name("valueContent").chainedInput(true)
												.type(base.derivedTypes().bufferedDataType()).outputMethod("copy").inputMethod("apply")))));

		factory.apply("content", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, CONTENT.toString()).buffer())));

		factory.apply("property", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, PROPERTY.toString()).buffer())));

		factory.apply("simple", m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").optional(false)
				.provideValue(new BufferingDataTarget().put(STRING, SIMPLE.toString()).buffer())));

		factory.apply("data",
				m -> m.baseModel(typedDataModel).addChild(n -> n.data().name("format").occurrences(between(0, 0))));

		/* Type Models */

		dataTypeModel = factory.apply("type", m -> m.dataType(new TypeToken<DataType<?>>() {}).addChild(o -> o.complex()
				.name("configurator").model(bindingNodeModel).dataType(new TypeToken<DataTypeConfigurator<?>>() {})
				.inputBindingType(SchemaConfigurator.class).inputBindingStrategy(TARGET_ADAPTOR)
				.addChild(c -> c.inputSequence().name("configure").chainedInput(true).inputMethod("addDataType"))
				.addChild(n -> n.data().format(PROPERTY).name("private").inputMethod("isPrivate").optional(true)
						.type(base.primitiveType(BOOLEAN)))
				.addChild(n -> n.data().format(PROPERTY).name("baseType").optional(true)
						.type(base.derivedTypes().referenceType()).dataType(new TypeToken<DataType<?>>() {})
						.addChild(p -> p.data().name("targetModel").provideValue(
								new BufferingDataTarget().put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer()))
						.addChild(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
								.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))));

		/* Schema Models */

		schemaModel = factory.apply("schema", m -> m
				.dataType(
						Schema.class)
				.inputBindingType(
						SchemaConfigurator.class)
				.addChild(
						n -> n.data().format(PROPERTY).name("name").inputMethod("qualifiedName").outputMethod("qualifiedName")
								.type(
										base.primitiveType(Primitive.QUALIFIED_NAME)))
				.addChild(
						n -> n.data().format(SIMPLE).name("dependencies")
								.occurrences(
										Range
												.between(0, 1))
								.type(
										base.derivedTypes().setType())
								.addChild(
										o -> o.data().inputMethod("add").name("element").type(base.derivedTypes().importType())
												.dataType(Schema.class).outputSelf().occurrences(Range.between(0, null))
												.addChild(
														i -> i.data().name("import")
																.addChild(p -> p.data().name("targetModel").provideValue(new BufferingDataTarget()
																		.put(Primitive.QUALIFIED_NAME, new QualifiedName("schema", namespace)).buffer()))
																.addChild(p -> p.data().name("targetId")
																		.provideValue(new BufferingDataTarget()
																				.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace)).buffer())))
												.addChild(p -> p.data().name("dataTypes").inputNone().type(base.derivedTypes().includeType())
														.inputBindingType(Schema.class).addChild(
																q -> q.inputSequence().name("dataTypes").chainedInput(true))
														.addChild(q -> q.data().name("targetModel")
																.provideValue(new BufferingDataTarget()
																		.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace)).buffer())))
												.addChild(
														p -> p.data().name("models").inputNone().type(base.derivedTypes().includeType())
																.inputBindingType(Schema.class)
																.addChild(q -> q.inputSequence().name("models").chainedInput(true))
																.addChild(q -> q.data().name("targetModel").provideValue(new BufferingDataTarget()
																		.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace)).buffer())))))
				.addChild(
						i -> i.data().format(SIMPLE).name("imports").optional(true).inputBindingStrategy(TARGET_ADAPTOR)
								.outputSelf().dataType(Schema.class).inputBindingType(SchemaConfigurator.class).inputNone()
								.addChild(n -> n.data().name("importsIn").inputMethod("imports").outputNone()
										.type(base.derivedTypes().setType()).addChild(
												e -> e.data().name("element").type(base.derivedTypes().classType())))
								.addChild(n -> n.data().name("importsOut").inputNone().optional(true).outputMethod("imports")
										.dataType(Imports.class).addChild(s -> s.data().name("imports").outputMethod("getImportedClasses")
												.inputNone().type(base.derivedTypes().setType())
												.addChild(e -> e.data().name("element").type(base.derivedTypes().classType())))))
				.addChild(n -> n.complex().name("types").outputSelf().occurrences(Range.between(0, 1)).dataType(Schema.class)
						.inputBindingType(SchemaConfigurator.class).inputNone().inputBindingStrategy(TARGET_ADAPTOR)
						.addChild(o -> o.complex().model(dataTypeModel).inputNone().inputBindingStrategy(TARGET_ADAPTOR)
								.outputMethod("dataTypes").name("type").dataType(new TypeToken<DataType<?>>() {})
								.occurrences(Range.between(0, null))))
				.addChild(n -> n.complex().name("models").outputSelf().occurrences(Range.between(0, 1)).dataType(Schema.class)
						.inputBindingType(SchemaConfigurator.class).inputNone().inputBindingStrategy(TARGET_ADAPTOR)
						.addChild(o -> o.complex().model(metaModel).inputNone().inputBindingStrategy(TARGET_ADAPTOR)
								.outputMethod("models").occurrences(Range.between(0, null))))
				.addChild(n -> n.inputSequence().name("create").chainedInput(true)));
	}

	@Override
	public QualifiedName qualifiedName() {
		return metaSchema.qualifiedName();
	}

	@Override
	public Schemata dependencies() {
		return metaSchema.dependencies();
	}

	@Override
	public DataTypes dataTypes() {
		return metaSchema.dataTypes();
	}

	@Override
	public Models models() {
		return metaSchema.models();
	}

	@Override
	public Model<Schema> getSchemaModel() {
		return schemaModel;
	}

	@Override
	public Model<Model<?>> getMetaModel() {
		return metaModel;
	}

	@Override
	public Model<DataType<?>> getDataTypeModel() {
		return dataTypeModel;
	}

	@Override
	public boolean equals(Object obj) {
		return metaSchema.equals(obj);
	}

	@Override
	public int hashCode() {
		return metaSchema.hashCode();
	}

	@Override
	public Imports imports() {
		return Imports.empty().withImport(Schema.class);
	}

	@Override
	public String toString() {
		return qualifiedName().toString();
	}
}

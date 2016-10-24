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
import static uk.co.strangeskies.modabi.schema.bindingconditions.RequiredBindingOccurrences.occurrences;
import static uk.co.strangeskies.modabi.schema.bindingconditions.OptionalBinding.optional;
import static uk.co.strangeskies.modabi.schema.bindingconditions.SynchronizedBinding.asynchronous;

import java.util.Arrays;
import java.util.function.Function;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.modabi.BaseSchema;
import uk.co.strangeskies.modabi.MetaSchema;
import uk.co.strangeskies.modabi.Models;
import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaBuilder;
import uk.co.strangeskies.modabi.SchemaConfigurator;
import uk.co.strangeskies.modabi.Schemata;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.declarative.InputBindingStrategy;
import uk.co.strangeskies.modabi.declarative.OutputBindingStrategy;
import uk.co.strangeskies.modabi.io.BufferingDataTarget;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.DataLoader;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.ModelConfigurator;
import uk.co.strangeskies.modabi.schema.SchemaNode.Format;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.TypeToken;

public class MetaSchemaImpl implements MetaSchema {
	private interface ModelFactory {
		<T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> type);
	}

	private final Schema metaSchema;
	private Model<Schema> schemaModel;
	private Model<Model<?>> metaModel;

	public MetaSchemaImpl(SchemaBuilder schema, DataLoader loader, BaseSchema base) {
		QualifiedName name = QUALIFIED_NAME;
		Namespace namespace = name.getNamespace();

		/*
		 * Schema
		 */
		SchemaConfigurator schemaConfigurator = schema
				.configure(loader)
				.qualifiedName(name)
				.dependencies(Arrays.asList(base));

		/*
		 * Models
		 */
		buildModels(new ModelFactory() {
			@Override
			public <T> Model<T> apply(String name, Function<ModelConfigurator<?>, ModelConfigurator<T>> modelFunction) {
				return modelFunction.apply(schemaConfigurator.addModel().name(new QualifiedName(name, namespace))).create();
			}
		}, base, namespace);

		metaSchema = schemaConfigurator.create();
	}

	private void buildModels(ModelFactory factory, BaseSchema base, Namespace namespace) {
		/* Node Models */

		@SuppressWarnings("unchecked")
		Model<BindingPoint<?>> bindingPointModel = factory.apply("bindingPoint",
				m -> m.concrete(false).dataType(new TypeToken<BindingPoint<?>>() {}).node(n -> n
						.addChildBindingPoint(c -> c.inputSequence().name("addChild").chainedInput(true))
						.addChildBindingPoint(
								c -> c.inputSequence().name("configure").concrete(false).chainedInput(true).postInputType(
										new TypeToken<ChildNodeConfigurator<?, ?>>() {}))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("orderedOccurrences").type(base.primitive(BOOLEAN)).optional(true))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("occurrences").type(base.derived().rangeType()).optional(true))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.type(base.primitive(Primitive.STRING))
								.name("postInputType")
								.optional(true))));

		Model<SchemaNodeConfigurator> nodeModel = factory.apply("node",
				m -> m
						.concrete(false)
						.dataType(new TypeToken<SchemaNodeConfigurator>() {})
						.node(n -> n
								.initializeInput(i -> i.parent().invokeResolvedMethod("node"))
								.initializeOutput(o -> o.parent().invokeResolvedMethod("node"))
								.addChildBindingPoint(
										b -> b.name("name").baseModel(base.primitive(Primitive.QUALIFIED_NAME)).condition(optional()))
								.addChildBindingPoint(
										b -> b.name("concrete").baseModel(base.primitive(Primitive.BOOLEAN)).condition(optional()))
								.addChildBindingPoint(b -> b
										.name("child")
										.extensible(true)
										.concrete(false)
										.dataType(new TypeToken<ChildBindingPoint<?>>() {})
										.noInput()
										.condition(asynchronous().and(occurrences(between(0, null)))))
								.addChildBindingPoint(b -> b.name("create").noOutput().input(
										i -> i.target().assign(i.target().invokeResolvedMethod("create"))))));

		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * TODO replace dataType(String) and getDataTypeString() methods with their
		 * TypeToken equivalents, have a magic "node reference" to "imports" to
		 * support unqualified names
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
		 */

		Model<BindingNodeConfigurator<?, ?, ?>> bindingNodeModel = factory.apply("binding",
				m -> m
						.baseModel(branchModel)
						.concrete(false)
						.dataType(new TypeToken<BindingNodeConfigurator<?, ?, ?>>() {})
						.addChildBindingPoint(c -> c.inputSequence().name("configure").concrete(false).postInputType(
								new TypeToken<BindingNodeConfigurator<?, ?, Object>>() {}))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(
								o -> o.data().format(PROPERTY).name("dataType").outputMethod("getDataTypeString").optional(true).type(
										base.primitive(Primitive.STRING)))
						.addChildBindingPoint(o -> o
								.data()
								.format(PROPERTY)
								.name("inputBindingStrategy")
								.type(base.derived().enumType())
								.dataType(InputBindingStrategy.class)
								.optional(true))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("inputBindingType")
								.outputMethod("getInputBindingTypeString")
								.optional(true)
								.type(base.primitive(Primitive.STRING)))
						.addChildBindingPoint(o -> o
								.data()
								.format(PROPERTY)
								.name("outputBindingStrategy")
								.type(base.derived().enumType())
								.dataType(OutputBindingStrategy.class)
								.optional(true))
						.addChildBindingPoint(
								o -> o.data().format(PROPERTY).name("outputBindingMethod").type(base.primitive(STRING)).optional(true))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("outputBindingType")
								.outputMethod("getOutputBindingTypeString")
								.optional(true)
								.type(base.primitive(Primitive.STRING)))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("providedOutputBindingMethodParameters")
								.optional(true)
								.type(base.derived().listType())
								.addChildBindingPoint(o -> o.data().name("element").type(base.primitive(Primitive.QUALIFIED_NAME))))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("outputBindingFactoryType")
								.outputMethod("getOutputBindingFactoryTypeString")
								.optional(true)
								.type(base.primitive(Primitive.STRING))));

		Model<InputNodeConfigurator<?, ?>> inputModel = factory.apply("input",
				m -> m
						.concrete(false)
						.baseModel(childModel)
						.dataType(new TypeToken<InputNodeConfigurator<?, ?>>() {})
						.addChildBindingPoint(c -> c.inputSequence().name("configure").concrete(false).postInputType(
								new TypeToken<InputNodeConfigurator<?, ?>>() {}))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(o -> o
								.choice()
								.name("inputMember")
								.optional(true)
								.addChildBindingPoint(n -> n.data().format(PROPERTY).name("inputMethod").type(base.primitive(STRING)))
								.addChildBindingPoint(n -> n
										.sequence()
										.name("inputNone")
										.addChildBindingPoint(
												e -> e.data().format(PROPERTY).name("inputField").type(base.primitive(STRING)).provideValue(
														new BufferingDataTarget().put(Primitive.STRING, "void").buffer()))
										.addChildBindingPoint(e -> e.inputSequence().name("inputNone")))
								.addChildBindingPoint(n -> n.data().format(PROPERTY).name("inputField").type(base.primitive(STRING))))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("chainedInput").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("castInput").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("uncheckedInput").optional(true).type(base.primitive(BOOLEAN))));

		@SuppressWarnings("unchecked")
		Model<BindingChildNodeConfigurator<?, ?, ?>> bindingChildNodeModel = factory.apply("bindingChild",
				m -> m
						.concrete(false)
						.dataType(new TypeToken<BindingChildNodeConfigurator<?, ?, ?>>() {})
						.baseModel(inputModel, bindingNodeModel)
						.addChildBindingPoint(c -> c.inputSequence().name("configure").concrete(false).postInputType(
								new TypeToken<BindingChildNodeConfigurator<?, ?, Object>>() {}))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("extensible").type(base.primitive(BOOLEAN)).optional(true))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("synchronous").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(o -> o
								.choice()
								.name("outputMember")
								.optional(true)
								.addChildBindingPoint(n -> n.data().format(PROPERTY).name("outputMethod").type(base.primitive(STRING)))
								.addChildBindingPoint(n -> n
										.sequence()
										.name("outputNone")
										.addChildBindingPoint(
												e -> e.data().format(PROPERTY).name("outputField").type(base.primitive(STRING)).provideValue(
														new BufferingDataTarget().put(Primitive.STRING, "void").buffer()))
										.addChildBindingPoint(e -> e.inputSequence().name("outputNone")))
								.addChildBindingPoint(n -> n
										.sequence()
										.name("outputThis")
										.addChildBindingPoint(
												e -> e.data().format(PROPERTY).name("outputField").type(base.primitive(STRING)).provideValue(
														new BufferingDataTarget().put(Primitive.STRING, "this").buffer()))
										.addChildBindingPoint(e -> e.inputSequence().name("outputSelf")))
								.addChildBindingPoint(n -> n.data().format(PROPERTY).name("outputField").type(base.primitive(STRING))))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("iterableOutput").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("castOutput").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("uncheckedOutput").optional(true).type(base.primitive(BOOLEAN))));

		factory.apply("choice",
				m -> m
						.dataType(ChoiceNodeConfigurator.class)
						.baseModel(childModel)
						.addChildBindingPoint(c -> c.inputSequence().name("configure").inputMethod("choice"))
						.addChildBindingPoint(n -> n.data().name("name")));

		factory.apply("sequence",
				m -> m.dataType(SequenceNodeConfigurator.class).baseModel(childModel).addChildBindingPoint(
						c -> c.inputSequence().name("configure").inputMethod("sequence")));

		@SuppressWarnings({ "unchecked", "unused" })
		Model<InputSequenceNodeConfigurator> inputSequenceModel = factory.apply("inputSequence",
				m -> m
						.dataType(InputSequenceNodeConfigurator.class)
						.baseModel(inputModel, childModel)
						.addChildBindingPoint(c -> c.inputSequence().name("configure").inputMethod("inputSequence").postInputType(
								InputSequenceNodeConfigurator.class))
						.addChildBindingPoint(n -> n.data().name("name")));

		/*
		 * configure a node to bind a list of base models
		 */
		Function<DataNodeConfigurator<Object>, SchemaNodeConfigurator> baseModelConfiguration = n -> n
				.format(PROPERTY)
				.optional(true)
				.type(base.derived().listType())
				.uncheckedOutput(true)
				.uncheckedInput(true)
				.addChildBindingPoint(o -> o
						.data()
						.name("element")
						.type(base.derived().referenceType())
						.dataType(new TypeToken<Model<?>>() {})
						.addChildBindingPoint(
								p -> p.data().name("targetModel").dataType(new TypeToken<Model<Model<?>>>() {}).provideValue(
										new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
												.buffer()))
						.addChildBindingPoint(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
								.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
								.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
								.buffer())));

		metaModel = factory.apply("model", m -> m
				.dataType(new TypeToken<Model<?>>() {})
				.inputBindingType(SchemaConfigurator.class)
				.inputBindingStrategy(TARGET_ADAPTOR)
				.addChildBindingPoint(o -> o
						.complex()
						.name("configurator")
						.inline(true)
						.model(bindingNodeModel)
						.dataType(new TypeToken<ModelConfigurator<?>>() {})
						.inputBindingType(SchemaConfigurator.class)
						.inputBindingStrategy(TARGET_ADAPTOR)
						.inputNone()
						.addChildBindingPoint(c -> c.inputSequence().name("configure").chainedInput(true).inputMethod("addModel"))
						.addChildBindingPoint(n -> n.data().name("name").optional(false))
						.addChildBindingPoint(n -> baseModelConfiguration.apply(n.data().name("baseModel")))));

		Model<ComplexNodeConfigurator<?>> abstractComplexModel = factory.apply("abstractComplex",
				m -> m
						.concrete(false)
						.dataType(new TypeToken<ComplexNodeConfigurator<?>>() {})
						.baseModel(bindingChildNodeModel)
						.addChildBindingPoint(c -> c.inputSequence().name("addChild"))
						.addChildBindingPoint(
								c -> c.inputSequence().name("configure").inputMethod("complex").chainedInput(true).postInputType(
										new TypeToken<ComplexNodeConfigurator<Object>>() {}))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(n -> baseModelConfiguration.apply(n.data().name("model")))
						.addChildBindingPoint(
								c -> c.data().name("inline").concrete(false).optional(true).valueResolution(REGISTRATION_TIME).type(
										base.primitive(BOOLEAN))));

		factory.apply("complex", m -> m.baseModel(abstractComplexModel).addChildBindingPoint(c -> c
				.data()
				.name("inline")
				.optional(true)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, false).buffer())));

		factory.apply("inline", m -> m.baseModel(abstractComplexModel).addChildBindingPoint(c -> c
				.data()
				.name("inline")
				.optional(false)
				.provideValue(new BufferingDataTarget().put(BOOLEAN, true).buffer())));

		Model<DataNodeConfigurator<?>> typedDataModel = factory.apply("typedData",
				m -> m
						.baseModel(bindingChildNodeModel)
						.dataType(new TypeToken<DataNodeConfigurator<?>>() {})
						.concrete(false)
						.addChildBindingPoint(c -> c.inputSequence().name("addChild"))
						.addChildBindingPoint(c -> c.inputSequence().name("configure").inputMethod("data").chainedInput(true))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("format")
								.optional(true)
								.valueResolution(REGISTRATION_TIME)
								.concrete(false)
								.type(base.derived().enumType())
								.dataType(Format.class)
								.postInputType(DataNodeConfigurator.class))
						.addChildBindingPoint(n -> n.data().name("name"))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("type")
								.optional(true)
								.type(base.derived().referenceType())
								.dataType(new TypeToken<SimpleNode<?>>() {})
								.addChildBindingPoint(p -> p.data().name("targetModel").valueResolution(REGISTRATION_TIME).provideValue(
										new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace))
												.buffer()))
								.addChildBindingPoint(p -> p
										.data()
										.name("targetId")
										.valueResolution(REGISTRATION_TIME)
										.provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
												.buffer())))
						.addChildBindingPoint(
								n -> n.data().format(PROPERTY).name("nullIfOmitted").optional(true).type(base.primitive(BOOLEAN)))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("valueResolution")
								.optional(true)
								.type(base.derived().enumType())
								.dataType(ValueResolution.class))
						.addChildBindingPoint(n -> n
								.data()
								.format(PROPERTY)
								.name("value")
								.inputMethod("provideValue")
								.outputMethod("getProvidedValue")
								.type(base.derived().bufferedDataType())
								.optional(true)));

		factory.apply("content",
				m -> m.baseModel(typedDataModel).addChildBindingPoint(n -> n.data().name("format").optional(false).provideValue(
						new BufferingDataTarget().put(STRING, CONTENT.toString()).buffer())));

		factory.apply("property",
				m -> m.baseModel(typedDataModel).addChildBindingPoint(n -> n.data().name("format").optional(false).provideValue(
						new BufferingDataTarget().put(STRING, PROPERTY.toString()).buffer())));

		factory.apply("simple",
				m -> m.baseModel(typedDataModel).addChildBindingPoint(n -> n.data().name("format").optional(false).provideValue(
						new BufferingDataTarget().put(STRING, SIMPLE.toString()).buffer())));

		factory.apply("data",
				m -> m.baseModel(typedDataModel).addChildBindingPoint(n -> n.data().name("format").occurrences(between(0, 0))));

		/* Type Models */

		dataTypeModel = factory.apply("type",
				m -> m
						.dataType(new TypeToken<SimpleNode<?>>() {})
						.inputBindingType(SchemaConfigurator.class)
						.inputBindingStrategy(TARGET_ADAPTOR)
						.addChildBindingPoint(o -> o
								.complex()
								.name("configurator")
								.model(bindingNodeModel)
								.inline(true)
								.dataType(new TypeToken<DataTypeConfigurator<?>>() {})
								.inputBindingType(SchemaConfigurator.class)
								.inputBindingStrategy(TARGET_ADAPTOR)
								.inputNone()
								.addChildBindingPoint(
										c -> c.inputSequence().name("configure").chainedInput(true).inputMethod("addDataType"))
								.addChildBindingPoint(
										n -> n.data().format(PROPERTY).name("exported").inputMethod("export").optional(true).type(
												base.primitive(BOOLEAN)))
								.addChildBindingPoint(n -> n
										.data()
										.format(PROPERTY)
										.name("baseType")
										.optional(true)
										.type(base.derived().referenceType())
										.dataType(new TypeToken<SimpleNode<?>>() {})
										.addChildBindingPoint(p -> p.data().name("targetModel").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace))
												.buffer()))
										.addChildBindingPoint(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("configurator", namespace))
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
												.buffer())))));

		/* Schema Models */

		schemaModel = factory.apply("schema", m -> m
				.dataType(Schema.class)
				.inputBindingType(SchemaConfigurator.class)
				.addChildBindingPoint(
						n -> n.data().format(PROPERTY).name("name").inputMethod("qualifiedName").outputMethod("qualifiedName").type(
								base.primitive(Primitive.QUALIFIED_NAME)))
				.addChildBindingPoint(n -> n
						.data()
						.format(SIMPLE)
						.name("dependencies")
						.occurrences(Range.between(0, 1))
						.type(base.derived().setType())
						.addChildBindingPoint(o -> o
								.data()
								.inputMethod("add")
								.name("element")
								.type(base.derived().importType())
								.dataType(Schema.class)
								.outputSelf()
								.occurrences(Range.between(0, null))
								.addChildBindingPoint(i -> i
										.data()
										.name("import")
										.addChildBindingPoint(p -> p.data().name("targetModel").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("schema", namespace))
												.buffer()))
										.addChildBindingPoint(p -> p.data().name("targetId").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("name", namespace))
												.buffer())))
								.addChildBindingPoint(p -> p
										.data()
										.name("dataTypes")
										.inputNone()
										.type(base.derived().includeType())
										.inputBindingType(Schema.class)
										.addChildBindingPoint(q -> q.inputSequence().name("dataTypes").chainedInput(true))
										.addChildBindingPoint(q -> q.data().name("targetModel").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("type", namespace))
												.buffer())))
								.addChildBindingPoint(p -> p
										.data()
										.name("models")
										.inputNone()
										.type(base.derived().includeType())
										.inputBindingType(Schema.class)
										.addChildBindingPoint(q -> q.inputSequence().name("models").chainedInput(true))
										.addChildBindingPoint(q -> q.data().name("targetModel").provideValue(new BufferingDataTarget()
												.put(Primitive.QUALIFIED_NAME, new QualifiedName("model", namespace))
												.buffer())))))
				.addChildBindingPoint(i -> i
						.data()
						.format(SIMPLE)
						.name("imports")
						.optional(true)
						.inputBindingStrategy(TARGET_ADAPTOR)
						.outputSelf()
						.dataType(Schema.class)
						.inputBindingType(SchemaConfigurator.class)
						.inputNone()
						.addChildBindingPoint(n -> n
								.data()
								.name("importsIn")
								.inputMethod("imports")
								.outputNone()
								.type(base.derived().setType())
								.addChildBindingPoint(e -> e.data().name("element").type(base.derived().classType())))
						.addChildBindingPoint(n -> n
								.data()
								.name("importsOut")
								.inputNone()
								.optional(true)
								.outputMethod("imports")
								.dataType(Imports.class)
								.addChildBindingPoint(s -> s
										.data()
										.name("imports")
										.outputMethod("getImportedClasses")
										.inputNone()
										.type(base.derived().setType())
										.addChildBindingPoint(e -> e.data().name("element").type(base.derived().classType())))))
				.addChildBindingPoint(n -> n
						.complex()
						.name("types")
						.outputSelf()
						.occurrences(Range.between(0, 1))
						.dataType(Schema.class)
						.inputBindingType(SchemaConfigurator.class)
						.inputNone()
						.inputBindingStrategy(TARGET_ADAPTOR)
						.addChildBindingPoint(o -> o
								.complex()
								.model(dataTypeModel)
								.inputNone()
								.inputBindingStrategy(TARGET_ADAPTOR)
								.outputMethod("dataTypes")
								.name("type")
								.dataType(new TypeToken<SimpleNode<?>>() {})
								.occurrences(Range.between(0, null))))
				.addChildBindingPoint(n -> n
						.complex()
						.name("models")
						.outputSelf()
						.occurrences(Range.between(0, 1))
						.dataType(Schema.class)
						.inputBindingType(SchemaConfigurator.class)
						.inputNone()
						.inputBindingStrategy(TARGET_ADAPTOR)
						.addChildBindingPoint(o -> o
								.complex()
								.model(metaModel)
								.inputNone()
								.inputBindingStrategy(TARGET_ADAPTOR)
								.outputMethod("models")
								.occurrences(Range.between(0, null))))
				.addChildBindingPoint(n -> n.inputSequence().name("create").chainedInput(true)));
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

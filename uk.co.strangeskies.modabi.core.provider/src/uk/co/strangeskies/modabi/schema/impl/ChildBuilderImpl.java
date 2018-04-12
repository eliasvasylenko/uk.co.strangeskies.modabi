package uk.co.strangeskies.modabi.schema.impl;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder;
import uk.co.strangeskies.modabi.schema.meta.AnonymousModelBuilder.ChildrenStep;
import uk.co.strangeskies.modabi.schema.meta.ChildBuilder;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBuilderImpl<E> implements ChildBuilder.PropertiesStep<E> {
  private static final Expression NO_IO = v -> {};

  private final SchemaBuilderImpl schemaBuilder;
  private final ChildBuilderContext<E> context;

  private final String name;

  private final Model<?> model;
  private final TypeToken<?> type;

  private final Expression input;
  private final Expression output;

  private final BindingConstraintSpecification bindingCondition;
  private final Boolean ordered;

  private final Model<?> modelOverride;
  private final AnonymousModelBuilder<?> modelOverrideBuilder;

  public ChildBuilderImpl(SchemaBuilderImpl schemaBuilder, ChildBuilderContext<E> context) {
    this.schemaBuilder = schemaBuilder;
    this.context = context;
    this.name = null;
    this.model = null;
    this.type = null;
    this.input = null;
    this.output = null;
    this.bindingCondition = null;
    this.ordered = null;
    this.modelOverride = null;
    this.modelOverrideBuilder = null;
  }

  public ChildBuilderImpl(
      SchemaBuilderImpl schemaBuilder,
      ChildBuilderContext<E> context,
      String name,
      Model<?> model,
      TypeToken<?> type,
      Expression inputExpression,
      Expression outputExpression,
      BindingConstraintSpecification bindingCondition,
      Boolean ordered,
      Model<?> modelOverride,
      AnonymousModelBuilder<?> modelOverrideBuilder) {
    this.schemaBuilder = schemaBuilder;
    this.context = context;
    this.name = name;
    this.model = model;
    this.type = type;
    this.input = inputExpression;
    this.output = outputExpression;
    this.bindingCondition = bindingCondition;
    this.ordered = ordered;
    this.modelOverride = modelOverride;
    this.modelOverrideBuilder = modelOverrideBuilder;
  }

  @Override
  public ChildBuilder.PropertiesStep<E> input(Expression input) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> noInput() {
    return input(NO_IO);
  }

  @Override
  public Optional<Expression> getInput() {
    return hasNoInput() ? Optional.empty() : Optional.ofNullable(input);
  }

  @Override
  public boolean hasNoInput() {
    return input == NO_IO;
  }

  @Override
  public ChildBuilder.PropertiesStep<E> output(Expression output) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> noOutput() {
    return output(NO_IO);
  }

  @Override
  public Optional<Expression> getOutput() {
    return hasNoOutput() ? Optional.empty() : Optional.ofNullable(output);
  }

  protected FunctionalExpressionCompiler getExpressionCompiler() {
    return schemaBuilder.getExpressionCompiler();
  }

  @Override
  public boolean hasNoOutput() {
    return output == NO_IO;
  }

  @Override
  public ChildBuilder.PropertiesStep<E> ordered(boolean ordered) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public Optional<Boolean> getOrdered() {
    return ofNullable(ordered);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> bindingConstraint(
      BindingConstraintSpecification bindingCondition) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public Optional<BindingConstraintSpecification> getBindingConstraint() {
    return ofNullable(bindingCondition);
  }

  @Override
  public E endChild() {
    return context.endChild(this);
  }

  @Override
  public Optional<String> getName() {
    return Optional.ofNullable(name);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> name(String name) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public Optional<QualifiedName> getModel() {
    return getModelImpl().map(Model::name);
  }

  public Optional<Model<?>> getModelImpl() {
    return Optional.ofNullable(model);
  }

  @Override
  public Optional<TypeToken<?>> getType() {
    return Optional.ofNullable(type);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> type(TypeToken<?> type) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        model,
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public ChildBuilder.PropertiesStep<E> model(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChildBuilder.PropertiesStep<E> model(QualifiedName modelName) {
    return new ChildBuilderImpl<>(
        schemaBuilder,
        context,
        name,
        schemaBuilder.getModel(modelName),
        type,
        input,
        output,
        bindingCondition,
        ordered,
        modelOverride,
        modelOverrideBuilder);
  }

  @Override
  public ChildrenStep<ChildBuilder<E>> overrideModel() {
    return new AnonymousModelBuilderImpl<>(
        schemaBuilder,
        nodeBuilder -> new ChildBuilderImpl<>(
            schemaBuilder,
            context,
            name,
            model,
            type,
            input,
            output,
            bindingCondition,
            ordered,
            new ModelImpl<>(nodeBuilder.getModelBuilder()),
            nodeBuilder));
  }

  @Override
  public Optional<AnonymousModelBuilder<?>> getModelOverride() {
    return Optional.ofNullable(modelOverrideBuilder);
  }
}

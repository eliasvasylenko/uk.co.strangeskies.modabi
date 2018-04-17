package uk.co.strangeskies.modabi.instruction;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.FieldToken.staticFields;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forNull;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionException;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

public class InstructionCompiler {
  private final Scope scope;

  public InstructionCompiler(Scope scope) {
    this.scope = scope;
  }

  public Instructions compile(Expression expression) {
    ExpressionVisitorImpl visitor = new ExpressionVisitorImpl(expression, scope);
    return visitor.instructions();
  }

  private class ExpressionVisitorImpl implements ExpressionVisitor {
    private final Scope scope;
    private final List<Instruction> instructionSequence;
    private TypeToken<?> type;
    private final Instructions instructions;

    public ExpressionVisitorImpl(Expression expression, Scope scope) {
      this.scope = scope;
      this.instructionSequence = new ArrayList<>();
      expression.evaluate(this);
      if (type == null)
        throw new ExpressionException(MESSAGES.expressionIsNotCompleted());
      instructions = new Instructions(type, instructionSequence);
    }

    private void complete(TypeToken<?> type) {
      if (this.type != null)
        throw new ExpressionException(MESSAGES.expressionIsAlreadyCompleted());
      this.type = requireNonNull(type);
    }

    public Instructions compileStep(Expression expression) {
      Instructions instructions = compile(expression);
      this.instructionSequence.add(instructions);
      return instructions;
    }

    public Instructions instructions() {
      return instructions;
    }

    private ExecutableToken<Object, ?> resolveInvocationOverload(
        Supplier<Stream<? extends ExecutableToken<?, ?>>> executables,
        List<Expression> arguments) {
      List<Instructions> argumentInstructions = arguments
          .stream()
          .map(a -> compile(a))
          .collect(toList());

      List<TypeToken<?>> argumentTypes = argumentInstructions
          .stream()
          .map(m -> m.resultType())
          .collect(toList());

      @SuppressWarnings("unchecked")
      ExecutableToken<Object, ?> executable = (ExecutableToken<Object, ?>) executables
          .get()
          .map(ExecutableToken::infer)
          .collect(resolveOverload(argumentTypes))
          .resolve();

      if (executable.isVariableArityInvocation()) {
        int argumentCount = arguments.size();
        int nonVarargsCount = (int) executable.getParameters().count() - 1;
        int varargsCount = argumentCount - nonVarargsCount;

        for (int i = 0; i < nonVarargsCount; i++) {
          instructionSequence.add(argumentInstructions.get(i));
        }

        TypeToken<?> arrayType = executable
            .getParameters()
            .reduce((a, b) -> b)
            .get()
            .getTypeToken();

        instructionSequence.add(v -> v.visitLiteral(varargsCount));
        instructionSequence.add(v -> v.newArray(arrayType));

        for (int i = nonVarargsCount; i < argumentCount; i++) {
          int index = i;
          instructionSequence.add(v -> v.visitDuplicate());
          instructionSequence.add(v -> v.visitLiteral(index));
          instructionSequence.add(argumentInstructions.get(i));
          instructionSequence.add(v -> v.visitArrayStore());
        }
      } else {
        argumentInstructions.forEach(instructionSequence::add);
      }

      return executable;
    }

    @Override
    public <U> void visitStaticInvocation(
        Class<U> type,
        String method,
        List<Expression> arguments) {
      ExecutableToken<Object, ?> executable = resolveInvocationOverload(
          () -> staticMethods(type).filter(anyMethod().named(method)),
          arguments);

      instructionSequence.add(v -> v.visitStaticInvocation(executable));
      complete(executable.getReturnType());
    }

    @Override
    public void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
      Instructions receiverInstructions = compileStep(receiver);

      ExecutableToken<Object, ?> executable = resolveInvocationOverload(
          () -> receiverInstructions.resultType().methods().filter(anyMethod().named(method)),
          arguments);

      instructionSequence.add(v -> v.visitMemberInvocation(executable));
      complete(executable.getReturnType());
    }

    @Override
    public <U> void visitConstructorInvocation(Class<U> type, List<Expression> arguments) {
      ExecutableToken<Object, ?> executable = resolveInvocationOverload(
          () -> forClass(type).infer().constructors(),
          arguments);

      instructionSequence.add(v -> v.visitConstructorInvocation(executable));
      complete(executable.getReturnType());
    }

    @Override
    public <U> void visitCast(TypeToken<U> type, Expression value) {
      Instructions valueInstructions = compileStep(value);

      /*
       * TODO cast check is not yet supported, must imply necessary bounds
       */
      // if (!type.isCastableFrom(valueMetadata.type))
      // throw new ExpressionException(MESSAGES.cannotPerformCast(type,
      // valueMetadata.type));

      instructionSequence.add(valueInstructions);
      complete(valueInstructions.resultType());
    }

    @Override
    public <U> void visitCheck(TypeToken<U> type, Expression value) {
      Instructions valueInstructions = compileStep(value);

      /*
       * TODO check is not yet supported, must imply necessary bounds
       */
      // if (!type.isCastableFrom(valueMetadata.type))
      // throw new ExpressionException(MESSAGES.cannotPerformCast(type,
      // valueMetadata.type));

      instructionSequence.add(valueInstructions);
      complete(valueInstructions.resultType());
    }

    @Override
    public void visitField(Expression receiver, String variable) {
      Instructions receiverInstructions = compileStep(receiver);

      FieldToken<?, ?> field = receiverInstructions
          .resultType()
          .fields()
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(
                  MESSAGES.cannotResolveField(receiverInstructions.resultType(), variable)));

      instructionSequence.add(v -> v.visitGetField(field));
      complete(field.getFieldType());
    }

    @Override
    public void visitFieldAssignment(Expression receiver, String variable, Expression value) {
      Instructions receiverInstructions = compileStep(receiver);
      Instructions valueInstructions = compileStep(value);

      FieldToken<?, ?> field = receiverInstructions
          .resultType()
          .fields()
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(
                  MESSAGES.cannotResolveField(receiverInstructions.resultType(), variable)));

      if (!field
          .getFieldType()
          .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueInstructions.resultType()))
        throw new ExpressionException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), valueInstructions.resultType()));

      instructionSequence.add(v -> v.visitPutField(field));
      complete(field.getFieldType());
    }

    @Override
    public void visitStaticField(Class<?> type, String variable) {
      FieldToken<?, ?> field = staticFields(type)
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(MESSAGES.cannotResolveStaticField(type, variable)));

      instructionSequence.add(v -> v.visitGetStaticField(field));
      complete(field.getFieldType());
    }

    @Override
    public void visitStaticFieldAssignment(Class<?> type, String variable, Expression value) {
      Instructions valueInstructions = compileStep(value);

      FieldToken<Void, ?> field = staticFields(type)
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(MESSAGES.cannotResolveStaticField(type, variable)));

      if (!field
          .getFieldType()
          .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueInstructions.resultType()))
        throw new ExpressionException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), valueInstructions.resultType()));

      instructionSequence.add(v -> v.visitPutField(field));
      complete(field.getFieldType());
    }

    @Override
    public void visitNull() {
      instructionSequence.add(v -> v.visitNull());
      complete(forNull());
    }

    @Override
    public void visitLiteral(Object value) {
      if (value instanceof Class<?>) {
        instructionSequence.add(v -> v.visitLiteral((Class<?>) value));

      } else if (value instanceof Integer) {
        instructionSequence.add(v -> v.visitLiteral((int) value));

      } else if (value instanceof Long) {
        instructionSequence.add(v -> v.visitLiteral((long) value));

      } else if (value instanceof Float) {
        instructionSequence.add(v -> v.visitLiteral((float) value));

      } else if (value instanceof Double) {
        instructionSequence.add(v -> v.visitLiteral((double) value));

      } else if (value instanceof String) {
        instructionSequence.add(v -> v.visitLiteral((String) value));

      } else {
        throw new ExpressionException(MESSAGES.illegalLiteralType(value.getClass()));
      }

      complete(forClass(value.getClass()));
    }

    @Override
    public void visitIteration(Expression value) {
      Instructions iterable = compileStep(value);

      // TODO special cases for arrays and streams
      TypeToken<?> itemType = iterable
          .resultType()
          .resolveSupertype(Iterable.class)
          .getTypeArguments()
          .findAny()
          .get()
          .getTypeToken();
      complete(itemType);
    }

    @Override
    public void visitNamed(String name) {
      Instructions variableInstructions = scope.lookupVariable(name);

      instructionSequence.add(variableInstructions);
      complete(variableInstructions.resultType());
    }

    @Override
    public void visitNamedAssignment(String name, Expression value) {
      Instructions valueInstructions = compile(value);
      Instructions variableInstructions = scope.lookupVariableAssignment(name, valueInstructions);

      instructionSequence.add(variableInstructions);
      complete(variableInstructions.resultType());
    }

    @Override
    public void visitNamedInvocation(String name, List<Expression> arguments) {
      List<Instructions> argumentInstructions = arguments
          .stream()
          .map(a -> compile(a))
          .collect(toList());
      Instructions invocationInstructions = scope.lookupInvocation(name, argumentInstructions);

      instructionSequence.add(invocationInstructions);
      complete(invocationInstructions.resultType());
    }
  }
}

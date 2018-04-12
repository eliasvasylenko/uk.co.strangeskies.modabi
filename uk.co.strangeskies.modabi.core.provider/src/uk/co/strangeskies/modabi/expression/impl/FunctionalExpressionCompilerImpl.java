package uk.co.strangeskies.modabi.expression.impl;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forNull;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionException;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.functional.FunctionCapture;
import uk.co.strangeskies.modabi.expression.functional.FunctionImplementation;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.expression.functional.Named;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@Component
public class FunctionalExpressionCompilerImpl implements FunctionalExpressionCompiler {
  @Override
  public <T> FunctionImplementation<T> compile(
      Expression expression,
      TypeToken<T> implementationType) {
    return compile(expression, implementationType, forClass(void.class)).capture(null);
  }

  @Override
  public <T, C> FunctionCapture<C, T> compile(
      Expression expression,
      TypeToken<T> implementationType,
      TypeToken<C> captureScope) {
    Class<?> implementationClass = implementationType.getErasedType();

    if (!implementationClass.isInterface())
      throw new ExpressionException(
          MESSAGES.typeMustBeFunctionalInterface(implementationType.getType()));

    ExecutableToken<T, ?> executable = stream(implementationClass.getMethods())
        .filter(m -> !m.isDefault() && !isStatic(m.getModifiers()))
        .reduce(
            throwingReduce(
                (a, b) -> new ExpressionException(
                    MESSAGES.typeMustBeFunctionalInterface(implementationType.getType()))))
        .map(ExecutableToken::forMethod)
        .map(e -> e.withReceiverType(implementationType))
        .orElseThrow(
            () -> new ExpressionException(
                MESSAGES.typeMustBeFunctionalInterface(implementationType.getType())));

    ExpressionVisitorImpl<T, C> visitor = new ExpressionVisitorImpl<>(executable, captureScope);

    System.out.println("=======================================");
    TypeToken<?> returnType = visitor.compileStep(expression).type;
    System.out.println("*************** " + returnType);
    List<Instructions> instructions = new ArrayList<>(visitor.instructions);

    /*
     * TODO this is wrong! the returnType needs to be a lower bound of the
     * executable return type, but withTargetType uses it as an upper bound.
     */
    // executable = executable.withTargetType(returnType);

    /*
     * TODO so now we have the return type and the compiled steps...
     */

    return new FunctionCapture<C, T>() {
      @Override
      public FunctionImplementation<T> capture(C capture) {
        @SuppressWarnings("unchecked")
        T result = (T) newProxyInstance(
            implementationType.getErasedType().getClassLoader(),
            new Class<?>[] { implementationType.getErasedType() },
            (proxy, method, args) -> {
              /*
               * TODO delegate to default method implementations
               */
              ExecutionContext context = new ExecutionContext(instructions, capture, args);
              context.next();
              return context.pop();
            });

        return new FunctionImplementation<T>() {
          @Override
          public T getInstance() {
            return result;
          }

          @Override
          public BoundSet getBounds() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public TypeToken<T> getResolvedFunctionType() {
            // TODO Auto-generated method stub
            return null;
          }

          @Override
          public TypeToken<T> getFunctionType() {
            // TODO Auto-generated method stub
            return null;
          }
        };
      }

      @Override
      public BoundSet getBounds() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<C> getCaptureType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<T> getFunctionType() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public TypeToken<T> getResolvedFunctionType() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }

  private static class ExpressionVisitorImpl<T, C> implements ExpressionVisitor {
    private final ExecutableToken<T, ?> executable;
    private final TypeToken<C> captureScope;

    private final List<Instructions> instructions;
    private final List<InstructionDescription> stack;
    private int maximumStackSize;

    public ExpressionVisitorImpl(ExecutableToken<T, ?> executable, TypeToken<C> captureScope) {
      this.executable = executable;
      this.captureScope = captureScope;

      this.instructions = new ArrayList<>();
      this.stack = new ArrayList<>();
    }

    private void completeStep(TypeToken<?> type, Instructions step) {
      instructions.add(step);
      stack.add(new InstructionDescription(type));
      maximumStackSize = Math.max(maximumStackSize, stack.size());
    }

    private InstructionDescription compileStep(Expression expression) {
      expression.evaluate(this);
      return stack.remove(stack.size() - 1);
    }

    private List<InstructionDescription> compileAllSteps(Collection<Expression> expressions) {
      expressions.forEach(e -> e.evaluate(this));

      ArrayList<InstructionDescription> metadata = new ArrayList<>(expressions.size());
      int cutFrom = stack.size() - expressions.size();
      for (int i = 0; i < expressions.size(); i++)
        metadata.add(stack.remove(cutFrom));

      return metadata;
    }

    private <U> void visitInvocationImpl(
        InstructionDescription receiver,
        Supplier<Stream<? extends ExecutableToken<?, ?>>> executables,
        List<Expression> arguments) {
      List<InstructionDescription> compiledArguments = compileAllSteps(arguments);

      List<TypeToken<?>> argumentTypes = compiledArguments
          .stream()
          .map(m -> m.type)
          .collect(toList());

      @SuppressWarnings("unchecked")
      ExecutableToken<Object, ?> executable = (ExecutableToken<Object, ?>) executables
          .get()
          .map(ExecutableToken::infer)
          .collect(resolveOverload(argumentTypes))
          .resolve();

      System.out.println(" %   " + executable);

      int actualArgumentCount = (int) executable.getParameters().count();
      if (executable.isVariableArityInvocation()) {
        int arraySize = arguments.size() - actualArgumentCount + 1;
        instructions.add(c -> {
          Object[] varargs = new Object[arraySize];
          for (int i = varargs.length - 1; i >= 0; i--) {
            varargs[i] = c.pop();
          }
          c.push(varargs);
        });
      }

      if (receiver != null) {
        completeStep(
            executable.getReturnType(),
            c -> c.push(executable.invoke(c.pop(), c.pop(actualArgumentCount))));
      } else {
        completeStep(
            executable.getReturnType(),
            c -> c.push(executable.invoke(null, c.pop(actualArgumentCount))));
      }
    }

    @Override
    public <U> void visitStaticInvocation(
        Class<U> type,
        String method,
        List<Expression> arguments) {
      System.out.println("s " + type + "." + method);

      visitInvocationImpl(
          null,
          () -> staticMethods(type).filter(anyMethod().named(method)),
          arguments);
    }

    @Override
    public void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
      InstructionDescription receiverMetadata = compileStep(receiver);

      System.out.println("m " + receiverMetadata.type.getErasedType() + "." + method);

      visitInvocationImpl(
          receiverMetadata,
          () -> receiverMetadata.type.methods().filter(anyMethod().named(method)),
          arguments);
    }

    @Override
    public <U> void visitConstructorInvocation(Class<U> type, List<Expression> arguments) {
      System.out.println("c " + type);

      visitInvocationImpl(null, () -> forClass(type).infer().constructors(), arguments);
    }

    @Override
    public <U> void visitCast(TypeToken<U> type, Expression value) {
      InstructionDescription valueMetadata = compileStep(value);

      /*
       * TODO cast check is not yet supported, must imply necessary bounds
       */
      // if (!type.isCastableFrom(valueMetadata.type))
      // throw new ExpressionException(MESSAGES.cannotPerformCast(type,
      // valueMetadata.type));

      completeStep(type, c -> {});
    }

    @Override
    public <U> void visitCheck(TypeToken<U> type, Expression value) {
      InstructionDescription valueMetadata = compileStep(value);

      /*
       * TODO check is not yet supported, must imply necessary bounds
       */
      // if (!type.isCastableFrom(valueMetadata.type))
      // throw new ExpressionException(MESSAGES.cannotPerformCast(type,
      // valueMetadata.type));

      completeStep(valueMetadata.type, c -> {});
    }

    private void visitFieldImpl(InstructionDescription receiver, String variable) {
      @SuppressWarnings("unchecked")
      FieldToken<Object, ?> field = (FieldToken<Object, ?>) receiver.type
          .fields()
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(MESSAGES.cannotResolveField(receiver.type, variable)));

      completeStep(field.getFieldType(), c -> c.push(field.get(c.pop())));
    }

    @Override
    public void visitField(Expression receiver, String variable) {
      InstructionDescription receiverMetadata = compileStep(receiver);
      visitFieldImpl(receiverMetadata, variable);
    }

    private void visitFieldAssignmentImpl(
        InstructionDescription receiver,
        String variable,
        Expression value) {
      InstructionDescription valueMetadata = compileStep(value);

      @SuppressWarnings("unchecked")
      FieldToken<Object, Object> field = (FieldToken<Object, Object>) receiver.type
          .fields()
          .filter(anyVariable().named(variable))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(MESSAGES.cannotResolveField(receiver.type, variable)));

      if (!field.getFieldType().satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueMetadata.type))
        throw new ExpressionException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), valueMetadata.type));

      completeStep(field.getFieldType(), c -> {
        Object v = c.pop();
        field.set(c.pop(), v);
        c.push(v);
      });
    }

    @Override
    public void visitFieldAssignment(Expression receiver, String variable, Expression value) {
      InstructionDescription receiverMetadata = compileStep(receiver);
      visitFieldAssignmentImpl(receiverMetadata, variable, value);
    }

    @Override
    public void visitNull() {
      completeStep(forNull(), c -> c.push(null));
    }

    @Override
    public void visitLiteral(Object value) {
      completeStep(forClass(value.getClass()), c -> c.push(value));
    }

    @Override
    public void visitIteration(Expression value) {
      InstructionDescription iterable = compileStep(value);

      // TODO special cases for arrays and streams
      TypeToken<?> itemType = iterable.type
          .resolveSupertype(Iterable.class)
          .getTypeArguments()
          .findAny()
          .get()
          .getTypeToken();
      completeStep(itemType, c -> {
        Iterable<?> i = (Iterable<?>) c.pop();
        for (Object o : i) {
          c.push(o);
          c.next();
        }
      });
    }

    @Override
    public void visitNamed(String variable) {
      ExecutableParameter parameter = executable
          .getParameters()
          .filter(p -> variable.equals(getName(p)))
          .findAny()
          .orElse(null);

      if (parameter != null) {
        int index = asList(parameter.getParameter().getDeclaringExecutable().getParameters())
            .indexOf(parameter.getParameter());

        completeStep(parameter.getTypeToken(), c -> c.pushArgument(index));
      } else {
        InstructionDescription receiverMetadata = compileCaptureStep();
        visitFieldImpl(receiverMetadata, variable);
      }
    }

    private Object getName(ExecutableParameter parameter) {
      if (parameter.getParameter().isNamePresent())
        return parameter.getName();

      if (parameter.getParameter().isAnnotationPresent(Named.class))
        return parameter.getParameter().getAnnotation(Named.class).value();

      return null;
    }

    @Override
    public void visitNamedAssignment(String variable, Expression value) {
      /*
       * TODO if the variable name matches a parameter, assign to that! else:
       */

      InstructionDescription receiverMetadata = compileCaptureStep();
      visitFieldAssignmentImpl(receiverMetadata, variable, value);
    }

    private InstructionDescription compileCaptureStep() {
      instructions.add(c -> c.pushCapture());
      return new InstructionDescription(captureScope);
    }

    @Override
    public void visitNamedInvocation(String method, List<Expression> arguments) {
      InstructionDescription receiverMetadata = compileCaptureStep();
      visitInvocationImpl(
          receiverMetadata,
          () -> receiverMetadata.type.methods().filter(anyMethod().named(method)),
          arguments);
    }
  }
}

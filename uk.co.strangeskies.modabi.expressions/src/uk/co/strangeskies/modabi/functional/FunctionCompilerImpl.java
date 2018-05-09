package uk.co.strangeskies.modabi.functional;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.modabi.expression.Expressions.argumentInstructionSequence;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionException;
import uk.co.strangeskies.modabi.expression.InstructionVisitor;
import uk.co.strangeskies.modabi.expression.Instructions;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.expression.Scope;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@Component
public class FunctionCompilerImpl implements FunctionCompiler {
  @Override
  public <T, C> FunctionCapture<C, T> compile(
      Expression expression,
      Preprocessor preprocessor,
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

    Scope scope = preprocessor.decorateScope(new FunctionScope(executable, captureScope));

    Instructions instructions = expression.compile(scope);

    TypeToken<?> returnType = instructions.getResultType();
    System.out.println(" #RET - " + returnType);

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

              InstructionVisitorImpl<T, C> visitor = new InstructionVisitorImpl<>(
                  executable,
                  captureScope,
                  context);

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

  private static class FunctionScope implements Scope {
    private static final String PARAMETER = "parameter";
    private static final String CAPTURE = "capture";

    private final ExecutableToken<?, ?> executable;
    private final TypeToken<?> captureScope;

    public FunctionScope(ExecutableToken<?, ?> executable, TypeToken<?> captureScope) {
      this.executable = executable;
      this.captureScope = captureScope;
    }

    private Instructions getCapture() {
      return new Instructions(captureScope, w -> w.getNamed(CAPTURE));
    }

    @Override
    public Instructions lookupVariable(String variableName) {
      ExecutableParameter parameter = getParameter(executable, variableName);

      if (parameter != null) {
        return new Instructions(parameter.getTypeToken(), v -> v.getNamed(PARAMETER));

      } else {
        FieldToken<?, ?> field = captureScope
            .fields()
            .filter(anyVariable().named(variableName))
            .findAny()
            .orElseThrow(
                () -> new ExpressionException(
                    MESSAGES.cannotResolveField(captureScope, variableName)));

        return new Instructions(field.getFieldType(), v -> v.getMember(getCapture(), field));
      }
    }

    @Override
    public Instructions lookupVariableAssignment(String variableName, Instructions value) {
      FieldToken<?, ?> field = captureScope
          .fields()
          .filter(anyVariable().named(variableName))
          .findAny()
          .orElseThrow(
              () -> new ExpressionException(
                  MESSAGES.cannotResolveField(captureScope, variableName)));

      if (!field
          .getFieldType()
          .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, value.getResultType()))
        throw new ExpressionException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), value.getResultType()));

      return new Instructions(field.getFieldType(), v -> v.putMember(getCapture(), field, value));
    }

    @Override
    public Instructions lookupInvocation(String invocationName, List<Instructions> arguments) {
      Instructions capture = getCapture();

      List<TypeToken<?>> argumentTypes = arguments
          .stream()
          .map(m -> m.getResultType())
          .collect(toList());

      ExecutableToken<?, ?> executable = capture
          .getResultType()
          .methods()
          .filter(anyMethod().named(invocationName))
          .map(ExecutableToken::infer)
          .collect(resolveOverload(argumentTypes))
          .resolve();

      List<Instructions> varArgs = argumentInstructionSequence(executable, arguments);

      return new Instructions(
          executable.getReturnType(),
          v -> v.invokeMember(capture, executable, varArgs));
    }
  }

  public class ExecutionContext {
    private final Instructions instructions;
    private final Object capture;
    private final Object[] arguments;

    private int instructionPointer;
    private final Deque<Object> stack;

    public ExecutionContext(Instructions instructions, Object capture, Object[] arguments) {
      this.instructionPointer = 0;
      this.instructions = instructions;

      this.capture = capture;
      this.arguments = arguments;

      this.stack = new ArrayDeque<>();
    }

    public void push(Object item) {
      stack.push(item);
    }

    public List<Object> pop(int count) {
      List<Object> list = new ArrayList<>(count);
      for (int i = 0; i < count; i++)
        list.add(0, stack.pop());
      return list;
    }

    public Object pop() {
      return stack.pop();
    }

    public Object peek() {
      return stack.peek();
    }

    public void next() {
      int i;
      do {
        i = instructionPointer;
        //Finstructions.get(i).visit(this);
      } while (i == instructionPointer++);
    }

    public Object pushCapture() {
      return capture;
    }

    public Object pushArgument(int i) {
      return arguments[i];
    }
  }

  private static class InstructionVisitorImpl<T, C> implements InstructionVisitor {
    private final ExecutableToken<T, ?> executable;
    private final TypeToken<C> captureScope;
    private final ExecutionContext executionContext;

    public InstructionVisitorImpl(
        ExecutableToken<T, ?> executable,
        TypeToken<C> captureScope,
        ExecutionContext executionContext) {
      this.executable = executable;
      this.captureScope = captureScope;
      this.executionContext = executionContext;
    }

    @Override
    public void invokeMember(
        Instructions receiver,
        ExecutableToken<?, ?> method,
        List<Instructions> arguments) {
      // TODO Auto-generated method stub

    }

    @Override
    public void invokeStatic(ExecutableToken<?, ?> method, List<Instructions> arguments) {
      // TODO Auto-generated method stub

    }

    @Override
    public void invokeConstructor(ExecutableToken<?, ?> method, List<Instructions> arguments) {
      // TODO Auto-generated method stub

    }

    @Override
    public void invokeNamed(String name, List<Instructions> arguments) {
      // TODO Auto-generated method stub

    }

    @Override
    public void getMember(Instructions receiver, FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void getStatic(FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void getNamed(String name) {
      ExecutableParameter parameter = getParameter(executable, name);

      int index = asList(parameter.getParameter().getDeclaringExecutable().getParameters())
          .indexOf(parameter.getParameter());

    }

    @Override
    public void putMember(Instructions receiver, FieldToken<?, ?> field, Instructions value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void putStatic(FieldToken<?, ?> field, Instructions value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void putNamed(String name, Instructions value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void newArray(TypeToken<?> type, int size) {
      // TODO Auto-generated method stub

    }

    @Override
    public void newArray(TypeToken<?> type, List<Instructions> instructions) {
      // TODO Auto-generated method stub

    }

    @Override
    public void nullLiteral() {
      // TODO Auto-generated method stub

    }

    @Override
    public void intLiteral(int value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void longLiteral(long value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void floatLiteral(float value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void doubleLiteral(double value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void stringLiteral(String value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void classLiteral(Class<?> value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void iterate(Instructions instructions) {
      // TODO Auto-generated method stub

    }

    @Override
    public void cast(TypeToken<?> resultType, Instructions value) {
      // TODO Auto-generated method stub

    }
  }

  private static Object getParameterName(ExecutableParameter parameter) {
    if (parameter.getParameter().isNamePresent())
      return parameter.getName();

    if (parameter.getParameter().isAnnotationPresent(Named.class))
      return parameter.getParameter().getAnnotation(Named.class).value();

    return null;
  }

  private static ExecutableParameter getParameter(ExecutableToken<?, ?> executable, String name) {
    return executable
        .getParameters()
        .filter(p -> name.equals(getParameterName(p)))
        .findAny()
        .orElse(null);
  }
}

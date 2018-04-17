package uk.co.strangeskies.modabi.functional;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.modabi.expression.ExpressionException.MESSAGES;
import static uk.co.strangeskies.modabi.instruction.InstructionCompiler.forScope;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionException;
import uk.co.strangeskies.modabi.instruction.Instruction;
import uk.co.strangeskies.modabi.instruction.InstructionVisitor;
import uk.co.strangeskies.modabi.instruction.Instructions;
import uk.co.strangeskies.modabi.instruction.Scope;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

@Component
public class FunctionCompilerImpl implements FunctionCompiler {
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

    Instructions instructions = forScope(new ScopeImpl(executable, captureScope))
        .compile(expression);

    TypeToken<?> returnType = instructions.resultType();

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
              InstructionVisitorImpl<T, C> visitor = new InstructionVisitorImpl<>(
                  executable,
                  captureScope,
                  executionContext);
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

  private static class ScopeImpl implements Scope {
    private static final String PARAMETER = "parameter";
    private static final String CAPTURE = "capture";

    private final ExecutableToken<?, ?> executable;
    private final TypeToken<?> captureScope;

    public ScopeImpl(ExecutableToken<?, ?> executable, TypeToken<?> captureScope) {
      this.executable = executable;
      this.captureScope = captureScope;
    }

    @Override
    public Instructions lookupVariable(String variableName) {
      ExecutableParameter parameter = executable
          .getParameters()
          .filter(p -> variableName.equals(getParameterName(p)))
          .findAny()
          .orElse(null);

      if (parameter != null) {
        int index = asList(parameter.getParameter().getDeclaringExecutable().getParameters())
            .indexOf(parameter.getParameter());

        return new Instructions(
            parameter.getTypeToken(),
            v -> v.visitLiteral(index),
            v -> v.visitGetNamed(PARAMETER));
      } else {
        FieldToken<?, ?> field = captureScope
            .fields()
            .filter(anyVariable().named(variableName))
            .findAny()
            .orElseThrow(
                () -> new ExpressionException(
                    MESSAGES.cannotResolveField(captureScope, variableName)));

        return new Instructions(
            field.getFieldType(),
            v -> v.visitGetNamed(CAPTURE),
            v -> v.visitGetField(field));
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

      if (!field.getFieldType().satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, value.resultType()))
        throw new ExpressionException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), value.resultType()));

      return new Instructions(
          field.getFieldType(),
          v -> v.visitGetNamed(CAPTURE),
          v -> v.visitPutField(field));
    }

    @Override
    public Instructions lookupInvocation(String invocationName, List<Instructions> arguments) {
      InstructionDescription receiverMetadata = compileCaptureStep();
      visitInvocationImpl(
          receiverMetadata,
          () -> receiverMetadata.type.methods().filter(anyMethod().named(method)),
          arguments);
    }

    private Object getParameterName(ExecutableParameter parameter) {
      if (parameter.getParameter().isNamePresent())
        return parameter.getName();

      if (parameter.getParameter().isAnnotationPresent(Named.class))
        return parameter.getParameter().getAnnotation(Named.class).value();

      return null;
    }
  }

  public class ExecutionContext {
    private final List<Instruction> instructions;
    private final Object capture;
    private final Object[] arguments;

    private int instructionPointer;
    private final Deque<Object> stack;

    public ExecutionContext(List<Instruction> instructions, Object capture, Object[] arguments) {
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
        instructions.get(i).visit(this);
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
    public void visitMemberInvocation(ExecutableToken<?, ?> method) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitStaticInvocation(ExecutableToken<?, ?> method) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitConstructorInvocation(ExecutableToken<?, ?> method) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitNamedInvocation(String name, int arguments) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitGetField(FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitGetStaticField(FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitGetNamed(String name) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitPutField(FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitPutStaticField(FieldToken<?, ?> field) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitPutNamed(String name) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitNull() {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitCapture() {
      // TODO Auto-generated method stub

    }

    @Override
    public void newArray(TypeToken<?> type) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(int value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(long value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(float value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(double value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(String value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitLiteral(Class<?> value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitDuplicate() {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitArrayStore() {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitIterate() {
      // TODO Auto-generated method stub

    }
  }
}

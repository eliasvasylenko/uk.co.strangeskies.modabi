package uk.co.strangeskies.modabi.expression.impl;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.modabi.ModabiException.MESSAGES;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forNull;

import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.binding.impl.BindingContextImpl;
import uk.co.strangeskies.modabi.expression.CaptureFunction;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.Expressions;
import uk.co.strangeskies.modabi.expression.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.expression.Scope;
import uk.co.strangeskies.reflection.token.FieldToken;
import uk.co.strangeskies.reflection.token.TypeToken;

/*
 * 
 * 
 * 
 * TODO plan of implementation:
 * 
 * - for now compile expressions after the fact, when we already
 * have the full node graph built. This is easier as we don't have
 * to worry about expressions which block on 
 * 
 * 
 * 
 * 
 */
public class FunctionalExpressionCompilerImpl implements FunctionalExpressionCompiler {
  @Override
  public <T> T compile(Expression expression, TypeToken<T> implementationType) {
    return compile(expression, implementationType, v -> null).capture(null);
  }

  @Override
  public <T, C> CaptureFunction<C, T> compile(
      Expression expression,
      TypeToken<T> implementationType,
      Scope<C> captureScope) {
    ExpressionVisitorImpl<T, C> visitor = new ExpressionVisitorImpl<>(
        new CompilationTarget<>(expression, implementationType, captureScope));
    expression.evaluate(visitor);
    CompiledExpression<?> compiled = visitor.getCompiledExpression();
    return null; // TODO
  }

  private static class CompilationTarget<T, C> {
    public final Expression expression;
    public final TypeToken<T> implementationType;
    public final Scope<C> captureScope;

    public CompilationTarget(
        Expression expression,
        TypeToken<T> implementationType,
        Scope<C> captureScope) {
      this.expression = expression;
      this.implementationType = implementationType;
      this.captureScope = captureScope;
    }
  }

  private static class ExpressionVisitorImpl<T, C> implements ExpressionVisitor {
    CompilationTarget<T, C> target;
    CompiledExpression<?> compiled;

    public ExpressionVisitorImpl(CompilationTarget<T, C> target) {
      this.target = target;
    }

    public CompiledExpression<?> getCompiledExpression() {
      if (compiled == null)
        throw new IllegalStateException(); // TODO error message
      return compiled;
    }

    @SuppressWarnings("unchecked")
    public <U> void complete(TypeToken<U> type, Function<BindingContextImpl, Object> function) {
      if (compiled != null)
        throw new IllegalStateException();
      this.compiled = new CompiledExpression<>(type, (Function<BindingContextImpl, U>) function);
    }

    public List<CompiledExpression<?>> compileAll(List<Expression> valueExpressions) {
      return valueExpressions.stream().map(FunctionalExpressionCompilerImpl.this::compile).collect(
          toList());
    }

    @Override
    public <U> void visitStaticInvocation(
        Class<U> type,
        String method,
        List<Expression> arguments) {
      List<CompiledExpression<?>> expressions = compileAll(arguments);
    }

    @Override
    public void visitInvocation(Expression receiver, String method, List<Expression> arguments) {
      List<CompiledExpression<?>> expressions = compileAll(arguments);
    }

    @Override
    public <U> void visitConstructorInvocation(Class<U> type, List<Expression> arguments) {
      List<CompiledExpression<?>> expressions = compileAll(arguments);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> void visitCast(TypeToken<U> type, Expression value) {
      CompiledExpression<?> castFrom = compile(value);

      if (!type.isCastableFrom(castFrom.getType()))
        throw new ModabiException(MESSAGES.cannotPerformCast(type, castFrom.getType()));

      complete(type, a -> (U) castFrom.getFunction().apply(a));
    }

    @Override
    public void visitField(Expression receiver, String variable) {
      CompiledExpression<?> receiverExpression = compile(receiver);
      Function<BindingContextImpl, ?> receiverFunction = receiverExpression.getFunction();

      // TODO resolve the properly! Don't just take random member...
      FieldToken<?, ?> field = receiverExpression.getType().fields().findAny().get();

      complete(field.getFieldType(), c -> field.get(receiverFunction.apply(c)));
    }

    @Override
    public void visitFieldAssignment(Expression receiver, String variable, Expression value) {
      CompiledExpression<?> receiverExpression = compile(receiver);
      Function<BindingContextImpl, ?> receiverFunction = receiverExpression.getFunction();

      CompiledExpression<?> valueExpression = compile(value);
      Function<BindingContextImpl, ?> valueFunction = valueExpression.getFunction();

      // TODO resolve the properly! Don't just take random member...
      FieldToken<?, ?> field = receiverExpression.getType().fields().findAny().get();

      if (!field
          .getFieldType()
          .satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, valueExpression.getType()))
        throw new ModabiException(
            MESSAGES.cannotPerformAssignment(field.getFieldType(), valueExpression.getType()));

      complete(
          field.getFieldType(),
          c -> field.set(receiverFunction.apply(c), valueFunction.apply(c)));
    }

    @Override
    public void visitNull() {
      complete(forNull(), c -> null);
    }

    @Override
    public void visitLiteral(Object value) {
      complete(forClass(value.getClass()), c -> value);
    }

    @Override
    public void visitIteration(Expression value) {
      // TODO Auto-generated method stub

    }

    @Override
    public void visitNamed(String name) {
      switch (name) {
      case Expressions.CONTEXT_VALUE:
        break;
      case Expressions.PARENT_VALUE:
        break;
      case Expressions.RESULT_VALUE:
        break;
      case Expressions.SOURCE_VALUE:
        break;
      case Expressions.TARGET_VALUE:
        break;
      default:
        throw new ModabiException(MESSAGES.cannotResolveVariable(name));
      }
    }

    @Override
    public void visitNamedAssignment(String name, Expression value) {
      switch (name) {
      case Expressions.CONTEXT_VALUE:
        break;
      case Expressions.PARENT_VALUE:
        break;
      case Expressions.RESULT_VALUE:
        break;
      case Expressions.SOURCE_VALUE:
        break;
      case Expressions.TARGET_VALUE:
        break;
      default:
        throw new ModabiException(MESSAGES.cannotResolveVariable(name));
      }
    }

    @Override
    public void visitNamedInvocation(String name, List<Expression> arguments) {
      // TODO Auto-generated method stub

    }
  }
}

package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static uk.co.strangeskies.modabi.schema.BindingExpressions.BINDING_POINT_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BINDING_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.text.parsing.Parser.matchingAll;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.impl.NodeBuilderImpl;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.parsing.Parser;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ExpressionVisitor expressionVisitor;
  private final NodeBuilderImpl<?> bindingNode;
  private final Parser<QualifiedName> qualifiedNameParser;

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      NodeBuilderImpl<?> bindingNode,
      Parser<QualifiedName> qualifiedNameParser) {
    this.expressionVisitor = expressionVisitor;
    this.bindingNode = bindingNode;
    this.qualifiedNameParser = qualifiedNameParser;
  }

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      NodeBuilderImpl<?> bindingNode) {
    this(expressionVisitor, bindingNode, matchingAll(QualifiedName::parseString));
  }

  @Override
  public ExpressionVisitor visitor() {
    return expressionVisitor;
  }

  public ChildBindingPoint<?> findBindingPoint(String name) {
    /*
     * TODO find an already built binding point, presumably through the owning
     * builder since it can keep track of what's already been built.
     * 
     * Does this mean we have to do things in order? Yes probably. We should be able
     * to look through all children of the current node as well as all previous
     * nodes (and all children of previous nodes).
     */

    if (name.isEmpty()) {
      // TODO it's the binding point currently being built!
      return null;
    } else {
      // TODO it's the named binding point!
      QualifiedName qualifiedName = qualifiedNameParser.parse(name);
      return null;
    }
  }

  private Expression rawBindingPointExpression(ChildBindingPoint<?> bindingPoint) {
    return null; // TODO locate the binding point during processing through the context
  }

  private Expression rawBindingExpression(ChildBindingPoint<?> bindingPoint) {
    Expression bindingPointExpression = rawBindingPointExpression(bindingPoint);
    return null; // TODO locate the binding during processing through the context
  }

  private <T> void visitBindingPointExpression(ChildBindingPoint<T> bindingPoint) {
    visitor()
        .visitCast(
            new TypeToken<BindingPoint<T>>() {}
                .withTypeArguments(new TypeArgument<T>(bindingPoint.dataType()) {}),
            rawBindingPointExpression(bindingPoint));
  }

  private <T> void visitBindingExpression(ChildBindingPoint<T> bindingPoint) {
    visitor()
        .visitCast(
            new TypeToken<Binding<T>>() {}
                .withTypeArguments(new TypeArgument<T>(bindingPoint.dataType()) {}),
            rawBindingExpression(bindingPoint));
  }

  private void visitBoundExpression(ChildBindingPoint<?> bindingPoint) {
    visitor()
        .visitCast(bindingPoint.dataType(), rawBindingExpression(bindingPoint).invoke("getData"));
  }

  @Override
  public void visitNamed(String name) {
    if (name.startsWith(BINDING_POINT_PREFIX)) {
      name = name.substring(BINDING_POINT_PREFIX.length());
      visitBindingPointExpression(findBindingPoint(name));

    } else if (name.startsWith(BINDING_PREFIX)) {
      name = name.substring(BINDING_PREFIX.length());
      visitBindingExpression(findBindingPoint(name));

    } else if (name.startsWith(BOUND_PREFIX)) {
      name = name.substring(BOUND_PREFIX.length());
      visitBoundExpression(findBindingPoint(name));

    } else {
      visitor().visitNamed(name);
    }
  }

  @Override
  public void visitNamedAssignment(String name, Expression value) {
    if (name.startsWith(BINDING_POINT_PREFIX)) {
      // TODO throw, can't assign to this

    } else if (name.startsWith(BINDING_PREFIX)) {
      // TODO throw, can't assign to this

    } else if (name.startsWith(BOUND_PREFIX)) {
      // TODO throw, can't assign to this
    }

    visitor().visitNamedAssignment(name, value);
  }
}

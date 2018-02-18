package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.PARENT_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.RESULT_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.SOURCE_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.TARGET_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.qualifiedName;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;
import static uk.co.strangeskies.text.parsing.Parser.matchingAll;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.ExpressionVisitor;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.modabi.schema.impl.ChildBindingPointBuilderImpl;
import uk.co.strangeskies.modabi.schema.impl.ChildBindingPointImpl;
import uk.co.strangeskies.modabi.schema.impl.NodeBuilderImpl;
import uk.co.strangeskies.text.parsing.Parser;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ExpressionVisitor expressionVisitor;
  private final Parser<QualifiedName> qualifiedNameParser;

  private final ChildBindingPointImpl<?> bindingPoint;
  private final NodeBuilderImpl<?> bindingNode;

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      Parser<QualifiedName> qualifiedNameParser,
      NodeBuilderImpl<?> bindingNode) {
    this.expressionVisitor = expressionVisitor;
    this.bindingPoint = null;
    this.bindingNode = bindingNode;
    this.qualifiedNameParser = qualifiedNameParser;
  }

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      Parser<QualifiedName> qualifiedNameParser,
      ChildBindingPointImpl<?> bindingPoint,
      ChildBindingPointBuilderImpl<?> bindingPointBuilder) {
    this.expressionVisitor = expressionVisitor;
    this.bindingPoint = bindingPoint;
    this.bindingNode = bindingPointBuilder.getParent();
    this.qualifiedNameParser = qualifiedNameParser;
  }

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      NodeBuilderImpl<?> bindingNode) {
    this(expressionVisitor, matchingAll(QualifiedName::parseString), bindingNode);
  }

  public BindingFunctionPreprocessor(
      ExpressionVisitor expressionVisitor,
      ChildBindingPointImpl<?> bindingPoint,
      ChildBindingPointBuilderImpl<?> bindingPointBuilder) {
    this(
        expressionVisitor,
        matchingAll(QualifiedName::parseString),
        bindingPoint,
        bindingPointBuilder);
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
      return bindingPoint;

    } else {
      QualifiedName qualifiedName = qualifiedNameParser.parse(name);
      return bindingNode
          .getChildBindingPointsImpl()
          .stream()
          .filter(p -> qualifiedName.equals(p.name()))
          .findAny()
          .orElseThrow(() -> new ModabiSchemaException(MESSAGES.noChildFound(qualifiedName)));
    }
  }

  private Expression rawBindingExpression(QualifiedName bindingPoint) {
    return v -> v
        .visitInvocation(
            c -> c.visitNamed("context"),
            "getBoundObject",
            asList(qualifiedName(bindingPoint)));
  }

  private void visitBoundExpression(ChildBindingPoint<?> bindingPoint) {
    Preprocessor.super.visitCast(
        bindingPoint.dataType(),
        rawBindingExpression(bindingPoint.name()).invoke("getObject"));
  }

  @Override
  public void visitNamed(String name) {
    if (name.equals(TARGET_VALUE)) {
      visitor().visitCast(bindingPoint.getTargetType(), v -> v.visitNamed(TARGET_VALUE));

    } else if (name.equals(RESULT_VALUE)) {
      visitBoundExpression(bindingPoint);

    } else if (name.equals(SOURCE_VALUE)) {
      visitor().visitCast(bindingNode.getSourceType(), v -> v.visitNamed(TARGET_VALUE));

    } else if (name.equals(PARENT_VALUE)) {
      throw new UnsupportedOperationException("PARENT not supported yet");

    } else if (name.startsWith(BOUND_PREFIX)) {
      name = name.substring(BOUND_PREFIX.length());
      visitBoundExpression(findBindingPoint(name));

    } else {
      Preprocessor.super.visitNamed(name);
    }
  }

  @Override
  public void visitNamedAssignment(String name, Expression value) {
    if (name.equals(TARGET_VALUE)) {
      visitor().visitNamedAssignment(name, process(value.check(bindingNode.getTargetType())));

    } else if (name.equals(RESULT_VALUE)) {
      visitBoundExpression(bindingPoint);

    } else if (name.equals(SOURCE_VALUE)) {
      visitor().visitNamedAssignment(name, process(value.check(bindingNode.getSourceType())));

    } else if (name.equals(PARENT_VALUE)) {
      throw new UnsupportedOperationException("PARENT not supported yet");

    } else if (name.startsWith(BOUND_PREFIX)) {
      throw new ModabiSchemaException(MESSAGES.cannotAssignToBoundObject());

    } else {
      Preprocessor.super.visitNamedAssignment(name, v -> value.evaluate(this));
    }
  }
}

package uk.co.strangeskies.modabi.binding.impl;

import static uk.co.strangeskies.modabi.expression.Expressions.cast;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BINDING_POINT_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BINDING_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;

import java.util.List;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingPreprocessor implements Preprocessor {
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
      QualifiedName qualifiedName = parseQualifiedName(name);
      return null;
    }
  }

  public QualifiedName parseQualifiedName(String name) {
    /*
     * TODO we need to revisit that whole can of worms about the format of qualified
     * names being different depending on the current data format. I guess we can
     * have a qualified name converter as a provided object. Actually I think this
     * may actually be a much simpler issue now that the underlying structured data
     * APIonly deals with strings...
     */
    return QualifiedName.parseString(name);
  }

  @Override
  public Expression process(String name) {
    if (name.startsWith(BINDING_POINT_PREFIX)) {
      name = name.substring(BINDING_POINT_PREFIX.length());
      return bindingPointExpression(findBindingPoint(name));

    } else if (name.startsWith(BINDING_PREFIX)) {
      name = name.substring(BINDING_PREFIX.length());
      return bindingExpression(findBindingPoint(name));

    } else if (name.startsWith(BOUND_PREFIX)) {
      name = name.substring(BOUND_PREFIX.length());
      return boundExpression(findBindingPoint(name));
    }

    return null;
  }

  private Expression rawBindingPointExpression(ChildBindingPoint<?> bindingPoint) {
    return null; // TODO locate the binding point during processing through the context
  }

  private Expression rawBindingExpression(ChildBindingPoint<?> bindingPoint) {
    Expression bindingPointExpression = rawBindingPointExpression(bindingPoint);
    return null; // TODO locate the binding during processing through the context
  }

  private <T> Expression bindingPointExpression(ChildBindingPoint<T> bindingPoint) {
    return cast(
        new TypeToken<BindingPoint<T>>() {}
            .withTypeArguments(new TypeArgument<T>(bindingPoint.dataType()) {}),
        rawBindingPointExpression(bindingPoint));
  }

  private <T> Expression bindingExpression(ChildBindingPoint<T> bindingPoint) {
    return cast(
        new TypeToken<Binding<T>>() {}
            .withTypeArguments(new TypeArgument<T>(bindingPoint.dataType()) {}),
        rawBindingExpression(bindingPoint));
  }

  private Expression boundExpression(ChildBindingPoint<?> bindingPoint) {
    return cast(bindingPoint.dataType(), rawBindingExpression(bindingPoint).invoke("getData"));
  }

  @Override
  public Expression process(String name, List<Expression> arguments) {
    return null;
  }
}

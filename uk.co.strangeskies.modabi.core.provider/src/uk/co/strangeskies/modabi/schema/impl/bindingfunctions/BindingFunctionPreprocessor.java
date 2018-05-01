package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static java.util.Arrays.asList;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.modabi.expression.Expressions.named;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.OBJECT_VALUE;
import static uk.co.strangeskies.modabi.schema.BindingExpressions.PROVIDE_METHOD;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;
import static uk.co.strangeskies.reflection.AnnotatedWildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.Annotations.from;
import static uk.co.strangeskies.reflection.token.TypeToken.forAnnotatedType;

import java.util.List;

import uk.co.strangeskies.modabi.expression.Instructions;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.expression.Scope;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ChildLookup childLookup;
  private final TypeToken<?> objectType;
  private final TypeToken<?> objectAssignedType;

  public BindingFunctionPreprocessor(ChildLookup context, TypeToken<?> objectType) {
    this.childLookup = context;
    this.objectType = objectType;
    this.objectAssignedType = forAnnotatedType(wildcard(from(Infer.class)));
  }

  @Override
  public Scope decorateScope(Scope scope) {
    return new Scope() {
      private Instructions rawBoundExpression(Child<?> bindingPoint) {
        return named("context")
            .invoke("getBoundObject", asList(literal(bindingPoint.index())))
            .invoke("getObject")
            .compile(scope);
      }

      private Instructions boundExpression(Child<?> bindingPoint) {
        return new Instructions(
            bindingPoint.type(),
            v -> v.cast(bindingPoint.type(), rawBoundExpression(bindingPoint)));
      }

      @Override
      public Instructions lookupVariable(String variableName) {
        if (variableName.equals(OBJECT_VALUE)) {
          return new Instructions(objectType, v -> v.getNamed(OBJECT_VALUE));

        } else if (variableName.startsWith(BOUND_PREFIX)) {
          String boundName = variableName.substring(BOUND_PREFIX.length());
          return boundExpression(
              childLookup
                  .getChild(boundName)
                  .orElseThrow(
                      () -> new ModabiSchemaException(
                          MESSAGES.cannotResolveVariable(variableName))));

        } else {
          return Scope.super.lookupVariable(variableName);
        }
      }

      @Override
      public Instructions lookupVariableAssignment(String variableName, Instructions value) {
        if (variableName.equals(OBJECT_VALUE)) {
          boolean isIterated = false; // TODO just visit "value"
          if (isIterated) {
            // TODO
          }
          return new Instructions(value.getResultType(), v -> v.putNamed(OBJECT_VALUE, value));

        } else if (variableName.startsWith(BOUND_PREFIX)) {
          throw new ModabiSchemaException(MESSAGES.cannotAssignToBoundObject());

        } else {
          return Scope.super.lookupVariableAssignment(variableName, value);
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public Instructions lookupInvocation(String invocationName, List<Instructions> arguments) {
        if (invocationName.equals(PROVIDE_METHOD) && arguments.size() == 1) {
          Instructions argument = arguments.get(0);

          TypeToken<?> provideTypeToken = argument.getResultType();
          if (!provideTypeToken.getErasedType().equals(TypeToken.class)) {
            return Scope.super.lookupInvocation(invocationName, arguments);
          }

          return new Instructions(
              getProvideType((TypeToken<TypeToken<?>>) provideTypeToken),
              v -> v.invokeNamed(invocationName, arguments));

        } else {
          return Scope.super.lookupInvocation(invocationName, arguments);
        }
      }

      @SuppressWarnings("unchecked")
      private <T> TypeToken<T> getProvideType(TypeToken<? extends TypeToken<T>> provideTypeToken) {
        TypeToken<?> innerArgument = provideTypeToken
            .getTypeArguments()
            .findFirst()
            .get()
            .getTypeToken();
        System.out.println(innerArgument);
        return (TypeToken<T>) innerArgument;
      }
    };
  }
}

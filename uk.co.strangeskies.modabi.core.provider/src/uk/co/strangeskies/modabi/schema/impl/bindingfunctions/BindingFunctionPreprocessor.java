package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static uk.co.strangeskies.modabi.expression.Expressions.literal;
import static uk.co.strangeskies.modabi.expression.Expressions.named;
import static uk.co.strangeskies.modabi.schema.ModabiSchemaException.MESSAGES;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.BOUND_PREFIX;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.OBJECT_VALUE;
import static uk.co.strangeskies.modabi.schema.meta.BindingExpressions.PROVIDE_METHOD;

import java.util.List;

import uk.co.strangeskies.modabi.expression.Instructions;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.modabi.expression.Scope;
import uk.co.strangeskies.modabi.schema.Child;
import uk.co.strangeskies.modabi.schema.ModabiSchemaException;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingFunctionPreprocessor implements Preprocessor {
  private final ChildLookup childLookup;
  private final TypeToken<?> objectType;
  private TypeToken<?> assignedObjectType;

  public BindingFunctionPreprocessor(ChildLookup context, TypeToken<?> objectType) {
    this.childLookup = requireNonNull(context);
    this.objectType = requireNonNull(objectType);
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
          if (assignedObjectType != null) {
            throw new ModabiSchemaException(MESSAGES.cannotRepeatAssignmentToBindingObject());
          }
          assignedObjectType = value.getResultType();
          if (isIterated(value)) {
            // TODO ensure assigned type is assignable to object type
          }
          return new Instructions(value.getResultType(), v -> v.putNamed(OBJECT_VALUE, value));

        } else if (variableName.startsWith(BOUND_PREFIX)) {
          throw new ModabiSchemaException(MESSAGES.cannotAssignToBoundObject());

        } else {
          return Scope.super.lookupVariableAssignment(variableName, value);
        }
      }

      private boolean isIterated(Instructions value) {
        // TODO Auto-generated method stub
        return false;
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

  public TypeToken<?> getAssignedObjectType() {
    if (assignedObjectType == null) {
      return objectType;
    } else {
      return assignedObjectType;
    }
  }
}

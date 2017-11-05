package uk.co.strangeskies.modabi.expression;

import java.util.List;

import uk.co.strangeskies.reflection.token.TypedObject;

public interface Capture {
  TypedObject<?> findValue(String name);

  void setValue(String name, TypedObject<?> value);

  TypedObject<?> findFunction(String name, List<TypedObject<?>> arguments);

  interface CapturedValue {
    
  }
  
  interface CapturedFunction {
    TypedObject<?> invoke(List<TypedObject<?>> arguments);
  }
}

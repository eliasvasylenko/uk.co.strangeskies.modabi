package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import uk.co.strangeskies.modabi.schema.impl.ChildImpl;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingFunctionContext {
  ChildImpl<?> getChild(String name);

  TypeToken<?> typeBefore();

  TypeToken<?> typeAfter();
}

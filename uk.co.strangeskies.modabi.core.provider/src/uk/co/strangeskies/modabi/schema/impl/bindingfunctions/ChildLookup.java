package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import java.util.Optional;

import uk.co.strangeskies.modabi.schema.Child;

public interface ChildLookup {
  Optional<? extends Child<?>> getChild(String name);
}

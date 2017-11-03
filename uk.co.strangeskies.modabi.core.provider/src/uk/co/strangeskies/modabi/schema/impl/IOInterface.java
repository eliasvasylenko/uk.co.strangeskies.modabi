package uk.co.strangeskies.modabi.schema.impl;

import uk.co.strangeskies.modabi.binding.BindingContext;

public class IOInterface {
  private final BindingContext context;

  public IOInterface(BindingContext context) {
    this.context = context;
  }

  public BindingContext getContext() {
    return context;
  }
}

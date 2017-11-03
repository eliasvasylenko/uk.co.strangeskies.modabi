package uk.co.strangeskies.modabi.binding.impl;

import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypedObject;

public class NodeWriter<T> {
  @SuppressWarnings("unchecked")
  public Binding<? extends T> bind(
      BindingContextImpl context,
      BindingPoint<T> bindingPoint,
      T data) {
    context = context.withBindingObject(typedObject(bindingPoint.dataType(), data));

    System.out.println("Write object binding: " + context.getBindingObject().getObject());
    System.out.println("  - model: " + bindingPoint.model().name());
    System.out.println("  - type: " + bindingPoint.dataType());

    Model<? super T> model = getExactModel(bindingPoint.model());

    TypedObject<? extends T> result = (TypedObject<? extends T>) context.getBindingObject();

    return new Binding<>(bindingPoint, model, result);
  }

  public Binding<? extends T> bind(
      BindingContextImpl context,
      ChildBindingPoint<T> bindingPoint) {
    System.out.println("Write child binding: " + bindingPoint.name());
    System.out.println("  - model: " + bindingPoint.model().name());
    System.out.println("  - type: " + bindingPoint.dataType());

    return null;
  }

  private Model<? super T> getExactModel(Model<? super T> model) {
    return model;
  }
}

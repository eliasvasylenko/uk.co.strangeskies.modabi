package uk.co.strangeskies.modabi.binding.impl;

import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.reflection.token.TypedObject.typedObject;

import uk.co.strangeskies.modabi.Binding;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.reflection.token.TypedObject;

public class NodeWriter {
  @SuppressWarnings("unchecked")
  public <T> Binding<? extends T> bind(
      BindingContextImpl context,
      BindingPoint<T> bindingPoint,
      T data) {
    context = context.withBindingObject(typedObject(bindingPoint.dataType(), data));

    System.out.println("Write object binding: " + context.getBindingObject().getObject());
    System.out.println("  - model: " + bindingPoint.model().name());
    System.out.println("  - type: " + bindingPoint.dataType());

    Model<? super T> model = getExactModel(bindingPoint.model(), data);
    TypedObject<? extends T> result = (TypedObject<? extends T>) context.getBindingObject();

    StructuredDataWriter output = context.output().get();
    output.addChild(model.name());

    context = model.rootNode().children().reduce(context, (c, child) -> {
      new NodeWriter().bind(c, child);
      return c;
    }, throwingReduce());

    output.endChild();

    return new Binding<>(bindingPoint, model, result);
  }

  public <T> Binding<? extends T> bind(
      BindingContextImpl context,
      ChildBindingPoint<T> bindingPoint) {
    System.out.println("Write child binding: " + bindingPoint.name());
    System.out.println("  - model: " + bindingPoint.model().name());
    System.out.println("  - type: " + bindingPoint.dataType());

    return null;
  }

  private <T> Model<? super T> getExactModel(Model<? super T> model, T data) {
    return model;
  }
}

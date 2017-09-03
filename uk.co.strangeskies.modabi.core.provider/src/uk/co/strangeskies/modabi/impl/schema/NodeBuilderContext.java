package uk.co.strangeskies.modabi.impl.schema;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.schema.Node;

public interface NodeBuilderContext<T, E> {
  Optional<Namespace> namespace();

  Stream<Node<? super T>> overrideNode();

  E endNode(NodeImpl<T> node);
}

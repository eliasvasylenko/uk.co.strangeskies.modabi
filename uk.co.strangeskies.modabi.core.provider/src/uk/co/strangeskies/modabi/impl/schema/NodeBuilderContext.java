package uk.co.strangeskies.modabi.impl.schema;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.schema.Node;

public interface NodeBuilderContext<E> {
  Optional<Namespace> namespace();

  Stream<Node> overrideNode();

  E endNode(NodeImpl node);
}

package uk.co.strangeskies.modabi.schema.impl;

import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.Node;

public interface NodeBuilderContext<E> {
  Optional<Namespace> namespace();

  Stream<Node> overrideNode();

  E endNode(NodeBuilderImpl<?> nodeBuilder);

  FunctionalExpressionCompiler expressionCompiler();
}

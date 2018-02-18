package uk.co.strangeskies.modabi.schema.impl;

import java.util.Optional;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.Imports;

public interface NodeBuilderContext<E> {
  Imports imports();

  Optional<Namespace> namespace();

  Optional<Node> overrideNode();

  E endNode(NodeBuilderImpl<?> nodeBuilder);

  FunctionalExpressionCompiler expressionCompiler();
}

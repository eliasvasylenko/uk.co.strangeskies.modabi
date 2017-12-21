package uk.co.strangeskies.modabi.schema.impl;

import static uk.co.strangeskies.collection.stream.StreamUtilities.upcastStream;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.text.parsing.Parser;

public class NodeImpl implements Node {
  private final boolean extensible;
  private final boolean concrete;
  private List<ChildBindingPointImpl<?>> children;

  protected NodeImpl() {
    extensible = false;
    concrete = true;
  }

  protected NodeImpl(NodeBuilderImpl<?> configurator) {
    extensible = configurator
        .overrideChildren(Node::extensible, NodeBuilder::getExtensible)
        .validateOverride((a, b) -> true)
        .orDefault(false)
        .get();
    concrete = configurator
        .overrideChildren(Node::concrete, NodeBuilder::getConcrete)
        .validateOverride((a, b) -> true)
        .orDefault(true)
        .get();

    children = configurator.getChildBindingPointsImpl();

    /*
     * TODO load "provided" values
     * 
     * TODO The previous approach taken here was all wrong. The process of binding
     * should essentially allow us to determine the type as a free side-effect! The
     * exact type is inferred as we go along in such cases. Just propagate this
     * information out through the API to here.
     * 
     * TODO One of the difficulties last time was the bootstrapping issue. We need
     * to bind against the NodeImpl being instantiated to get the provided value,
     * but to complete instantiation of the NodeImpl we need to have the provided
     * value. Previously this was particularly tricky because we needed to know the
     * type of the node before hand, but also the type of the bound object could
     * inform the ultimate type of the node if it is more specific. We don't have
     * this problem now, as the type is specified in the containing child binding
     * point. Rejoice!
     * 
     * TODO 2 Generalize providing these values to allow buffering structured data,
     * too. i.e. StructuredDataSource instead of DataSource
     */
    // configurator.dataLoader().loadData(this, valuesBuffer);
  }

  @Override
  public boolean concrete() {
    return concrete;
  }

  @Override
  public boolean extensible() {
    return extensible;
  }

  @Override
  public Parser<?> parser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Node> baseNodes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<ChildBindingPoint<?>> children() {
    return upcastStream(children.stream());
  }

  @Override
  public List<ChildBindingPoint<?>> descendents(List<QualifiedName> names) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChildBindingPoint<?> child(QualifiedName name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Optional<?> providedValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Expression initializeInputExpression() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Expression initializeOutputExpression() {
    // TODO Auto-generated method stub
    return null;
  }

}

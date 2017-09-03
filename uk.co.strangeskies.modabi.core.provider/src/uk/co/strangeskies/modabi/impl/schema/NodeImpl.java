package uk.co.strangeskies.modabi.impl.schema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.text.parsing.Parser;

public class NodeImpl<T> implements Node<T> {
  private final boolean extensible;
  private final boolean concrete;

  protected NodeImpl() {
    extensible = false;
    concrete = true;
  }

  protected NodeImpl(NodeBuilderImpl<T, ?> configurator) {
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

    /*
     * TODO load "provided" values
     * 
     * TODO The previous approach taken here was all wrong. The process of binding
     * should essentially allow us to determine the type as a free side-effect! The
     * exact type is inferred as we go along in such cases. Just propagate this
     * information out through the API to here.
     * 
     * We still need a special (perhaps internal?) DataLoader api here rather than
     * just passing through the source SchemaManager directly, as BaseSchema and
     * MetaSchema are built before the SchemaManager. They need their results
     * spoofing since they are built before the normal binding process and models
     * can be available.
     * 
     * TODO 2 generalize providing these values to allow buffering structured data,
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
  public NodeBuilder<T, ?> configurator() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Parser<T> parser() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<Node<?>> baseNodes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stream<ChildBindingPoint<?>> children() {
    // TODO Auto-generated method stub
    return null;
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
  public Optional<T> providedValue() {
    // TODO Auto-generated method stub
    return null;
  }

}

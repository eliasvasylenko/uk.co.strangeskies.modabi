package uk.co.strangeskies.modabi.schema.impl.bindingfunctions;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.functional.FunctionCapture;
import uk.co.strangeskies.modabi.expression.functional.FunctionalExpressionCompiler;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingFunction;
import uk.co.strangeskies.reflection.token.TypeToken;

public class BindingFunctionImpl implements BindingFunction {
  public interface BindingFunctionInterface {
    void bind();
  }

  public static class BindingFunctionCapture {
    public Object object;

    public BindingContext context;

    public <T> T provide(TypeToken<T> type) {
      return context.provide(type).getObject();
    }
  }

  private final Expression expression;
  private final FunctionCapture<BindingFunctionCapture, BindingFunctionInterface> bindingFunction;

  /*
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * TODO we may have a complex expression whose exact type we don't know until
   * later. At compilation time we just want to be able to perform override
   * resolution and confirm resolvability, but the exact type of some nodes in the
   * expression (e.g. bound items) may change at evaluation time such that we can
   * infer a more specific type.
   * 
   * To facilitate this the types of these nodes should be INFERENCE TYPES, with
   * upper bounds on the types of the binding points they represent.
   * 
   * This way the compilation can produce a BoundSet, and further bounds can be
   * incorporated at evaluation time based on the exact types of the bound items.
   * 
   * Things to consider:
   * 
   * - we may need to maintain uninferred bound sets through the evaluation of
   * multiple child binding points (e.g. new ArrayList<?>() then add(T)
   * 
   * - the type of a node is always fully inferred before it is referenced by the
   * input and output expressions of its child binding point or elsewhere.
   * 
   * - we don't need to perform the inference type trick for all nodes, only ones
   * which can actually influence the inference of external types. In other words,
   * if a set of such inference types have no bounds relating to external
   * inference types they can be inferred at compilation time rather than deferred
   * to evaluation time. We may be able to avoid using inference types for these
   * items in the first place by analyzing the structure of the expression tree.
   * 
   * 
   * 
   * 
   * 
   * 
   * Can we limit the times we need to defer exact type inference until the actual
   * binding is performed? This greatly simplifies things! Is it too limiting?
   * 
   * - we CAN HAVE IT BOTH WAYS!!!!!! we determine the type for these things by
   * deferring inference to binding time ... but then doing the binding at compile
   * time to deal with the "provided values" thing, then we use the inferred type
   * to inform the type of the node.
   * 
   * - we need a way to infer type based on type tokens supplied in the
   * expression. The most obvious case for this is the model for TypeToken, which
   * when bound as a "provided value" should give a binding which is properly
   * typed as TypeToken<[actual token type]>.
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */
  public BindingFunctionImpl(
      BindingFunctionContext context,
      Expression expression,
      FunctionalExpressionCompiler compiler) {
    this.expression = expression;
    Expression processedExpression = visitor -> expression
        .evaluate(new BindingFunctionPreprocessor(visitor, context));
    this.bindingFunction = compiler
        .compile(
            processedExpression,
            forClass(BindingFunctionInterface.class),
            forClass(BindingFunctionCapture.class));
  }

  @Override
  public Expression getExpression() {
    return expression;
  }

  @Override
  public void apply(BindingContext context) {
    BindingFunctionCapture capture = new BindingFunctionCapture();
    capture.object = context.getBindingObject();
    capture.context = context;
    bindingFunction.capture(capture).getInstance().bind();
  }

  @Override
  public TypeToken<?> getTypeBefore() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeToken<?> getTypeAfter() {
    // TODO Auto-generated method stub
    return null;
  }
}

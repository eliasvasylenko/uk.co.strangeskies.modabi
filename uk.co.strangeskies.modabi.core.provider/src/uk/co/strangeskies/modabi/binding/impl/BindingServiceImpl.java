package uk.co.strangeskies.modabi.binding.impl;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.modabi.binding.BindingService;
import uk.co.strangeskies.modabi.binding.InputBinder;
import uk.co.strangeskies.modabi.binding.OutputBinder;
import uk.co.strangeskies.modabi.functional.FunctionCompiler;
import uk.co.strangeskies.modabi.functional.FunctionCompilerImpl;
import uk.co.strangeskies.modabi.io.DataFormat;
import uk.co.strangeskies.modabi.io.DataFormats;
import uk.co.strangeskies.modabi.schema.Schemata;
import uk.co.strangeskies.modabi.schema.impl.BindingContextImpl;
import uk.co.strangeskies.modabi.schema.impl.CoreSchemata;
import uk.co.strangeskies.modabi.schema.impl.SchemaBuilderImpl;

/*
 * TODO Can we get rid of this? Most of this central management isn't necessary.
 * 
 * We can provide SchemaBuilder/InputBinder/OutputBinder as bundle
 * scoped, wiring up and providing only the things that bundle needs:
 * 
 * - providers/schemata/data-types can be wired in and out through services
 * 
 * - the build tool plugin already adds all the necessary package imports and
 *   lists required/provided services
 * 
 * - the build tool can be extended to wire services through DS by generating XML,
 *   this means a schema can have requirements on all the schemata / providers /
 *   data-types they depend on with service filters on names (possibly versions).
 *   this solves ordering!!!
 *   
 * - TODO can we do away with all the tedious futures crap?
 * 
 * - The above only solves ordering for building schema ... unless we somehow make
 *   the bundle-scoped Schemata service instance wait for all the needed stuff to
 *   resolve... can we do this with individual DS schemata service instances filtered
 *   with a servicepermission to only be internal to the bundle? This doesn't even
 *   need to be DS on second thoughts... but it would probably make it easier.
 *   
 * - TODO uses directive can be used on arbitrary provided capabilities!!!!! Put a
 *   uses directive on the cap (probs either a Schema service or the current extender
 *   pattern stuff)  
 * 
 * A bundle can add schemata to the Schemata service wired to it, but they will
 * not be exposed outside. Is this okay? Are there use-cases for wanting to do
 * that? No I don't think so, if a user wants to manually load or programmatically
 * generate a schema they can share it through other means.
 */
/**
 * A schema manager implementation for an OSGi context.
 * <p>
 * Any bundle can add schemata / models / data formats into the manager, but
 * they will only be visible to from the adding bundle ... With the exception of
 * schemata, which will be made available to any other bundle which explicitly
 * makes requirements on the appropriate capability.
 * <p>
 * Data formats and providers should typically be contributed via the service
 * whiteboard model. They will be available to all bundles which have visibility
 * to those services. There's not really much point in limiting visibility
 * further than this... Providers might cause conflicts with one another in odd
 * cases, but it should be reasonable to consider that the problem of the person
 * designing a deployment. All conflicts are resolved by service ranking!
 * 
 * @author Elias N Vasylenko
 */
@Component(immediate = true)
public class BindingServiceImpl implements BindingService {
  class DataFormatsImpl extends DataFormats {
    @Override
    public void add(DataFormat element) {
      super.add(element);
    }

    @Override
    public void remove(DataFormat element) {
      super.remove(element);
    }
  }

  /*
   * Data formats available for binding and unbinding
   */
  private final DataFormatsImpl dataFormats;
  private final Schemata schemata;

  public BindingServiceImpl() {
    dataFormats = new DataFormatsImpl();

    FunctionCompiler expressionCompiler = new FunctionCompilerImpl();
    SchemaBuilderImpl schemaBuilder = new SchemaBuilderImpl(expressionCompiler);
    CoreSchemata core = new CoreSchemata(() -> schemaBuilder);
    schemata = new Schemata(core.baseSchema());
  }

  private BindingContextImpl getProcessingContext() {
    return new BindingContextImpl(schemata);
  }

  @Override
  public InputBinder<?> bindInput() {
    return InputBinderImpl.bind(getProcessingContext(), dataFormats);
  }

  @Override
  public <T> OutputBinder<? super T> bindOutput(T data) {
    return OutputBinderImpl.bind(getProcessingContext(), dataFormats, data);
  }
}

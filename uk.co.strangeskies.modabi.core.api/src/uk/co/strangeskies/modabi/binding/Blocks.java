package uk.co.strangeskies.modabi.binding;

import java.util.List;

import uk.co.strangeskies.modabi.binding.provisions.ReferenceReader;
import uk.co.strangeskies.modabi.schema.Model;

/**
 * A summary view of the blocking dependencies of a binding process.
 * <p>
 * Blocks can either be internal or external. Internal blocks are expected to be
 * satisfied by completion of some concurrent part of the binding process.
 * External blocks are expected to be satisfied by external resources.
 * <p>
 * We can detect deadlocks by observing that every currently executing
 * {@link IOBuilder IO expression} is currently blocking on an input resource,
 * and we can determine which expressions are blocking on input resources by
 * intercepting invocations of
 * {@link ReferenceReader#dereference(Model, List, String)} for the instances
 * provided by each individual {@link BindingContext} in the tree.
 * 
 * @author Elias N Vasylenko
 */
public interface Blocks {

}

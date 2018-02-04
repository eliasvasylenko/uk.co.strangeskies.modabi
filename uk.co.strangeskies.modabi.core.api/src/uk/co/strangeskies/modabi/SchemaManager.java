/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi;

import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.ReifiedToken;

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
public interface SchemaManager {
  Stream<Provider> getProviders();

  Schemata schemata();

  DataFormats dataFormats();

  InputBinder<?> bindInput();

  <T> OutputBinder<? super T> bindOutput(T data);

  default <T extends ReifiedToken<T>> OutputBinder<? super T> bindOutput(T data) {
    return bindOutput(data).from(data.getThisTypeToken());
  }
}

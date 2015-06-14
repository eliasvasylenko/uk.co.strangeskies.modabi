/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.modabi.declarative;

/**
 * 
 * modabi-core (main interfaces / classes)
 * 
 * modabi-impl (reference implementation published as OSGi services, classes to
 * scan declarative schema info in META-INF)
 * 
 * modabi-osgi (republish schema/providers discovered in jar metadata as OSGi
 * services)
 * 
 * This annotation marks a class or interface as being primarily related to a
 * particular schema model.
 * 
 * Maven/Gradle plugin actions:
 * 
 * 1) Translate annotated 'model' and 'type' classes and interface into schema.
 * Obviously throw errors if these translations are impossible.
 * 
 * 2) Identify all XML / YAML / whatever resources which are applicable
 * (including any newly generated schema), and try to bind them all, to make
 * sure they're valid. It may be difficult to deal with dependencies properly
 * here... Maybe just do schema, or generate warnings rather than errors if we
 * can't locate dependencies.
 * 
 * How do we deal with identifying applicable resources? Just by searching
 * exhaustively for known file types, then matching fully qualified root name to
 * known schema models? Seems reasonable.
 * 
 * 3) Generate meta data for jar pointing to all identified resources (or just
 * schema)? Is this useful outside of an OSGi environment where each bundle can
 * be responsible for publishing these resources as services at runtime?
 * 
 * 
 * Things OSGi can do better than anything I could re-implement:
 * 
 * - Publishing 'providers' to be consumed by schema manager. (service registry)
 * 
 * - Publishing Schema to be consumed by schema manager? (service registry, annotated classes?)
 * 
 * How to have OSGi work alongside custom systems for people not working in OSGi
 * environments?
 * 
 * Perhaps have metadata scanners which look for what metadata is published in
 * META-INF for all jars on the classpath.
 * 
 * @author eli
 *
 */
public @interface Model {

}

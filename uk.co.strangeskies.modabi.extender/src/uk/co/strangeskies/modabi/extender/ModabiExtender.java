/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.extender.
 *
 * uk.co.strangeskies.modabi.extender is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.extender is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.extender.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.extender;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import aQute.bnd.annotation.headers.ProvideCapability;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.osgi.ExtenderManager;
import uk.co.strangeskies.utilities.classpath.DelegatingClassLoader;

@ProvideCapability(ns = ExtenderNamespace.EXTENDER_NAMESPACE, name = ModabiExtender.MODABI_EXTENDER_NAME, version = "1.0.0")
@Component(immediate = true)
public class ModabiExtender extends ExtenderManager {
	public static final String MODABI_EXTENDER_NAME = "uk.co.strangeskies.modabi";

	@Reference
	private SchemaManager manager;

	@Override
	protected boolean register(Bundle bundle) {
		log(Level.INFO, "Registering bundle '" + bundle.getSymbolicName() + "' in Modabi extender");

		for (BundleCapability capability : bundle.adapt(BundleWiring.class).getCapabilities(MODABI_EXTENDER_NAME)) {
			QualifiedName schemaName = QualifiedName.parseString((String) capability.getAttributes().get("schema"));

			URL resource = bundle.getResource("/" + capability.getAttributes().get("resource"));

			new Thread(() -> {
				log(Level.INFO, "Registering schema capability '" + schemaName + "' at resource '" + resource + "'");

				try {
					ClassLoader targetClassloader = bundle.adapt(BundleWiring.class).getClassLoader();
					targetClassloader = new DelegatingClassLoader(targetClassloader, Schema.class.getClassLoader());

					Schema schema = manager.bindSchema().withClassLoader(targetClassloader).from(resource).resolve();

					if (!schema.qualifiedName().equals(schemaName)) {
						throw new SchemaException(
								"Schema bound '" + schema.qualifiedName() + "' does not match declared name '" + schemaName + "'");
					}

					log(Level.INFO, "Successfully bound schema '" + schemaName + "' in Modabi extender");
				} catch (Throwable e) {
					log(Level.ERROR, "Failed to bind schema '" + schemaName + "' in Modabi extender", e);
					throw e;
				}
			}).start();
		}

		return true;
	}

	@Override
	protected void unregister(Bundle bundle) {}
}

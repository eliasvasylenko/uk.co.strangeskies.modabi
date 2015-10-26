/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.bnd.
 *
 * uk.co.strangeskies.modabi.bnd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.bnd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.bnd.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.bnd.xml;

import aQute.bnd.annotation.plugin.BndPlugin;
import aQute.bnd.service.AnalyzerPlugin;
import uk.co.strangeskies.modabi.bnd.ModabiRegistration;
import uk.co.strangeskies.modabi.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.io.xml.XmlInterface;

@BndPlugin(name = "modabi-xml")
public class ModabiXmlRegistration extends ModabiRegistration
		implements AnalyzerPlugin {
	public ModabiXmlRegistration() {
		super(new SchemaManagerImpl(), new XmlInterface());
	}
}

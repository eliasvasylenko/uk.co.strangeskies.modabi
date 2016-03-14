/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.scripting.
 *
 * uk.co.strangeskies.modabi.scripting is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.scripting is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.scripting.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.scripting;

import javax.script.ScriptEngineManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import uk.co.strangeskies.modabi.SchemaManager;

@Component
public class ScriptEngineManagerRegistration {
	@Reference
	ScriptEngineManager scriptEngineManager;

	@Reference
	SchemaManager schemaManager;

	@Activate
	public void activate() {
		schemaManager.provisions().registerProvider(ScriptEngineManager.class, () -> scriptEngineManager);
	}
}

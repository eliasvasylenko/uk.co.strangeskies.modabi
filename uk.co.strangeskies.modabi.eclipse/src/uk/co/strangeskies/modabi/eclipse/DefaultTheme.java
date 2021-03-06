/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.eclipse.
 *
 * uk.co.strangeskies.modabi.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.eclipse.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.eclipse;

import org.eclipse.fx.ui.services.theme.Theme;
import org.eclipse.fx.ui.theme.AbstractTheme;
import org.osgi.service.component.annotations.Component;

@Component(service = Theme.class)
public class DefaultTheme extends AbstractTheme {
	public DefaultTheme() {
		super("theme.default", "Default theme", DefaultTheme.class.getClassLoader().getResource("css/default.css"));
	}
}

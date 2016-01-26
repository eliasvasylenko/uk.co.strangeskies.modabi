/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.eclipse.xml.
 *
 * uk.co.strangeskies.modabi.eclipse.xml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.eclipse.xml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.eclipse.xml.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.eclipse.xml.editors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.XMLContentDescriber;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

public class ModabiXmlContentTypeDescriber extends XMLContentDescriber {
	@Override
	public int describe(InputStream input, IContentDescription description)
			throws IOException {
		return describe(new InputStreamReader(input), description);
	}

	@Override
	public int describe(Reader input, IContentDescription description)
			throws IOException {
		int result = super.describe(input, description);
		if (result == VALID) {
			input.reset();

		}
		System.out.println("IS MODABI???? " + result);
		return result;
	}
}

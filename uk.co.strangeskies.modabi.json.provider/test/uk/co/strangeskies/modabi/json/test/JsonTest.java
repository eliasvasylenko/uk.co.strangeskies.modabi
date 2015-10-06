/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.json.provider.
 *
 * uk.co.strangeskies.modabi.json.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.json.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.json.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.json.test;

import java.time.LocalDate;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.Primitive;
import uk.co.strangeskies.modabi.io.structured.StructuredDataTarget;
import uk.co.strangeskies.modabi.json.impl.JsonTarget;

public class JsonTest {
	private void run() {
		StructuredDataTarget output = new JsonTarget(System.out, true);

		output.registerDefaultNamespaceHint(Namespace.getDefault());
		output.nextChild(new QualifiedName("root"));
		output.nextChild(new QualifiedName("poot",
				new Namespace(String.class.getPackage(), LocalDate.now())));
		output.writeProperty(new QualifiedName("groot")).put(Primitive.BOOLEAN, true)
				.terminate();
		output.writeContent().put(Primitive.DOUBLE, 2d).put(Primitive.STRING, "coot")
				.terminate();
		output.endChild();
		output.nextChild(new QualifiedName("joot"));
		output.endChild();
		output.endChild();
	}

	public static void main(String... args) {
		new JsonTest().run();
	}
}

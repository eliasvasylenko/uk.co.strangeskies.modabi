/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.io.xml.
 *
 * uk.co.strangeskies.modabi.io.xml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.io.xml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.io.xml.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.io.xml.test;

import uk.co.strangeskies.modabi.Namespace;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.io.structured.StructuredDataWriter;
import uk.co.strangeskies.modabi.io.xml.XmlTarget;

public class XmlTest {
  private void run() {
    StructuredDataWriter output = new XmlTarget(System.out);

    output
        .registerDefaultNamespaceHint(Namespace.getDefault())
        .addChild(new QualifiedName("root"))
        .addChild(new QualifiedName("poot"))
        .writeProperty(new QualifiedName("groot"), "boot")
        .setPrimaryProperty(new QualifiedName("shoot"))
        .writeProperty(new QualifiedName("shoot"), "coot")
        .endChild()
        .addChild(new QualifiedName("joot"))
        .endChild()
        .endChild();
  }

  public static void main(String... args) {
    new XmlTest().run();
  }
}

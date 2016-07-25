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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Namespace {
	private final String namespace;
	private final LocalDate date;

	private final static Namespace DEFAULT = new Namespace(Namespace.class.getPackage().getName(),
			LocalDate.of(2014, 1, 1));

	public Namespace(String namespace, LocalDate date) {
		this.namespace = namespace;
		this.date = date;

		if (!namespace.matches("[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*")) {
			throw new ModabiException(t -> t.invalidNamespace(namespace));
		}
	}

	public Namespace(Package namespace, LocalDate date) {
		this.namespace = namespace.getName();
		this.date = date;
	}

	public Package getPackage() {
		return Package.getPackage(namespace);
	}

	public String getPackageString() {
		return namespace;
	}

	public LocalDate getDate() {
		return date;
	}

	@Override
	public String toString() {
		return namespace + ":" + date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	public String toHttpString() {
		List<String> packages = Arrays.asList(namespace.split("\\."));
		Collections.reverse(packages);
		return "http://" + packages.stream().collect(Collectors.joining(".")) + "/"
				+ date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "/";
	}

	public static Namespace parseString(String string) {
		int splitIndex = string.lastIndexOf(':');

		String packageString = string.substring(0, splitIndex);

		return new Namespace(packageString,
				LocalDate.parse(string.substring(splitIndex + 1), DateTimeFormatter.ISO_LOCAL_DATE));
	}

	public static Namespace parseHttpString(String httpString) {
		if (httpString.indexOf("http://") != 0)
			throw new ModabiException(t -> t.invalidNamespace(httpString));
		if (httpString.lastIndexOf('/') != httpString.length() - 1)
			throw new ModabiException(t -> t.invalidNamespace(httpString));

		String[] split = httpString.split("/");

		if (split.length != 4)
			throw new ModabiException(t -> t.invalidNamespace(httpString));

		String namespace = split[2];
		String date = split[3];

		List<String> packages = Arrays.asList(namespace.split("\\."));
		Collections.reverse(packages);

		String packageName = packages.stream().collect(Collectors.joining("."));

		return new Namespace(packageName, LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Namespace))
			return false;

		return namespace.equals(((Namespace) obj).namespace) && date.equals(((Namespace) obj).date);
	}

	@Override
	public int hashCode() {
		return namespace.hashCode() ^ date.hashCode();
	}

	public static Namespace getDefault() {
		return DEFAULT;
	}
}

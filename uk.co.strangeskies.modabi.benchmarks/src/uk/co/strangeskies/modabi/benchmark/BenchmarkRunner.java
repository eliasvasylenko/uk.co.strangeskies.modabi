/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.benchmarks.
 *
 * uk.co.strangeskies.modabi.benchmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.benchmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.benchmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.xml.impl.XMLSource;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class BenchmarkRunner {
	private static final String OUTPUT_FOLDER = System.getProperty("user.home")
			+ File.separatorChar + "xml-benchmark";

	public void run(SchemaManager manager) {
		try {
			File outputDir = new File(OUTPUT_FOLDER);
			if (!outputDir.exists()) {
				System.out.println("Creating output folder: "
						+ outputDir.getAbsolutePath());
				boolean created = outputDir.mkdirs();
				if (!created) {
					throw new IllegalStateException("Could not create "
							+ outputDir.getAbsolutePath() + ". Aborting...");
				}
			}

			StructuredDataSource benchmarkSchemaResource = new XMLSource(getClass()
					.getResourceAsStream("/BenchmarkSchema.xml"));
			manager.registerSchemaBinding(benchmarkSchemaResource);

			System.out.println(manager.registeredModels());

			boolean createXml = false;
			System.out.println("Will create XML files? " + createXml);
			if (createXml) {
				createXmlPortfolio(manager);
			}

			System.gc();
			System.gc();

			for (int i = 0; i < 10; i++) {
				readLargeXmlWithModabi(manager, new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-100.xml"));
				readLargeXmlWithModabi(manager, new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-1000.xml"));
				readLargeXmlWithModabi(manager, new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-10000.xml"));

				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100.xml"));
				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000.xml"));
				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"));

				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100.xml"));
				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000.xml"));
				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void createXmlPortfolio(SchemaManager manager)
			throws Exception {
		createXml(manager, 100, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-100.xml");
		System.out.println("Completed generation of large XML with 100 entries...");

		createXml(manager, 1000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-1000.xml");
		System.out
				.println("Completed generation of large XML with 1,000 entries...");

		createXml(manager, 10000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-10000.xml");
		System.out
				.println("Completed generation of large XML with 10,000 entries...");
	}

	private static void createXml(SchemaManager manager, int nbrElements,
			String fileName) throws Exception {
		PersonsType persons = new PersonsType();

		for (int i = 0; i < nbrElements; i++) {
			PersonType person = new PersonType();
			person.setActive(RandomUtils.nextInt(0, 2) == 0);

			person.setPostCode(RandomStringUtils.randomAlphanumeric(5));

			person.setLastName(RandomStringUtils.randomAlphabetic(RandomUtils
					.nextInt(5, 15)));

			person.setFirstName(RandomStringUtils.randomAlphabetic(RandomUtils
					.nextInt(3, 10)));

			person.setCountry(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(
					4, 11)));

			person.setCity(RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(4,
					11)));

			person.setAddress2(RandomStringUtils.randomAlphabetic(RandomUtils
					.nextInt(4, 11)));

			person.setAddress1(RandomStringUtils.randomAlphabetic(RandomUtils
					.nextInt(4, 11)));

			persons.getPerson().add(person);
		}

		File file = new File(fileName);

		OutputStream fos = new FileOutputStream(file);

		try {
			manager.unbind(PersonsType.class, new XMLTarget(fos), persons);
			fos.flush();
		} finally {
			fos.close();
		}
	}

	private static void readLargeXmlWithModabi(SchemaManager manager, File file)
			throws FileNotFoundException, IOException {
		long start = System.currentTimeMillis();

		try (FileInputStream fis = new FileInputStream(file)) {
			manager.bind(new XMLSource(fis));

			long end = System.currentTimeMillis();

			System.out.println("Modabi - (" + file + "): Time taken in ms: "
					+ (end - start));
		}
	}

	private static void readLargeXmlWithStax(File file)
			throws FactoryConfigurationError, XMLStreamException, JAXBException,
			IOException {

		// set up a StAX reader
		XMLInputFactory xmlif = XMLInputFactory.newInstance();

		JAXBContext ucontext = JAXBContext.newInstance(PersonType.class);

		Unmarshaller unmarshaller = ucontext.createUnmarshaller();

		long start = System.currentTimeMillis();

		try (FileReader fr = new FileReader(file)) {
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(fr);

			xmlr.nextTag();
			xmlr.require(XMLStreamConstants.START_ELEMENT, null, "persons");

			xmlr.nextTag();
			while (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
				unmarshaller.unmarshal(xmlr, PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			long end = System.currentTimeMillis();

			System.out.println("STax - (" + file + "): Time taken in ms: "
					+ (end - start));

		}
	}

	private static void readLargeXmlWithFasterStax(File file)
			throws FactoryConfigurationError, XMLStreamException, JAXBException,
			FileNotFoundException, IOException {

		// set up a StAX reader
		XMLInputFactory xmlif = XMLInputFactory.newInstance();

		JAXBContext ucontext = JAXBContext.newInstance(PersonType.class);

		Unmarshaller unmarshaller = ucontext.createUnmarshaller();

		long start = System.currentTimeMillis();

		try (FileReader fr = new FileReader(file)) {
			XMLStreamReader xmlr = xmlif.createXMLStreamReader(fr);

			xmlr.nextTag();
			xmlr.require(XMLStreamConstants.START_ELEMENT, null, "persons");

			xmlr.nextTag();
			while (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
				unmarshaller.unmarshal(xmlr, PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			long end = System.currentTimeMillis();

			System.out.println("Woodstox - (" + file + "): Time taken in ms: "
					+ (end - start));
		}
	}
}

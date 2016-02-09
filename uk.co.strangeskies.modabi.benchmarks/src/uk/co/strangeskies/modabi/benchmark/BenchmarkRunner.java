/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.LogService;

import uk.co.strangeskies.modabi.SchemaManager;
import uk.co.strangeskies.utilities.classpath.ContextClassLoaderRunner;

@Component
public class BenchmarkRunner {
	private static final String OUTPUT_FOLDER = System.getProperty("user.home")
			+ File.separatorChar + "xml-benchmark";

	private SchemaManager manager;

	private LogService logger;

	@Reference
	public void setSchemaManager(SchemaManager manager) {
		this.manager = manager;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	public void setLogger(LogService logger) {
		this.logger = logger;
	}

	public void unsetLogger(LogService logger) {
		this.logger = null;
	}

	private void log(int level, String message) {
		if (logger != null)
			logger.log(level, message);
	}

	@Activate
	public void activate(BundleContext context) {
		new Thread(() -> {
			try {
				run(context);
			} catch (Throwable t) {
				throw t;
			} finally {
				try {
					context.getBundle(0).stop();
				} catch (BundleException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	public void run(BundleContext context) {
		new ContextClassLoaderRunner(getClass().getClassLoader()).run(() -> {
			try {
				log(LogService.LOG_INFO, "Preparing benchmark...");

				File outputDir = new File(OUTPUT_FOLDER);
				if (!outputDir.exists()) {
					log(LogService.LOG_INFO,
							"Creating output directory: " + outputDir.getAbsolutePath());

					if (!outputDir.mkdirs()) {
						throw new IllegalStateException(
								"Could not create output directory, aborting...");
					}
				}

				System.out.println("test");
				manager.bindSchema()
						.from(context.getBundle()
								.getResource("/META-INF/modabi/BenchmarkSchema.xml")
								.openStream())
						.resolve(500);

				System.out.println("test2");

				boolean createXml = false;
				log(LogService.LOG_INFO, "Will create XML files? " + createXml);
				if (createXml) {
					createXmlPortfolio();
				}

				System.gc();
				System.gc();

				for (int i = 0; i < 10; i++) {
					readLargeXmlWithModabi(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-100.xml"));
					readLargeXmlWithModabi(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-1000.xml"));
					readLargeXmlWithModabi(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-10000.xml"));

					readLargeXmlWithStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-100.xml"));
					readLargeXmlWithStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-1000.xml"));
					readLargeXmlWithStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-10000.xml"));

					readLargeXmlWithFasterStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-100.xml"));
					readLargeXmlWithFasterStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-1000.xml"));
					readLargeXmlWithFasterStax(new File(
							OUTPUT_FOLDER + File.separatorChar + "large-person-10000.xml"));
				}
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void createXmlPortfolio() throws Exception {
		createXml(100, OUTPUT_FOLDER + File.separatorChar + "large-person-100.xml");
		log(LogService.LOG_INFO,
				"Completed generation of large XML with 100 entries...");

		createXml(1000,
				OUTPUT_FOLDER + File.separatorChar + "large-person-1000.xml");
		log(LogService.LOG_INFO,
				"Completed generation of large XML with 1,000 entries...");

		createXml(10000,
				OUTPUT_FOLDER + File.separatorChar + "large-person-10000.xml");
		log(LogService.LOG_INFO,
				"Completed generation of large XML with 10,000 entries...");
	}

	private void createXml(int nbrElements, String fileName) throws Exception {
		PersonsType persons = new PersonsType();

		for (int i = 0; i < nbrElements; i++) {
			PersonType person = new PersonType();
			person.setActive(RandomUtils.nextInt(0, 2) == 0);

			person.setPostCode(RandomStringUtils.randomAlphanumeric(5));

			person.setLastName(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(5, 15)));

			person.setFirstName(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(3, 10)));

			person.setCountry(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(4, 11)));

			person.setCity(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(4, 11)));

			person.setAddress2(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(4, 11)));

			person.setAddress1(
					RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(4, 11)));

			persons.getPerson().add(person);
		}

		manager.unbind(PersonsType.class, persons).to(new File(fileName));
	}

	private void readLargeXmlWithModabi(File file)
			throws FileNotFoundException, IOException {
		long start = System.currentTimeMillis();

		manager.bind().from(file).resolve();

		long end = System.currentTimeMillis();

		log(LogService.LOG_INFO,
				"Modabi - (" + file + "): Time taken in ms: " + (end - start));
	}

	private void readLargeXmlWithStax(File file) throws FactoryConfigurationError,
			XMLStreamException, JAXBException, IOException {

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

			log(LogService.LOG_INFO,
					"STax - (" + file + "): Time taken in ms: " + (end - start));
		}
	}

	private void readLargeXmlWithFasterStax(File file)
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

			log(LogService.LOG_INFO,
					"Woodstox - (" + file + "): Time taken in ms: " + (end - start));
		}
	}
}

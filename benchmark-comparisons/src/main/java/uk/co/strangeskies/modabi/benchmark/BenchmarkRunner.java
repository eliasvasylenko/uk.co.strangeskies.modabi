package uk.co.strangeskies.modabi.benchmark;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import uk.co.strangeskies.modabi.io.structured.StructuredDataSource;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLSource;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

public class BenchmarkRunner {
	private static final String OUTPUT_FOLDER = System.getProperty("user.home")
			+ File.separatorChar + "xml-benchmark";

	private final SchemaManager manager = new SchemaManagerImpl();

	public static void main(String[] args) {
		new BenchmarkRunner().run();
	}

	public void run() {
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

			boolean createXml = true;
			System.out.println("Will create XML files? " + createXml);
			if (createXml) {
				createXmlPortfolio();
			}

			System.gc();
			System.gc();

			for (int i = 0; i < 10; i++) {
				readLargeXmlWithModabi(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100.xml"));
				readLargeXmlWithModabi(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000.xml"));
				readLargeXmlWithModabi(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"));

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

	private void createXmlPortfolio() throws Exception {
		createXml(100, OUTPUT_FOLDER + File.separatorChar + "large-person-100.xml");
		System.out.println("Completed generation of large XML with 100 entries...");

		createXml(1000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-1000.xml");
		System.out
				.println("Completed generation of large XML with 1,000 entries...");

		createXml(10000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-10000.xml");
		System.out
				.println("Completed generation of large XML with 10,000 entries...");
	}

	private void createXml(int nbrElements, String fileName) throws Exception {
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
			manager.unbind(new XMLTarget(fos), PersonsType.class, persons);
			fos.flush();
		} finally {
			fos.close();
		}
	}

	private void readLargeXmlWithModabi(File file) {
		long start = System.currentTimeMillis();
		long memstart = Runtime.getRuntime().freeMemory();
		long memend = 0L;

		FileInputStream fis;

		try {
			fis = new FileInputStream(file);
			try {
				manager.bind(new XMLSource(fis));
				memend = Runtime.getRuntime().freeMemory();

				long end = System.currentTimeMillis();

				System.out.println("STax - (" + file + "): - Total memory used: "
						+ (memstart - memend));

				System.out.println("STax - (" + file + "): Time taken in ms: "
						+ (end - start));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readLargeXmlWithStax(File file)
			throws FactoryConfigurationError, XMLStreamException,
			FileNotFoundException, JAXBException {

		// set up a StAX reader
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileReader(file));

		JAXBContext ucontext = JAXBContext.newInstance(PersonType.class);

		Unmarshaller unmarshaller = ucontext.createUnmarshaller();

		long start = System.currentTimeMillis();
		long memstart = Runtime.getRuntime().freeMemory();
		long memend = 0L;

		try {
			xmlr.nextTag();
			xmlr.require(XMLStreamConstants.START_ELEMENT, null, "persons");

			xmlr.nextTag();
			while (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
				unmarshaller.unmarshal(xmlr, PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("STax - (" + file + "): - Total memory used: "
					+ (memstart - memend));

			System.out.println("STax - (" + file + "): Time taken in ms: "
					+ (end - start));

		} finally {
			xmlr.close();
		}

	}

	private void readLargeXmlWithFasterStax(File file)
			throws FactoryConfigurationError, XMLStreamException,
			FileNotFoundException, JAXBException {

		// set up a StAX reader
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlr = xmlif.createXMLStreamReader(new FileReader(file));

		JAXBContext ucontext = JAXBContext.newInstance(PersonType.class);

		Unmarshaller unmarshaller = ucontext.createUnmarshaller();

		long start = System.currentTimeMillis();
		long memstart = Runtime.getRuntime().freeMemory();
		long memend = 0L;

		try {
			xmlr.nextTag();
			xmlr.require(XMLStreamConstants.START_ELEMENT, null, "persons");

			xmlr.nextTag();
			while (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT) {
				unmarshaller.unmarshal(xmlr, PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("Woodstox - (" + file + "): Total memory used: "
					+ (memstart - memend));

			System.out.println("Woodstox - (" + file + "): Time taken in ms: "
					+ (end - start));

		} finally {
			xmlr.close();
		}

	}
}

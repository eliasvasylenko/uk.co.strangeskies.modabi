package uk.co.strangeskies.modabi.benchmark;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;
import uk.co.strangeskies.modabi.xml.impl.XMLSource;
import uk.co.strangeskies.modabi.xml.impl.XMLTarget;

interface PersonType {
	String getFirstName();

	String getLastName();

	String getAddress1();

	String getAddress2();

	String getPostCode();

	String getCity();

	String getCountry();

	boolean isActive();
}

interface PersonsType {
	List<PersonType> getPerson();
}

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

			manager.registerSchemaBinding(new XMLSource(getClass()
					.getResourceAsStream("/BenchmarkSchema.xml")));

			boolean createXml = true;
			System.out.println("Will create XML files? " + createXml);
			if (createXml) {
				createXmlPortfolio();
			}

			System.gc();
			System.gc();

			for (int i = 0; i < 10; i++) {
				readLargeFileWithJaxb(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"), 10000);
				readLargeFileWithJaxb(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100000.xml"), 100000);
				readLargeFileWithJaxb(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000000.xml"), 1000000);

				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"), 10000);
				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100000.xml"), 100000);
				readLargeXmlWithStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000000.xml"), 1000000);

				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-10000.xml"), 10000);
				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-100000.xml"), 100000);
				readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER + File.separatorChar
						+ "large-person-1000000.xml"), 1000000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createXmlPortfolio() throws Exception {
		createXml(10000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-10000.xml");
		System.out
				.println("Completed generation of large XML with 10,000 entries...");
		createXml(100000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-100000.xml");
		System.out
				.println("Completed generation of large XML with 100,000 entries...");
		createXml(1000000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-1000000.xml");
		System.out
				.println("Completed generation of large XML with 1,000,000 entries...");
	}

	private void createXml(int nbrElements, String fileName) throws Exception {
		PersonsType persons = new PersonsType() {
			private final List<PersonType> persons = new ArrayList<>();

			@Override
			public List<PersonType> getPerson() {
				return persons;
			}
		};
		List<PersonType> personList = persons.getPerson();
		PodamFactory factory = new PodamFactoryImpl();
		for (int i = 0; i < nbrElements; i++)
			personList.add(factory.manufacturePojo(PersonType.class));

		File file = new File(fileName);

		OutputStream fos = new FileOutputStream(file);

		try {
			manager.unbind(new XMLTarget(fos), persons);
			fos.flush();
		} finally {
			fos.close();
		}
	}

	private void readLargeFileWithJaxb(File file, int nbrRecords)
			throws Exception {

		JAXBContext ucontext = JAXBContext
				.newInstance("xml.integration.jemos.co.uk.large_file");
		Unmarshaller unmarshaller = ucontext.createUnmarshaller();

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

		long start = System.currentTimeMillis();
		long memstart = Runtime.getRuntime().freeMemory();
		long memend = 0L;

		try {
			@SuppressWarnings("unchecked")
			JAXBElement<PersonsType> root = (JAXBElement<PersonsType>) unmarshaller
					.unmarshal(bis);

			root.getValue().getPerson().size();

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("JAXB (" + nbrRecords + "): - Total Memory used: "
					+ (memstart - memend));

			System.out.println("JAXB (" + nbrRecords + "): Time taken in ms: "
					+ (end - start));

		} finally {
			bis.close();
		}

	}

	private void readLargeXmlWithStax(File file, int nbrRecords)
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

			System.out.println("STax - (" + nbrRecords + "): - Total memory used: "
					+ (memstart - memend));

			System.out.println("STax - (" + nbrRecords + "): Time taken in ms: "
					+ (end - start));

		} finally {
			xmlr.close();
		}

	}

	private void readLargeXmlWithFasterStax(File file, int nbrRecords)
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

			System.out.println("Woodstox - (" + nbrRecords + "): Total memory used: "
					+ (memstart - memend));

			System.out.println("Woodstox - (" + nbrRecords + "): Time taken in ms: "
					+ (end - start));

		} finally {
			xmlr.close();
		}

	}
}

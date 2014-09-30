package uk.co.strangeskies.modabi.benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Loggers;

import sun.misc.IOUtils;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.strangeskies.modabi.schema.processing.SchemaManager;
import uk.co.strangeskies.modabi.schema.processing.impl.SchemaManagerImpl;

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

	public static void main(String[] args) {
		BenchmarkRunner main = new BenchmarkRunner();
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

			String createXmlStr = System.getProperty("create.xml");
			System.out.println("-Dcreate.xml property was set as "
					+ createXmlStr);

			boolean createXml = false;

			if (createXmlStr != null && createXmlStr.equalsIgnoreCase("true")) {
				createXml = true;
			}

			System.out.println("Will create XML files? " + createXml);

			if (createXml) {
				main.createXmlPortfolio(main);
			}

			System.gc();
			System.gc();

			for (int i = 0; i < 10; i++) {

				main.readLargeFileWithJaxb(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-10000.xml"), 10000);
				main.readLargeFileWithJaxb(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-100000.xml"),
						100000);
				main.readLargeFileWithJaxb(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-1000000.xml"),
						1000000);

				main.readLargeXmlWithStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-10000.xml"), 10000);
				main.readLargeXmlWithStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-100000.xml"),
						100000);
				main.readLargeXmlWithStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-1000000.xml"),
						1000000);

				main.readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-10000.xml"), 10000);
				main.readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-100000.xml"),
						100000);
				main.readLargeXmlWithFasterStax(new File(OUTPUT_FOLDER
						+ File.separatorChar + "large-person-1000000.xml"),
						1000000);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createXmlPortfolio(BenchmarkRunner main) throws Exception {
		main.createXml(10000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-10000.xml");
		System.out
				.println("Completed generation of large XML with 10,000 entries...");
		main.createXml(100000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-100000.xml");
		System.out
				.println("Completed generation of large XML with 100,000 entries...");
		main.createXml(1000000, OUTPUT_FOLDER + File.separatorChar
				+ "large-person-1000000.xml");
		System.out
				.println("Completed generation of large XML with 1,000,000 entries...");
	}

	private void createXml(int nbrElements, String fileName) throws Exception {
		SchemaManager manager = new SchemaManagerImpl();

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
			manager.unbind(model, fos, persons);
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

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				file));

		long start = System.currentTimeMillis();
		long memstart = Runtime.getRuntime().freeMemory();
		long memend = 0L;

		try {
			JAXBElement<PersonsType> root = (JAXBElement<PersonsType>) unmarshaller
					.unmarshal(bis);

			root.getValue().getPerson().size();

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("JAXB (" + nbrRecords
					+ "): - Total Memory used: " + (memstart - memend));

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
		XMLStreamReader xmlr = xmlif
				.createXMLStreamReader(new FileReader(file));

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

				JAXBElement<PersonType> pt = unmarshaller.unmarshal(xmlr,
						PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("STax - (" + nbrRecords
					+ "): - Total memory used: " + (memstart - memend));

			System.out.println("STax - (" + nbrRecords
					+ "): Time taken in ms: " + (end - start));

		} finally {
			xmlr.close();
		}

	}

	private void readLargeXmlWithFasterStax(File file, int nbrRecords)
			throws FactoryConfigurationError, XMLStreamException,
			FileNotFoundException, JAXBException {

		// set up a StAX reader
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		XMLStreamReader xmlr = xmlif
				.createXMLStreamReader(new FileReader(file));

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

				JAXBElement<PersonType> pt = unmarshaller.unmarshal(xmlr,
						PersonType.class);

				if (xmlr.getEventType() == XMLStreamConstants.CHARACTERS) {
					xmlr.next();
				}
			}

			memend = Runtime.getRuntime().freeMemory();

			long end = System.currentTimeMillis();

			System.out.println("Woodstox - (" + nbrRecords
					+ "): Total memory used: " + (memstart - memend));

			System.out.println("Woodstox - (" + nbrRecords
					+ "): Time taken in ms: " + (end - start));

		} finally {
			xmlr.close();
		}

	}
}

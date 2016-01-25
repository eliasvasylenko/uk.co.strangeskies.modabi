package uk.co.strangeskies.modabi.eclipse.xml.editors;

import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;

public interface ModabiXmlProviderAdapter extends ModabiXmlPreviewAdapter {
	public String getPreviewSceneFXML();

	public String getPreviewFXML();

	public List<String> getPreviewCSSFiles();

	public String getPreviewResourceBundle();

	public List<URL> getPreviewClasspath();

	public IFile getFile();
}

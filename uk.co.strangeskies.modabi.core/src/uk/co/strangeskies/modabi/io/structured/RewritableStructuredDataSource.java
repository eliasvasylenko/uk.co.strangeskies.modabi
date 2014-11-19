package uk.co.strangeskies.modabi.io.structured;

import uk.co.strangeskies.modabi.io.DataTarget;
import uk.co.strangeskies.modabi.namespace.QualifiedName;

/**
 * Not entirely sure what the most sensible API is for this... Will have to work
 * at it from the other end to see what is needed. Eventually this should
 * provide means to update an existing data representation with an updated
 * object binding. The idea is to allow updates whilst retaining formatting
 * wherever possible.
 * 
 * There may turn out to be limits to the feasibility of this feature...
 * 
 * @author eli
 *
 */
public interface RewritableStructuredDataSource extends
		BufferedStructuredDataSource {
	DataTarget overwriteProperty(QualifiedName name);

	@Override
	public void endChild();

	public boolean deleteChild();

	public StructuredDataTarget editAtLocation();

	public Object anchorLocation();

	public void navigateToAnchor(Object key);

	public default StructuredDataTarget editAtAnchor(Object key) {
		navigateToAnchor(key);
		return editAtLocation();
	}
}

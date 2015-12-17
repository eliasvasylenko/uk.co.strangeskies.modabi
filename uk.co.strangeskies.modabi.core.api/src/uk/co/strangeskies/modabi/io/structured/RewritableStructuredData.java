package uk.co.strangeskies.modabi.io.structured;

public interface RewritableStructuredData
		extends NavigableStructuredDataSource, StructuredDataTarget {
	@Override
	RewritableStructuredData endChild();
}

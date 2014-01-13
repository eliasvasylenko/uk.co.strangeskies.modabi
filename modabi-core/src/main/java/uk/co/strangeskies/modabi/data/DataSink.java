package uk.co.strangeskies.modabi.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface DataSink {
	public DataSink binary(byte[] value);

	public DataSink string(String value);

	public DataSink integer(BigInteger value);

	public DataSink decimal(BigDecimal value);

	public DataSink intValue(int value);

	public DataSink longValue(long value);

	public DataSink floatValue(float value);

	public DataSink doubleValue(double value);

	public DataSink booleanValue(boolean value);
}

package uk.co.strangeskies.modabi.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TerminatingDataSink extends DataSink {
	public TerminatingDataSink binary(byte[] value);

	public TerminatingDataSink string(String value);

	public TerminatingDataSink integer(BigInteger value);

	public TerminatingDataSink decimal(BigDecimal value);

	public TerminatingDataSink intValue(int value);

	public TerminatingDataSink longValue(long value);

	public TerminatingDataSink floatValue(float value);

	public TerminatingDataSink doubleValue(double value);

	public TerminatingDataSink booleanValue(boolean value);

	public void end();
}

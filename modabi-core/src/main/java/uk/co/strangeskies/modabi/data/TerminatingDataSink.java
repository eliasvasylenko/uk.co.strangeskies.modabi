package uk.co.strangeskies.modabi.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TerminatingDataSink extends DataTarget {
	@Override
	public TerminatingDataSink binary(byte[] value);

	@Override
	public TerminatingDataSink string(String value);

	@Override
	public TerminatingDataSink integer(BigInteger value);

	@Override
	public TerminatingDataSink decimal(BigDecimal value);

	@Override
	public TerminatingDataSink intValue(int value);

	@Override
	public TerminatingDataSink longValue(long value);

	@Override
	public TerminatingDataSink floatValue(float value);

	@Override
	public TerminatingDataSink doubleValue(double value);

	@Override
	public TerminatingDataSink booleanValue(boolean value);

	public void end();
}

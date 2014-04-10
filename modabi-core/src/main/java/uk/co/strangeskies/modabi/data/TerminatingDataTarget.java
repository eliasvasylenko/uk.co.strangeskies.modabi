package uk.co.strangeskies.modabi.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface TerminatingDataTarget extends DataTarget {
	@Override
	public TerminatingDataTarget binary(byte[] value);

	@Override
	public TerminatingDataTarget string(String value);

	@Override
	public TerminatingDataTarget integer(BigInteger value);

	@Override
	public TerminatingDataTarget decimal(BigDecimal value);

	@Override
	public TerminatingDataTarget intValue(int value);

	@Override
	public TerminatingDataTarget longValue(long value);

	@Override
	public TerminatingDataTarget floatValue(float value);

	@Override
	public TerminatingDataTarget doubleValue(double value);

	@Override
	public TerminatingDataTarget booleanValue(boolean value);

	public void end();
}

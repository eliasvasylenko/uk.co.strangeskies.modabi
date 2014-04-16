package uk.co.strangeskies.modabi.data.io;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface BufferingDataTarget extends TerminatingDataTarget {
	public BufferedDataSource buffer();

	@Override
	public BufferingDataTarget binary(byte[] value);

	@Override
	public BufferingDataTarget string(String value);

	@Override
	public BufferingDataTarget integer(BigInteger value);

	@Override
	public BufferingDataTarget decimal(BigDecimal value);

	@Override
	public BufferingDataTarget intValue(int value);

	@Override
	public BufferingDataTarget longValue(long value);

	@Override
	public BufferingDataTarget floatValue(float value);

	@Override
	public BufferingDataTarget doubleValue(double value);

	@Override
	public BufferingDataTarget booleanValue(boolean value);
}

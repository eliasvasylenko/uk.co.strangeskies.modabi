package uk.co.strangeskies.modabi.data.io;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface DataTarget {
	public DataTarget binary(byte[] value);

	public DataTarget string(String value);

	public DataTarget integer(BigInteger value);

	public DataTarget decimal(BigDecimal value);

	public DataTarget intValue(int value);

	public DataTarget longValue(long value);

	public DataTarget floatValue(float value);

	public DataTarget doubleValue(double value);

	public DataTarget booleanValue(boolean value);
}

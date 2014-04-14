package uk.co.strangeskies.modabi.data;

import java.math.BigDecimal;
import java.math.BigInteger;

public interface DataSource {
	public byte[] binary();

	public String string();

	public BigInteger integer();

	public BigDecimal decimal();

	public int intValue();

	public long longValue();

	public float floatValue();

	public double doubleValue();

	public boolean booleanValue();
}

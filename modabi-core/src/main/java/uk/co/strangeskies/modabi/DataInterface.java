package uk.co.strangeskies.modabi;

import java.math.BigDecimal;
import java.math.BigInteger;

import uk.co.strangeskies.modabi.data.BufferedDataSource;
import uk.co.strangeskies.modabi.data.DataSource;
import uk.co.strangeskies.modabi.data.DataTarget;

public interface DataInterface extends DataSource, DataTarget {
	public static DataInterface forSource(DataSource source) {
		return new DataInterface() {
			@Override
			public DataTarget string(String value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget longValue(long value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget integer(BigInteger value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget intValue(int value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget floatValue(float value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget doubleValue(double value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget decimal(BigDecimal value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget booleanValue(boolean value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public DataTarget binary(byte[] value) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String string() {
				return source.string();
			}

			@Override
			public long longValue() {
				return source.longValue();
			}

			@Override
			public BigInteger integer() {
				return source.integer();
			}

			@Override
			public int intValue() {
				return source.intValue();
			}

			@Override
			public float floatValue() {
				return source.floatValue();
			}

			@Override
			public double doubleValue() {
				return source.doubleValue();
			}

			@Override
			public BigDecimal decimal() {
				return source.decimal();
			}

			@Override
			public boolean booleanValue() {
				return source.booleanValue();
			}

			@Override
			public byte[] binary() {
				return source.binary();
			}

			@Override
			public BufferedDataSource buffer() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

	public static DataInterface forTarget(DataTarget target) {
		return null;
	}
}

package uk.co.strangeskies.modabi.data.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

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

	public <T extends DataTarget> T pipe(T target, int items);

	public BufferedDataSource buffer(int items);

	public static DataSource repeat(byte[] data) {
		return new RepeatingDataSource(c -> c.binary(data)) {
			@Override
			public byte[] binary() {
				return data;
			}
		};
	}

	public static DataSource repeat(String data) {
		return new RepeatingDataSource(c -> c.string(data)) {
			@Override
			public String string() {
				return data;
			}
		};
	}

	public static DataSource repeat(BigInteger data) {
		return new RepeatingDataSource(c -> c.integer(data)) {
			@Override
			public BigInteger integer() {
				return data;
			}
		};
	}

	public static DataSource repeat(BigDecimal data) {
		return new RepeatingDataSource(c -> c.decimal(data)) {
			@Override
			public BigDecimal decimal() {
				return data;
			}
		};
	}

	public static DataSource repeat(int data) {
		return new RepeatingDataSource(c -> c.intValue(data)) {
			@Override
			public int intValue() {
				return data;
			}
		};
	}

	public static DataSource repeat(long data) {
		return new RepeatingDataSource(c -> c.longValue(data)) {
			@Override
			public long longValue() {
				return data;
			}
		};
	}

	public static DataSource repeat(float data) {
		return new RepeatingDataSource(c -> c.floatValue(data)) {
			@Override
			public float floatValue() {
				return data;
			}
		};
	}

	public static DataSource repeat(double data) {
		return new RepeatingDataSource(c -> c.doubleValue(data)) {
			@Override
			public double doubleValue() {
				return data;
			}
		};
	}

	public static DataSource repeat(boolean data) {
		return new RepeatingDataSource(c -> c.booleanValue(data)) {
			@Override
			public boolean booleanValue() {
				return data;
			}
		};
	}

	abstract class RepeatingDataSource implements DataSource {
		private final Consumer<DataTarget> dumpValue;

		public RepeatingDataSource(Consumer<DataTarget> dumpValue) {
			this.dumpValue = dumpValue;
		}

		@Override
		public byte[] binary() {
			throw new ClassCastException();
		}

		@Override
		public String string() {
			throw new ClassCastException();
		}

		@Override
		public BigInteger integer() {
			throw new ClassCastException();
		}

		@Override
		public BigDecimal decimal() {
			throw new ClassCastException();
		}

		@Override
		public int intValue() {
			throw new ClassCastException();
		}

		@Override
		public long longValue() {
			throw new ClassCastException();
		}

		@Override
		public float floatValue() {
			throw new ClassCastException();
		}

		@Override
		public double doubleValue() {
			throw new ClassCastException();
		}

		@Override
		public boolean booleanValue() {
			throw new ClassCastException();
		}

		@Override
		public <T extends DataTarget> T pipe(T target, int items) {
			if (items < 0)
				throw new ArrayIndexOutOfBoundsException(-1);

			for (int item = 0; item < items; item++)
				dumpValue.accept(target);

			return target;
		}

		@Override
		public BufferedDataSource buffer(int items) {
			return pipe(BufferedDataSource.from(), items).buffer();
		}
	}
}

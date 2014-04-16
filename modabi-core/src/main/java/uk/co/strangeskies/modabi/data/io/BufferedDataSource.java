package uk.co.strangeskies.modabi.data.io;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface BufferedDataSource extends TerminatingDataSource {
	void reset();

	public static BufferingDataTarget from() {
		return new BufferingDataTarget() {
			class BufferedDataSourceImpl implements BufferedDataSource {
				private final List<DataSource> dataSequence;
				private int index;

				public BufferedDataSourceImpl(List<DataSource> dataSequence) {
					index = 0;
					this.dataSequence = dataSequence;
				}

				@Override
				public String string() {
					return dataSequence.get(index++).string();
				}

				@Override
				public long longValue() {
					return dataSequence.get(index++).longValue();
				}

				@Override
				public BigInteger integer() {
					return dataSequence.get(index++).integer();
				}

				@Override
				public int intValue() {
					return dataSequence.get(index++).intValue();
				}

				@Override
				public float floatValue() {
					return dataSequence.get(index++).floatValue();
				}

				@Override
				public double doubleValue() {
					return dataSequence.get(index++).doubleValue();
				}

				@Override
				public BigDecimal decimal() {
					return dataSequence.get(index++).decimal();
				}

				@Override
				public boolean booleanValue() {
					return dataSequence.get(index++).booleanValue();
				}

				@Override
				public byte[] binary() {
					return dataSequence.get(index++).binary();
				}

				@Override
				public int size() {
					return dataSequence.size();
				}

				@Override
				public void reset() {
					index = 0;
				}

				@Override
				public <T extends DataTarget> T pipe(T target, int items) {
					for (int start = index; index < start; index++)
						dataSequence.get(index).pipeNext(target);

					return target;
				}

				@Override
				public BufferedDataSource buffer() {
					return buffer(dataSequence.size() - index);
				}

				@Override
				public BufferedDataSource buffer(int items) {
					return new BufferedDataSourceImpl(dataSequence.subList(index, index
							+ items));
				}
			}

			private List<DataSource> dataSequence = new ArrayList<>();
			private boolean terminated;

			@Override
			public BufferingDataTarget binary(byte[] value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget string(String value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget integer(BigInteger value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget decimal(BigDecimal value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget intValue(int value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget longValue(long value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget floatValue(float value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget doubleValue(double value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferingDataTarget booleanValue(boolean value) {
				dataSequence.add(DataSource.repeat(value));
				return this;
			}

			@Override
			public BufferedDataSource buffer() {
				terminate();
				return new BufferedDataSourceImpl(dataSequence);
			}

			@Override
			public void terminate() {
				if (!terminated)
					dataSequence = Collections.unmodifiableList(dataSequence);
				terminated = true;
			}

			@Override
			public boolean isTerminated() {
				return terminated;
			}
		};
	}
}
